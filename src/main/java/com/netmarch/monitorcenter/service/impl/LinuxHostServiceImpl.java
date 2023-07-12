package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.service.CacheService;
import com.netmarch.monitorcenter.service.LinuxHostService;
import com.netmarch.monitorcenter.service.NodeAgentService;
import com.netmarch.monitorcenter.service.SnmpNodeService;
import jnetman.snmp.SnmpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/***
 * 不同的snmp设备具有不同的指标(内存、cpu等),不同设备不同时具备各种不同的指标
 * 此处是针对linux主机的封装,以实现对客户端方法隐藏
 */
@Service
@Slf4j
public class LinuxHostServiceImpl implements LinuxHostService{

    @Autowired
    private SnmpNodeService nodeService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private NodeAgentService agentService;



    @Override
    public LinkedList<SnmpNode> queryAllHosts() {
        return nodeService.loadAllNodes();
    }

    /***
     * 保存抓取来的信息
     * @param captured
     * @return
     */
    @Override
    public int saveCaptured(CapturedData captured) {
        log.info("收到采集数据入库请求:{}",captured.getIfIpAddr());
        return saveCaptured(Arrays.asList(captured));
    }

    @Override
    public int saveCaptured(List<CapturedData> captured) {

        Long nodeId = null;
        SnmpCPUUsage cpuUsage = null;
        List<SnmpFileSystemUsage> fsUsage = null;
        SnmpLoad load = null;
        SnmpMemoryUsage memUsage = null;
        List<SnmpIfCardTraffic> traffics = null;


        for (CapturedData data:captured)
        {
            nodeId = data.getNodeId();
            cpuUsage = data.getCpuUsage();
            fsUsage = data.getFsUsage();
            load = data.getLoad();
            memUsage = data.getMemoryUsage();
            traffics = data.getTraffics();

            //先保存网卡流量和网卡信息,下面会用到会用到ip信息
            if(traffics != null) {
                traffics.forEach(traffic -> computeTraffic(traffic.getNodeId(), traffic));
                agentService.saveIfCardTraffics(traffics);
            }

            //计算增量值并入库
            if (cpuUsage !=null){
                cpuUsage = computeCpu(nodeId,cpuUsage);
                agentService.saveCpuUsage(cpuUsage);
            }

            if(CollectionUtils.isNotEmpty(fsUsage)) {
                agentService.saveFileSystemUsage(preHandle(fsUsage));
            }

            if(load != null) {
                agentService.saveLoad(load);
            }

            if(memUsage != null) {
                agentService.saveMemoryUsage(preHandle2(memUsage));
            }
        }
        return 0;
    }


    SnmpMemoryUsage preHandle2(SnmpMemoryUsage usage)
    {
        Long nodeId = usage.getNodeId();
        SnmpMemory cachedMem = cacheService.getCachedKV(CacheService.CACHED_MEM,
                (SnmpMemory mem)->{
                    return mem.getNodeId().equals(nodeId);
                },
                SnmpMemory.class);

        if(cachedMem == null)
        {
            SnmpMemory memory = new SnmpMemory.Builder(usage).build();
            agentService.saveMemory(memory);
            Long memId = memory.getId();
            usage.setMemId(memId);
        }else
        {
            Long memId = cachedMem.getId();
            usage.setMemId(memId);
        }
        return usage;
    }

    /***
     * 预处理
     * @param usages
     * @return
     */
    LinkedList<SnmpFileSystemUsage> preHandle(List<SnmpFileSystemUsage> usages)
    {
       return usages.stream()
            //1、排除无效的挂载点
            .filter(usage->{
                        String name = usage.getHrStorageDescr();
                        Long totalSize = usage.getHrStorageSize();
                        String storageType = usage.getHrStorageType();

                        //挂载点名称为null
                        return StringUtils.isNotBlank(name)
                                //存储空间为0则视为无效挂载
                                &&totalSize>0
                                //存储空间类型为null
                                &&StringUtils.isNotBlank(storageType);
                    }
            ).map(usage->{
                String oidType = usage.getHrStorageType();
                usage.setHrStorageType(agentService.translateStorageType(oidType));
                return usage;
            }).map(usage->{

            Long nodeId = usage.getNodeId();
            SnmpFileSystem cachedFs = cacheService.getCachedKV(CacheService.CACHED_FILESYSTEM,
                    (SnmpFileSystem fs)->
                            fs.getHrStorageDescr().equals(usage.getHrStorageDescr())
                            &&fs.getNodeId().equals(nodeId)
            ,SnmpFileSystem.class);

            //如果根据nodeId和mount节点名查询不到这个文件系统的缓存,把他保存起来
            if(cachedFs == null)
            {
                SnmpFileSystem fileSystem = new SnmpFileSystem.Builder(usage).build();
                agentService.saveFileSystem(fileSystem);
                Long fsId = fileSystem.getId();
                usage.setFsId(fsId);
            }else
            {
                Long fsId = cachedFs.getId();
                usage.setFsId(fsId);
            }
            return usage;
        }).collect(Collectors.toCollection(LinkedList::new));
    }

