package com.netmarch.monitorcenter.scheduled;

import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.service.CacheService;
import com.netmarch.monitorcenter.service.LinuxHostService;
import com.netmarch.monitorcenter.service.NodeAgentService;
import com.netmarch.monitorcenter.service.snmp.SnmpObjectNotation;
import jnetman.snmp.MIB;
import jnetman.snmp.SnmpClient;
import jnetman.snmp.SnmpHelper;
import jnetman.snmp.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Component
@Slf4j
public class SnmpJobScheduler {

    @Autowired
    private LinuxHostService linuxHostService;

    @Autowired
    private NodeAgentService agentService;

    @Autowired
    CacheService cacheService;

    ExecutorService executor = Executors.newCachedThreadPool();

    @PostConstruct
    public void init()
    {
        //加载采集状态
    }


    @PreDestroy
    public void saveState()
    {
        //将采集任务的状态保存起来
    }


    @Scheduled(cron = "${snmp.job.cronExpr}")
    public void run()
    {
        //先查缓存,其次数据库
        List<SnmpNode> hosts = cacheService.getCachedNodes();

        if(CollectionUtils.isEmpty(hosts))
        {
            hosts = linuxHostService.queryAllHosts();
        }

        if(CollectionUtils.isEmpty(hosts)) {
            log.warn("尚未设定采集主机ip信息!");
            return;
        }

        log.info("当前所有主机信息:{}",cacheService.friendlyText(hosts));

        hosts.parallelStream().forEach(node -> {
            CollectDataTask task =new CollectDataTask(node);
            executor.submit(task);
        });

    }

    /***
     * 采集主机信息
     * @param host
     */
    public CapturedData doCollect(SnmpNode host)
    {
        Long nodeId = host.getId();

        String ipAddr = host.getAddress();

        SnmpClient client = linuxHostService.getSnmpClient(ipAddr);

        CapturedData captured = new CapturedData(nodeId,ipAddr);

        try {
            SnmpHelper snmpHelper = new SnmpHelper(client);

            //内存使用率
            Table memory = snmpHelper.getTable(MIB.memory);
            agentService.readObject(memory, SnmpObjectNotation.memoryFieldNames, SnmpMemoryUsage.class,
                    (SnmpMemoryUsage) null,
                    captured::setMemoryUsage,
                    (usage) -> {
                        if(usage !=null) {
                            usage.setNodeId(nodeId);
                            return usage;
                        }
                        return null;
                    });


            //系统运行状态,从这里获取cpu使用率信息
            Table sysStats = snmpHelper.getTable(MIB.systemStats);
            agentService.readObject(sysStats, SnmpObjectNotation.systemStatFields, SnmpCPUUsage.class,
                    (SnmpCPUUsage)null,
                    captured::setCpuUsage,
                    (usage) -> {
                        if(usage !=null) {
                            usage.setNodeId(nodeId);
                            return usage;
                        }
                        return null;
                    });


            //负载
            float [] loadStat = snmpHelper.getFloat(MIB.linuxLoadOID);

            SnmpLoad load = new SnmpLoad(nodeId, loadStat[0], loadStat[1], loadStat[2]);
            load.setNodeId(nodeId);
            captured.setLoad(load);

            //读取ip地址、子网掩码
            Table ipAddrTable = snmpHelper.getTable(MIB.IpAddrTable);

            List<SnmpIfCard> ifCards =  agentService.readList(ipAddrTable,
                    agentService.translateOID(SnmpObjectNotation.ipAddrEntry),
                    SnmpObjectNotation.ipFieldNames, SnmpIfCard.class,
                    null,
                    (SnmpIfCard ifCard) -> {
                        if(ifCard!=null) {
                            //过滤掉回环地址
                            String ifIpAddr = ifCard.getIfIpAddr();
                            return !"127.0.0.1".equals(ifIpAddr);
                        }
                        return false;
                    },(SnmpIfCard ifCard)->{
                        if(ifCard != null) {
                            //设置nodeId
                            ifCard.setNodeId(nodeId);
                            return ifCard;
                        }
                        return null;
                    });

            //网口流量
            Table ifTable = snmpHelper.getTable(MIB.IfTable);
            agentService.readList(ifTable,SnmpObjectNotation.trafficFieldNames,SnmpIfCardTraffic.class,
                    null,
                    (SnmpIfCardTraffic traffic)->{
                        if(traffic != null) {
                            Integer ifIndex = traffic.getIfIndex();
                            //如果网口索引里没有这个IfIndex,则不采集(通常是过滤IfIndex =1的本地回环网卡 lo)
                            List<Integer> allIfIndex = ifCards.stream().map(SnmpIfCard::getIfIndex).collect(Collectors.toList());
                            return allIfIndex.contains(ifIndex);
                        }
                        return false;
                    },
                    captured::setTraffics,
                    (SnmpIfCardTraffic traffic)->{
                        if(traffic != null) {
                            //通过网口索引匹配,将对应网口索引的子网掩码和ip地址拷贝到网口流量信息实体上面
                            Optional<SnmpIfCard> matched = matchIfCard(ifCards, traffic);
                            if (matched.isPresent()) {
                                traffic.setNodeId(nodeId);
                                traffic.setIfIpNetMask(matched.get().getIfIpNetMask());
                                traffic.setIfIpAddr(matched.get().getIfIpAddr());
                            }
                            return traffic;
                        }
                        return null;
                    });


            //文件系统使用情况
            Table hrStorageEntry = snmpHelper.getTable(MIB.hrStorageEntry);

            agentService.readList(hrStorageEntry, SnmpObjectNotation.hrStorageEntryFields,
                    SnmpFileSystemUsage.class,null,
                    captured::setFsUsage,
                    (SnmpFileSystemUsage usage)->{
                        if(usage != null) {
                            usage.setNodeId(nodeId);
                            return usage;
                        }
                        return null;
                    });
        }catch (Exception ex)
        {
            log.error("出错:",ex);
        }
        return captured;
    }

    Optional<SnmpIfCard> matchIfCard(List<SnmpIfCard> ifCards, SnmpIfCardTraffic traffic)
    {
        Integer ifIndex = traffic.getIfIndex();
        Optional<SnmpIfCard> matched = ifCards.stream().filter(ifCard -> {
            return ifCard.getIfIndex().equals(ifIndex);
        }).findFirst();
        return matched;
    }

    class CollectDataTask implements Runnable
    {
        private SnmpNode node;

        public CollectDataTask(SnmpNode node)
        {
            this.node = node;
        }

        @Override
        public void run() {
            CapturedData data = doCollect(node);
            if(data.isNotEmpty()) {
                int effected = linuxHostService.saveCaptured(data);
                String ipAddr = data.getIfIpAddr();
                if (effected > 0) {
                    log.info("{}主机信息采集入库成功!", ipAddr);
                }
            }
        }
    }
}
