package com.netmarch.monitorcenter.controller;

import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.service.CacheService;
import com.netmarch.monitorcenter.util.Login;
import com.netmarch.monitorcenter.util.RequestDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("perf")
@Controller
public class ServerPerformanceController {

    @Autowired
    CacheService cacheService;

    TreeMap<Long, SnmpNode> nodeMapping = new TreeMap<>();

    @PostConstruct
    public void init()
    {
        List<SnmpNode> cachedNodes = cacheService.getCachedNodes();
        if(!CollectionUtils.isEmpty(cachedNodes))
        {
            cachedNodes.stream().collect(Collectors.toMap(SnmpNode::getId,Function.identity()))
                    .forEach((id,entity)-> nodeMapping.put(id,entity));
        }

    }

    @GetMapping("index")
    @Login
    public String queryRecentlyPerformance(Map<String, Object> map,String period) {

        return "serverPerformance/index";
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

    <R,T> RequestData processData(String label, String snapshotKey, Class<T> targetClass,
                                         Predicate<? super T> filter, final Function<? super T, ? extends R> mapper)
    {
        if(filter == null)
        {
            //如果没有指定过滤表达式,给一个默认值
            filter = (T entity)->{
                    LocalDateTime createTime = cacheService.getFieldValue(entity, "createTime", LocalDateTime.class);
                    return createTime.getMinute() % 10 == 0
                            &&createTime.getSecond() ==0;
            };
        }
        Map<String,Object> resultMap = new HashMap<>();
        Optional<SnmpNode> node1 = nodeMapping.values().stream().findFirst();
        List<T> list = null;
        if(node1.isPresent())
        {
            String ipAddr1 = node1.get().getAddress();
            list = cacheService.getCachedList(snapshotKey, ipAddr1, targetClass);
            //服务器ip
            resultMap.put("legend", nodeMapping.values().stream().map(SnmpNode::getAddress).collect(Collectors.toList()));

            //批量格式时间MM-dd HH:mm:ss并封送到List
            List<String> xAxis = list.stream()
                    .map(entity->cacheService.getFieldValue(entity,"createTime", LocalDateTime.class))
                    .filter(
                            localDateTime -> localDateTime.getMinute()%10==0 && localDateTime.getSecond() == 0
                    ).map(createTime -> createTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))).collect(Collectors.toList());

            //筛选后的样本大小
            int filteredSize = xAxis.size();

            //由于图表不能显示太多的数据，太多的就会被隐藏，因此只显示最近的20条数据
            xAxis = xAxis.stream().skip(filteredSize>=0?filteredSize-20:filteredSize).collect(Collectors.toList());
            resultMap.put("xAxis",xAxis);

            List<EChartSeries> series = new LinkedList<>();
            for(Map.Entry<Long,SnmpNode> entry: nodeMapping.entrySet())
            {
                SnmpNode node = entry.getValue();

                String ipAddr = node.getAddress();

                List<T> traffics = cacheService.getCachedList(snapshotKey, ipAddr, targetClass);

                List<R> data = traffics.stream()
                        .filter(filter)
                        .skip(filteredSize>=0?filteredSize-20:filteredSize)
                        .map(mapper).collect(Collectors.toList());

                EChartSeries chartSeries = new EChartSeries.Builder()
                        .withName(ipAddr)
                        .withStack(label)
                        .withType("line")
                        .withData((LinkedList<String>) new LinkedList<R>(data))
                        .build();

                if(!CollectionUtils.isEmpty(data)) {
                    series.add(chartSeries);
                }
            }
            resultMap.put("series",series);
        }

        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        requestData.setObjData(resultMap);
        requestData.setCode(StaticObj.R_STATUS_SUCCESS);
        requestData.setMsg(StaticObj.UPDATE_MSG_SUCCESS);
        return requestData;
    }

    @GetMapping("traffic.json")
    @Login
    @ResponseBody
    public RequestData traffic() {

        Function<SnmpIfCardTraffic,String> function = new Function<SnmpIfCardTraffic, String>() {
            @Override
            public String apply(SnmpIfCardTraffic traffic) {
                return String.valueOf(traffic.getIfUpLinkBytes()/1024/1024);
            }
        };

        return processData("上行流量",CacheService.SNAPSHOT_IFCARD_TRAFFIC,SnmpIfCardTraffic.class,null, function);
    }

    @GetMapping("mem.json")
    @Login
    @ResponseBody
    public RequestData memoryUsage()
    {

        Function<SnmpMemoryUsage,String> function = new Function<SnmpMemoryUsage, String>() {
            @Override
            public String apply(SnmpMemoryUsage usage) {

                    long memTotal = usage.getMemTotalReal();
                    long memAvail = usage.getMemAvailReal();
                    long used = memTotal - memAvail;
                    float percentage = (used *1.0f / memTotal * 100.0f);
                    float formatted = scale(percentage);
                    return String.valueOf(formatted);
            }
        };

        return processData("内存使用率",CacheService.SNAPSHOT_MEMORY_USAGE,SnmpMemoryUsage.class,null, function);
    }

    /***
     * 根据nodeId查询对应的负载
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param period 数据周期(格式:数字+[h|d|w|m|y],例如24h,2d,1w,3m)
     * @return
     */
    @GetMapping("load.json")
    @Login
    @ResponseBody
    public RequestData serverLoad(/*LocalDateTime startTime,LocalDateTime endTime,String period*/){

        Function<SnmpLoad,String> function = new Function<SnmpLoad, String>() {
            @Override
            public String apply(SnmpLoad load) {
                float load1 = scale(load.getLoad1());
                return String.valueOf(load1);
            }
        };
        return processData("1分钟负载",CacheService.SNAPSHOT_SERVER_LOAD,SnmpLoad.class,null, function);
    }

    @GetMapping("cpu.json")
    @Login
    @ResponseBody
    public RequestData cpuUsage(/*LocalDateTime startTime,LocalDateTime endTime,String period*/){


        Function<SnmpCPUUsage,String> function = new Function<SnmpCPUUsage, String>() {
            @Override
            public String apply(SnmpCPUUsage usage) {
                Float cpu_sys = usage.getCpuSys();
                Float cpu_user = usage.getCpuUser();
                Float cpu_used = cpu_sys + cpu_user;
                return String.valueOf(scale(cpu_used));
            }
        };
        return processData("1分钟负载",CacheService.SNAPSHOT_CPU_USAGE,SnmpCPUUsage.class,null, function);
    }
}