    /***
     * 计算网卡上下行流量变化
     * @param traffic
     */
    SnmpIfCardTraffic computeTraffic(Long nodeId, SnmpIfCardTraffic traffic)
    {

        String ipAddr = traffic.getIfIpAddr();

        SnmpIfCard ifCard = new SnmpIfCard.Builder(traffic).build();

        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        if(cachedIfCard == null)
        {
        //从采集的信息里提取网卡信息
            long primaryKey = agentService.saveIfCard(ifCard);
            if(primaryKey>0)
            {
                traffic.setIfCardId(primaryKey);
            }
        }else
        {
            Long ifCardId = cachedIfCard.getId();
            traffic.setIfCardId(ifCardId);

            ifCard.setId(ifCardId);
            //如果缓存里面的网卡信息与数据库不一致,更新数据库，减少不必要的磁盘IO
            if(!cachedIfCard.equals(ifCard)) {
                agentService.updateIfCard(ifCard);
            }
        }


        SnmpIfCardTraffic lastStatus = cacheService.getStatus(CacheService.STATUS_TRAFFIC_COUNTER,"lastStatus" ,ipAddr, SnmpIfCardTraffic.class);

        if(lastStatus != null)
        {
            //当前上行下行计数器值
            long currentIfOutOctets = traffic.getIfOutOctets().longValue();
            long currentIfInOctets = traffic.getIfInOctets().longValue();


            long previousIfOutOctets = lastStatus.getIfOutOctets().longValue();
            long previousIfInOctets = lastStatus.getIfInOctets().longValue();


            long inOctetsDelta = currentIfInOctets - previousIfInOctets;
            long outOctetsDelta = currentIfOutOctets - previousIfOutOctets;

            traffic.setIfUpLinkBytes(outOctetsDelta);

            traffic.setIfDownLinkBytes(inOctetsDelta);

            //存到redis内存里
            cacheService.updateStatus(CacheService.STATUS_TRAFFIC_COUNTER,"lastStatus",ipAddr,cacheService.toJsonString(traffic));


        }else
        {
            cacheService.addStatus(CacheService.STATUS_TRAFFIC_COUNTER,"lastStatus" ,ipAddr, cacheService.toJsonString(traffic));
        }

        return traffic;
    }


    /***
     * 根据计数器的值计算出cpu使用率
     * @param nodeId
     * @param usage
     * @return
     */

    SnmpCPUUsage computeCpu(Long nodeId, SnmpCPUUsage usage)
    {
        float [] percentages = new float[4];

        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        String ipAddr = cachedIfCard.getIfIpAddr();
        SnmpCPUUsage lastStatus = cacheService.getStatus(CacheService.STATUS_CPU_COUNTER,"lastStatus",ipAddr,SnmpCPUUsage.class);


        if(lastStatus != null)
        {
            usage.setId(lastStatus.getId());

            long [] last = new long[]{
                    usage.getSsCpuRawIdle().longValue(),
                    usage.getSsCpuRawUser().longValue(),
                    usage.getSsCpuRawWait().longValue(),
                    usage.getSsCpuRawSystem().longValue()
            };

            long [] current = new long[]{
                    lastStatus.getSsCpuRawIdle().longValue(),
                    lastStatus.getSsCpuRawUser().longValue(),
                    lastStatus.getSsCpuRawWait().longValue(),
                    lastStatus.getSsCpuRawSystem().longValue()
            };

            /***
             * 上一次计数器总和
             */
            long lastTotal = Arrays.stream(last).sum();
            /***
             * 本次计数器总和
             */
            long currentTotal = Arrays.stream(current).sum();

            long totalDelta = currentTotal - lastTotal;

            long []delta = cacheService.computeDelta(last,current);

            for(int i = 0 ;i < delta.length ;i++)
            {
                percentages[i] = scale(delta[i]/(totalDelta*1.0f)* 100);
            }

            usage.setCpuIdle(percentages[0]);

            usage.setCpuUser(percentages[1]);

            usage.setCpuWait(percentages[2]);

            usage.setCpuSys(percentages[3]);

            //更新缓存
            cacheService.updateStatus(CacheService.STATUS_CPU_COUNTER,"lastStatus",ipAddr,cacheService.toJsonString(usage));

        }
        else {

            //cpu计数器信息还没存储到缓存
            //存到redis内存里
            cacheService.addStatus(CacheService.STATUS_CPU_COUNTER,"lastStatus" ,ipAddr, cacheService.toJsonString(usage));
        }
        return usage;
    }

    /***
     * 保留2位小数
     * @param floatValue
     * @return
     */
    float scale(Float floatValue)
    {
        DecimalFormat format = new DecimalFormat("#.00");
        String str = format.format(floatValue);
        return Float.parseFloat(str);
    }

    @Override
    public SnmpClient getSnmpClient(String address) {
        return nodeService.getSnmpClient(address);
    }

    @Override
    public void saveJobStatus(ConcurrentHashMap<String, Object> snapShot) {

    }
}
