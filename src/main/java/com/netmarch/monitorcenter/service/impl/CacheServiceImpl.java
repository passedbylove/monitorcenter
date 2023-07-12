package com.netmarch.monitorcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.mapper.CacheMapper;
import com.netmarch.monitorcenter.service.CacheService;
import com.netmarch.monitorcenter.service.snmp.Pair;
import com.netmarch.monitorcenter.service.snmp.TimeScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@Configuration
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CacheMapper cacheMapper;


    @Value("${snmp.job.cronExpr}")
    String cronExpression;
    /***
     * 图表数据时间段
     */
    @Value("${redis.chart.data.period}")
    String chartDtaPeriod;


    //先查出所有节点(截止目前主要是linux服务器)
    ConcurrentSkipListSet<SnmpNode> nodes = new ConcurrentSkipListSet<>();

    ExecutorService executor = Executors.newCachedThreadPool();

    /***
     * 保存snmp采集过程中的运行时状态信息
     */
    private ConcurrentHashMap<String,Object> snapShot = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        LinkedList<SnmpNode> _nodes = cacheMapper.queryNode(null);

        for (SnmpNode node:_nodes)
        {
           nodes.add(node);
        }
        Pair<Timestamp, Timestamp> scope = TimeScope.computeDate(chartDtaPeriod);
    }



    @Override
    public Set<SnmpNode> getNodes()
    {
        return nodes;
    }

    @Override
    public SnmpIfCard getCachedIfCard(Long nodeId)
    {
        SnmpIfCard cachedIfCard = getCachedKV(CacheService.CACHED_IFCARD,
                (SnmpIfCard ifCard)->
                        ifCard.getNodeId().equals(nodeId)
                ,SnmpIfCard.class);
        if(cachedIfCard != null)
        {
            return cachedIfCard;
        }
        return null;
    }


    @Override
    public Map<Object,Object> getHashMap(String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public List<String> getCachedList(String key)
    {
        //TODO:size通过cron表达式推算出
        Long size = getCachedListSize(key);
        Long end = size - 1;
        return getCachedListByRange(key,0L,end);
    }


    /***
     * 数据存储方式  <nodeId,{SnmpMemory|SnmpIfCard|SnmpFileSystem-json}>
     * @param nodeId
     * @param list
     */
    void addKVCacheAsync(Long nodeId, List<ReadNodeIdable> list){
        if(CollectionUtils.isNotEmpty(list)) {
            ReadNodeIdable firstElem = list.get(0);
            String key = getCacheKey(firstElem);
            String json = toJsonString(list);
            addNameValueCacheAsync(key,json);
        }
    }

    void addKVCacheAsync(String key, List list)
    {
        if(CollectionUtils.isNotEmpty(list)) {
            addNameValueCacheAsync(key,toJsonString(list));
        }
    }


    /**
     * 使用hash数据结构存储缓存
     * @param key
     * @param field
     * @param value actl it's a json
     */
    @Override
    public void addInternal(String key, String field, String value/*boolean sort?*/)
    {
        redisTemplate.opsForHash().putIfAbsent(key,field,value);
    }

    @Override
    public void addInternal(String key, String field, String value, boolean overwrite)
    {
        if(overwrite)
        {
            removeCache(key,field);
        }
        addInternal(key,field,value);
    }

    void removeCache(String key,String field)
    {
        redisTemplate.opsForHash().delete(key,field);
    }

    boolean existCache(String key,String field)
    {
        return redisTemplate.opsForHash().hasKey(key,field);
    }


    /***
     * 将最近一段时间采集的数据存档到缓存,以后采集进来的数据添加到队尾(tail)，队头(head)数据出列
     * @param nodeId 节点id
     * @param list
     */
    void  addSnapshot(Long nodeId,List list)
    {
        if(CollectionUtils.isNotEmpty(list)) {

            //按数据入库时间编队(模拟队列) 存储缓存


            Object firstElem = list.get(0);

            String key = getSnapshotKey(firstElem);

            //启动时只加载最新24小时，删除最近24小时的数据
            //redisTemplate.opsForZSet().removeRange(key,0,24 * 3600 / 30);
            Long count = getCachedListSize(key);
            redisTemplate.opsForList().trim(key,0,count-1);


            for (Object target:list)
            {
                enQueue(key,target);
            }
        }
    }

    private void startScheduler(){

        String periodChar = chartDtaPeriod.substring(chartDtaPeriod.length()-1);
        Integer n = Integer.valueOf(chartDtaPeriod.substring(0,chartDtaPeriod.length()-1));

        long period = 0;

        //hour 小时
        if("h".equals(periodChar))
        {
            period = n * 3600 * 1000;
        }

        //day 天
        if("d".equals(periodChar))
        {
            period = n * 24 * 3600 * 1000;
        }

        //week 周
        if("w".equals(periodChar))
        {
            period = n * 7 * 24 * 3600 * 1000;
        }

        HashMap<String, Object> condition = new HashMap<>(2);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC+8"));
        LocalDateTime startTime = now.plusSeconds( - period/1000), endTime = now;

        condition.put("startTime",startTime);
        condition.put("endTime",endTime);

//        //查出内存条信息
//        List<SnmpMemory> memories = cacheMapper.queryMemory(null);
//
//        addKVCacheAsync(CACHED_MEM,memories);
//
//        //查出网卡信息
//        List<SnmpIfCard> ifCards = cacheMapper.queryIfCard(null);
//
//        addKVCacheAsync(CACHED_IFCARD,ifCards);
//
//        //查出文件系统
//        List<SnmpFileSystem> fileSystems = cacheMapper.queryFileSystem(null);
//        addKVCacheAsync(CACHED_FILESYSTEM,fileSystems);
//
//
//        //TODO 查出文件系统信息
//
//        List<SnmpMemoryUsage> recentlyMemUsages = cacheMapper.queryMemoryUsage(condition);
//
//
//        List<SnmpCPUUsage> recentlyCpuUsage = cacheMapper.queryCPUUsage(condition);
//
//        List<SnmpLoad> recentlyLoad = cacheMapper.queryLoad(condition);
//
//        List<SnmpIfCardTraffic> recentlyTraffics = cacheMapper.queryIfCardTraffic(condition);
//
//        List<SnmpFileSystemUsage> recentlyFsUsage  = cacheMapper.queryFileSystemUsage(condition);
//
//
//
//        for (SnmpNode node:nodes) {
//
//            Long nodeId = node.getId();
//
//            redisTemplate.opsForHash().put("snmp.cached.node",nodeId.toString(),toJsonString(node));
//
//            //================固件信息、操作系统预装信息==========================
//            //筛选出该节点下的内存信息放到缓存中
//            List<SnmpMemory> memOfNode = memories.stream()
//                    .filter(memory -> memory.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addKVCacheAsync(nodeId,memOfNode);
//
//            List<SnmpIfCard> ifCardOfNode =ifCards.stream()
//                    .filter(ifCard -> ifCard.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addKVCacheAsync(nodeId,ifCardOfNode);
//
//            //================采集来的数据信息=================================
//
//
//            List<SnmpMemoryUsage> memUsageOfNode = recentlyMemUsages.stream()
//                    .filter(usage -> usage.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addSnapshot(nodeId,memUsageOfNode);
//
//            List<SnmpIfCardTraffic> trafficOfNode = recentlyTraffics.stream()
//                    .filter(traffic->traffic.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addSnapshot(nodeId,trafficOfNode);
//
//            List<SnmpLoad> loadOfNode = recentlyLoad.stream()
//                    .filter(load->load.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addSnapshot(nodeId,loadOfNode);
//
//            List<SnmpCPUUsage> cpuUsageOfNode = recentlyCpuUsage.stream()
//                    .filter(usage->usage.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addSnapshot(nodeId,cpuUsageOfNode);
//
//
//            List<SnmpFileSystemUsage> fsUsageOfNode = recentlyFsUsage.stream().filter(usage -> usage.getNodeId().equals(nodeId))
//                    .collect(Collectors.toList());
//
//            addSnapshot(nodeId,fsUsageOfNode);
//        }

    }

    @PreDestroy
    public void close()
    {
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public boolean addNameValueCacheAsync(String key, String value)
    {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            log.error("redis set操作失败:",e);
            return false;
        }
    }

    @Override
    public <T> Map<String, T> getCachedKV(String cachedId,String hashKey, Class<T> targetClass)
    {
        String json = (String)redisTemplate.opsForHash().get(cachedId, hashKey);
        return Collections.singletonMap(hashKey,JSON.parseObject(json,targetClass));
    }

    @Override
    public <T> T getCachedKVObject(String cachedId, String hashKey, Class<T> targetClass)
    {
        String json = (String)redisTemplate.opsForHash().get(cachedId, hashKey);
        return JSON.parseObject(json,targetClass);
    }

    @Override
    public <T> T loadStatus(String name, T defaultValue) {
        return null;
    }

    @Override
    public void updateListSnapshotAsync(List list)
    {
        SnapshotTask snapshotTask =new SnapshotTask(list);
        executor.submit(snapshotTask);
    }

    @Override
    public void updateListSnapshotAsync(String snapshotId, List list)
    {
        SnapshotTask snapshotTask = new SnapshotTask(snapshotId,list);
        executor.submit(snapshotTask);
    }


    @Override
    public <T> void addListCache(String cacheKey,String ipAddr, T entity)
    {
        addListCache(cacheKey,ipAddr,Arrays.asList(entity));
    }

    @Override
    public <T> void addListCache(String cacheKey,String ipAddr, String value)
    {
        ListSnapshotTask task = new ListSnapshotTask<T>(cacheKey,ipAddr,value);
        executor.submit(task);
    }

    @Override
    public <T> void addListCache(String cacheKey,String ipAddr, List<T> list)
    {
        addListCache(cacheKey,ipAddr,list,false);
    }

    @Override
    public <T> void addListCache(String cacheKey,String ipAddr, List<T> list,boolean overwrite)
    {
        ListSnapshotTask task = new ListSnapshotTask<T>(cacheKey,ipAddr,list,overwrite);
        executor.submit(task);
    }

    @Override
    public <T> void updateSnapshot(String snapShotKey,String ipAddr,List<T> list)
    {
        ListSnapshotTask task = new ListSnapshotTask<T>(snapShotKey,ipAddr,list);
        executor.submit(task);
    }




    /***
     * 计算计数器的两次增量值
     * @param last
     * @param current
     * @return
     */
    @Override
    public long[] computeDelta(long[] last, long[] current)
    {
        if(ArrayUtils.isEmpty(last)||ArrayUtils.isEmpty(current))
        {
            throw new IllegalArgumentException("计数器数据不能为空");
        }

        if(!ArrayUtils.isSameLength(last,current))
        {
            throw new IllegalArgumentException("计算器数据格式不一致");
        }

        long[] delta = new long[last.length];

        for(int i = 0 ; i < current.length; i++)
        {
            delta[i] = current[i] - last[i];
        }
        return delta;
    }

    @Override
    public <T> List<T> getCachedListFromHash(String cacheId, Class<T> targetClass, ScanOptions scanOptions) {
        List<T> list = new LinkedList<>();
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(cacheId, scanOptions);
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();

            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            log.debug("map类型缓存,键名:{},键值:{}",key,value);
            T instance = JSON.parseObject(value,targetClass);
            list.add(instance);
        }
        return list;
    }

    @Override
    public <T> List<T> getCachedListFromHash(String cacheId, Long nodeId, Class<T> targetClass, ScanOptions scanOptions) {
        List<T> list = new LinkedList<>();
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(cacheId, scanOptions);
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();

            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            log.debug("map类型缓存,键名:{},键值:{}",key,value);
            T instance = JSON.parseObject(value,targetClass);
            list.add(instance);
        }
        return list;
    }


    @Override
    public <T> List<T> getCachedListFromHash(String cacheId, Long nodeId, Class<T> targetClass)
    {
        List<T> list = new LinkedList<T>();

        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(String.format("%s:nodeId%d",cacheId,nodeId));
        if (MapUtils.isEmpty(memCached)) {
            return Collections.synchronizedList(Collections.emptyList());
        }

        for (Map.Entry<Object, Object> entry : memCached.entrySet()) {
            String key = (String) entry.getKey();
            String json = (String) entry.getValue();
            T instance = JSON.parseObject(json,targetClass);
            list.add(instance);
        }
        return list;
    }

    @Override
    public void addStatus(String statusKey, String hashKey, Long nodeId,String value)
    {
        String statusId = String.format("%s:nodeId%d", statusKey,nodeId);
        addKVCacheAsync(statusId,nodeId,Collections.singletonMap(hashKey,value),true);
    }


    @Override
    public void addStatus(String statusKey, String hashKey, String ipAddr,String value)
    {
        addKVCacheAsync(statusKey,ipAddr,Collections.singletonMap(hashKey,value),true);
    }


    @Override
    public <T> List<Map<String,T>> getCachedKV(String cachedId, Class<T> targetClass)
    {
        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(cachedId);
        if (MapUtils.isEmpty(memCached)) {
            return Collections.emptyList();
        }
        List<Map<String,T>> list = new LinkedList<Map<String,T>>();

        for(Map.Entry<Object,Object> entry:memCached.entrySet())
        {
            String key = (String)entry.getKey();
            String json = (String)entry.getValue();
            list.add(Collections.singletonMap(key,JSON.parseObject(json,targetClass)));
        }
        return list;
    }

    @Override
    public <T> List<T> getCachedKV2(String cachedId, Class<T> targetClass)
    {
        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(cachedId);
        if (MapUtils.isEmpty(memCached)) {
            return Collections.emptyList();
        }
        List<T> list = new LinkedList<T>();

        for(Map.Entry<Object,Object> entry:memCached.entrySet())
        {
            String key = (String)entry.getKey();
            String json = (String)entry.getValue();
            list.add(JSON.parseObject(json,targetClass));
        }
        return list;
    }

    @Override
    public <T> Map<String,T>  getCachedKV(String cachedKey, String hashKey, Long nodeId, Class<T> targetClass)
    {
        String statusId = String.format("%s:nodeId%d", cachedKey,nodeId);

        List<Map<String,T>> list = getCachedKV(statusId,targetClass);
        if(CollectionUtils.isEmpty(list))
        {
            return Collections.emptyMap();
        }
        Optional<Map<String, T>> matched = list.stream().filter(map -> {
            return map.keySet().contains(hashKey);
        }).findFirst();

        if(matched.isPresent()) {
            return matched.get();
        }
        return Collections.emptyMap();
    }

    @Override
    public Long getCachedListSize(String key)
    {
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public List<String> getCachedListByRange(String key, Long start, Long end)
    {
        return redisTemplate.opsForList().range(key,start,end);
    }

    @Override
    public <T> List<T> getCachedList(List<String> jsons, Class<T> targetClass)
    {
        if(CollectionUtils.isEmpty(jsons))
        {
            return Collections.emptyList();
        }
        return jsons.stream().map(json->JSON.parseObject(json,targetClass)).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public <T> List<T> getCachedList(String snapshotId, Class<T> targetClass)
    {
        List<String> jsons = getCachedList(snapshotId);
        return getCachedList(jsons,targetClass);
    }

    @Override
    public <T> List<T> getCachedList(String snapshotKey, String ipAddr, Class<T> targetClass)
    {
        String snapshotId = String.format("%s.%s",snapshotKey,ipAddr);
        return getCachedList(snapshotId,targetClass);
    }

    @Override
    public <T> List<T> getCachedList(String snapshotKey, Long nodeId, Class<T> targetClass)
    {
        SnmpIfCard cachedIfCard = getCachedIfCard(nodeId);

        if(cachedIfCard == null)
        {
            return Collections.emptyList();
        }
        String ipAddr = cachedIfCard.getIfIpAddr();
        return getCachedList(snapshotKey,ipAddr, targetClass);
    }

    @Override
    public <T> List<T> getCachedList(String snapshotId, final Predicate<? super T> filter, Class<T> targetClass)
    {
        List<T> list = getCachedList(snapshotId, targetClass);
        if(CollectionUtils.isEmpty(list))
        {
            return Collections.emptyList();
        }
        return list.parallelStream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> getCachedList(String snapshotKey, String ipAddr, final Predicate<? super T> filter, Class<T> targetClass)
    {
        List<T> list = getCachedList(snapshotKey,ipAddr, targetClass);

        if(CollectionUtils.isEmpty(list))
        {
            return Collections.emptyList();
        }
        return list.parallelStream().filter(filter).collect(Collectors.toList());
    }


    @Override
    public <T> T getCachedKV(String cachedKey, final Predicate<? super T> filter, Class<T> targetClass)
    {
        List<T> memCached =  getCachedListFromHash(cachedKey,targetClass);
        if(CollectionUtils.isNotEmpty(memCached))
        {
            Optional<T> matched = memCached.stream().filter(filter).findFirst();
            if(matched.isPresent())
            {
                return matched.get();
            }
        }
        return null;
//        for (SnmpNode node:cachedNodes)
//        {
//            String ipAddr = node.getAddress();
//            String cachedId = String.format("%s:%s", cachedKey,ipAddr);
//            List<T> entity = getCachedKV2(cachedId, targetClass);
//            if(CollectionUtils.isNotEmpty(entity))
//            {
//                entity = entity.stream().filter(filter).collect(Collectors.toList());
//            }
//        }
    }

    @Override
    public <T> T getCachedKV2(String cachedKey, String hashKey, Long nodeId, Class<T> targetClass)
    {
        String statusId = String.format("%s:nodeId%d", cachedKey,nodeId);

        Map<String, T> memCached = getCachedKV(statusId, hashKey, nodeId, targetClass);
                Optional<Map<String, T>> matched = memCached.entrySet().stream().map(entry -> {
            String key = (String) entry.getKey();
                    String json = (String) entry.getValue();
            return Collections.singletonMap(key, JSON.parseObject(json, targetClass));

        }).filter(hashMap -> hashMap.containsKey(hashKey)).findFirst();

        if(matched.isPresent())
        {
            return matched.get().get(hashKey);
        }
        return null;
    }

    @Override
    public <T> T getStatus(String statusKey, String hashKey, Long nodeId, Class<T> targetClass)
    {
        Map<String, T> result = getCachedKV(statusKey, hashKey, nodeId, targetClass);

        if(!result.isEmpty())
        {
            Optional<T> entity = result.values().stream().findFirst();
            if(entity.isPresent())
            {
                return entity.get();
            }
        }

        return null;

//        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(statusId);
//        if (MapUtils.isNotEmpty(memCached)) {
//            return null;
//        }
//
//        Optional<Map<String, T>> matched = memCached.entrySet().stream().map(entry -> {
//            String key = (String) entry.getKey();
//            String json = (String) entry.getValue();
//            return Collections.singletonMap(key, JSON.parseObject(json, targetClass));
//
//        }).filter(hashMap -> hashMap.containsKey(hashKey)).findFirst();
//
//        if(matched.isPresent())
//        {
//            return matched.get().get(hashKey);
//        }
//        return null;
    }

    @Override
    public <T> T getStatus(String statusKey, String hashKey, String ipAddr, Class<T> targetClass)
    {
        String cachedId = String.format("%s:%s",statusKey,ipAddr);
        return getCachedKVObject(cachedId,hashKey,targetClass);
    }

    @Override
    public <T> T getStatus(String statusId, String hashKey, Class<T> targetClass)
    {
        return getCachedKVObject(statusId,hashKey,targetClass);
    }

    @Override
    public void updateStatus(String statusKey, String hashKey,Long nodeId, String value)
    {
        String statusId = String.format("%s:nodeId%d", statusKey,nodeId);
        addInternal(statusId,hashKey,value,true);
    }

    @Override
    public void updateStatus(String statusKey, String hashKey,String ipAddr, String value)
    {
        CacheKVTask task = new CacheKVTask(statusKey,ipAddr,hashKey,value,true);
        executor.submit(task);
    }



    @Override
    public <T> T getCachedObjectFromList(String cacheId, Class<T> targetClass)
    {
        List<T> list = new LinkedList<>();

        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(cacheId);

        Optional<String> matched = memCached.values().stream().map(String::valueOf).findFirst();
        if(matched.isPresent())
        {
            String json = matched.get();
            return JSON.parseObject(json,targetClass);
        }
        return null;
    }

    @Override
    public List<SnmpNode> getCachedNodes()
    {
        return getHashValues(CACHED_NODE,SnmpNode.class);
    }

    @Override
    public <T> List<T> getHashValues(String cacheId, Class<T> targetClass)
    {
        List<T> list = new LinkedList<>();

        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(cacheId);
        if(MapUtils.isEmpty(memCached))
        {
            return Collections.synchronizedList(Collections.emptyList());
        }

        for (Map.Entry<Object, Object> entry : memCached.entrySet()) {
            String key = (String) entry.getKey();
            String json = (String) entry.getValue();
            T instance = JSON.parseObject(json,targetClass);
            log.debug("list@{}查询到的数据,key:{},val:{}",cacheId,key,json);
            list.add(instance);
        }
        return list;
    }

    @Override
    public <T> List<T> getCachedListFromHash(String cacheId, Class<T> targetClass)
    {
        List<T> list = new LinkedList<>();

        String ipAddr = null;

        for(SnmpNode node : nodes) {

            ipAddr = node.getAddress();

            String _key = String.format("%s:%s",cacheId,ipAddr);

            Map<Object, Object> memCached = redisTemplate.opsForHash().entries(_key);

            if (MapUtils.isEmpty(memCached)) {
                continue;
            }

            for (Map.Entry<Object, Object> entry : memCached.entrySet()) {
                String key = (String) entry.getKey();
                String json = (String) entry.getValue();
                T instance = JSON.parseObject(json,targetClass);
                list.add(instance);
            }
        }
        return list;
    }

    public <T> List<T> getCached3(String cacheId, Class<T> targetClass)
    {
        List<T> list = new LinkedList<>();

        Map<Object, Object> memCached = redisTemplate.opsForHash().entries(cacheId);
        if(MapUtils.isEmpty(memCached))
        {
            return Collections.synchronizedList(Collections.emptyList());
        }

        for (Map.Entry<Object, Object> entry : memCached.entrySet()) {
            String key = (String) entry.getKey();
            String json = (String) entry.getValue();
            T instance = JSON.parseObject(json,targetClass);
            list.add(instance);
        }

        return list;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public String friendlyText(List data)
    {
        if(CollectionUtils.isEmpty(data)) {
            return "[]";
        }

        StringJoiner joiner = new StringJoiner(",", "[", "]");
        String body = (String)data.stream().map(entry -> toJsonString(entry)).collect(Collectors.joining(","));
        return joiner.add(body).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String friendlyText(Object target)
    {
        return toJsonString(target);
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public void addKVCacheAsync(String cacheId, Long nodeId, Map<String, String> values)
    {
        addKVCacheAsync(cacheId,nodeId,values,false);
    }

    @Override
    public void addKVCacheAsync(String cacheId, Long nodeId, Map<String, String> values, boolean overwrite)
    {
        CacheTask cacheTask = new CacheTask(cacheId,nodeId,values,overwrite);
        executor.submit(cacheTask);
    }

    @Override
    public void addKVCacheAsync(String cacheKey, String ipAddr, Map<String, String> values)
    {
        CacheKVTask task = new CacheKVTask(cacheKey,ipAddr,values,false);
        executor.submit(task);
    }

    @Override
    public void addKVCacheAsync(String cacheKey, String ipAddr, Map<String, String> values, boolean overwrite)
    {
        CacheKVTask task = new CacheKVTask(cacheKey,ipAddr,values,overwrite);
        executor.submit(task);
    }

    @Override
    public void updateKVCacheAsync(String cacheKey, String ipAddr, Map<String, String> values)
    {
        CacheKVTask task = new CacheKVTask(cacheKey,ipAddr,values,true);
        executor.submit(task);
    }




    @Override
    public <T> void addListCache(String cacheKey, List<T> list)
    {
        if(CollectionUtils.isNotEmpty(list))
        {
            list.stream().forEach(entity->enQueue(cacheKey,toJsonString(entity)));
        }
    }

    @Override
    public <T extends ReadNodeIdable> void updateListCache(String cacheKey, T entity)
    {
        String cachedId = getCacheKey(entity);

        String json = toJsonString(entity);
        redisTemplate.opsForList().remove(cachedId,0,toJsonString(entity));
        redisTemplate.opsForList().rightPush(cachedId,json);
//        if(matched.isPresent()) {
//            T target = matched.get();
//            //除了创建时间，其他全部拷贝
//            BeanUtils.copyProperties(entity,target,"createTime");
//        }
//        for (T item:list)
//        {
//            Long _id = getLongValue(item,"id");
//            if()
//        }
    }

    //public void updateCacheAsync(String cachedId)

    /***
     * 根据固件信息（内存条、网卡、CPU）、预装机信息(虚拟内存、文件系统）推断对应的缓存key
     * @param node
     * @return
     */
    public String getCacheKey(ReadNodeIdable node)
    {
        //从实体上读取其归属的节点信息nodeId
        Long nodeId = node.getNodeId();//getLongValue(target, "nodeId");

        SnmpIfCard cachedIfCard = getCachedIfCard(nodeId);

        if(cachedIfCard == null)
            throw new RuntimeException("无效的nodeId,或者网卡缓存信息无效!");

        String ipAddr = cachedIfCard.getIfIpAddr();

        if (node instanceof SnmpMemory) {
            return String.format("%s.%s", CACHED_MEM, ipAddr);
        }

        if(node instanceof SnmpIfCard)
        {
            return String.format("%s.%s", CACHED_IFCARD, ipAddr);
        }

        if(node instanceof SnmpFileSystem)
        {
            return String.format("%s.%s", CACHED_FILESYSTEM, ipAddr);
        }
        return null;
    }

    /***
     * 根据数据快照类推断对应的快照key
     * @param obj
     * @return
     */
    String getSnapshotKey(Object obj)
    {
        if(obj instanceof SnmpMemoryUsage)
        {
            return String.format("%s.%d", SNAPSHOT_MEMORY_USAGE,((SnmpMemoryUsage)obj).getNodeId());
        }

        if(obj instanceof SnmpLoad)
        {
            return String.format("%s.%d", SNAPSHOT_SERVER_LOAD,((SnmpLoad)obj).getNodeId());
        }

        if(obj instanceof SnmpCPUUsage)
        {
            return String.format("%s.%d", SNAPSHOT_CPU_USAGE,((SnmpCPUUsage)obj).getNodeId());
        }

        if(obj instanceof SnmpIfCardTraffic)
        {
            return String.format("%s.%d", SNAPSHOT_IFCARD_TRAFFIC,((SnmpIfCardTraffic)obj).getNodeId());
        }

        if(obj instanceof SnmpFileSystemUsage)
        {
            return String.format("%s.%d", SNAPSHOT_FILESYSTEM_USAGE,((SnmpFileSystemUsage)obj).getNodeId());
        }

        return null;
    }

    String getSnapshotKey(String prefix,ReadNodeIdable obj)
    {
        Long nodeId = obj.getNodeId();//getLongValue(obj,"nodeId");
        return String.format("%s.%d",prefix,nodeId);
    }

    /***
     * 目前只用到数据库主键id和nodeId，返回值都是long
     * 系统缓存多处默认使用node的id为主键,
     * 调用该方法的下游方法都会因为使用反射产生少量额外的栈开销,此处为了编程方便,人为忽略方法调用时产生的额外栈开销。
     * @param target
     * @param fieldName
     * @return
     */
    @Override
    public Long getLongValue(Object target, String fieldName)
    {
        return getFieldValue(target,fieldName,Long.class);
    }


    @Override
    public <T> T getFieldValue(Object target, String fieldName, Class<T> typeName)
    {
        try {
            Object fieldValue = FieldUtils.readField(target, fieldName, true);
            return (T)fieldValue;
        } catch (IllegalAccessException e) {
            log.error("出错:实体类{}没有{}类型的{}属性字段!",target.getClass(),typeName.getSimpleName(),fieldName);
            throw new RuntimeException(e);
        }
    }
    /***
     * 入列
     * @param key 键名
     * @param target 待转换对象
     */
    void enQueue(String key,Object target){

        //类的id属性，对应数据库主键，以数据库主键作为key放入zset(MAP)里面
        //Long score = getLongValue(target,"id");

        if(!(target instanceof String))
        {
            String json = toJsonString(target);
            Long succeed = redisTemplate.opsForList().rightPush(key, json);
            log.debug("快照数据入队成功:{} @json:{}",succeed,json);
        }else {
            String json = (String)target;
            Long succeed = redisTemplate.opsForList().rightPush(key, json);
            log.debug("快照数据入队成功:{} @json:{}",succeed,json);
        }
    }


    @Override
    public String toJsonString(Object target)
    {
        if(target instanceof List)
        {
            Object firstElem = ((List) target).get(0);
            if(firstElem instanceof  SnmpIfCardTraffic)
            {
                List<SnmpIfCardTraffic> list = (List<SnmpIfCardTraffic>) target;

                return JSON.toJSONStringWithDateFormat(list,"yyyy-MM-dd HH:mm:ss");
            }else {
                return JSON.toJSONStringWithDateFormat(target, "yyyy-MM-dd HH:mm:ss");
            }
        }else
        {
            if(target instanceof  SnmpIfCardTraffic)
            {
                SnmpIfCardTraffic obj = (SnmpIfCardTraffic) target;
                return JSON.toJSONStringWithDateFormat(obj,"yyyy-MM-dd HH:mm:ss");
            }else {
                return JSON.toJSONStringWithDateFormat(target,"yyyy-MM-dd HH:mm:ss");
            }
        }
    }


    //出列
    void deQueue(String key)
    {
        String head = redisTemplate.opsForList().leftPop(key);
        log.debug("弹出队头元素:{}", head);
    }


    class CacheKVTask implements Runnable
    {

        String cacheKey;
        String ipAddr;
        Map<String,String> values;
        boolean overwrite;

        public CacheKVTask(String cacheKey,String ipAddr,Map<String,String> values)
        {
            this.cacheKey = cacheKey;
            this.ipAddr = ipAddr;
            this.values = values;
        }

        public CacheKVTask(String cacheKey,String ipAddr,String field,String value)
        {
            this.cacheKey = cacheKey;
            this.ipAddr = ipAddr;
            this.values = Collections.singletonMap(field,value);
        }

        public CacheKVTask(String cacheKey,String ipAddr,String field,String value,boolean overwrite)
        {
            this(cacheKey,ipAddr,field,value);
            this.overwrite = overwrite;
        }

        public CacheKVTask(String cacheKey,String ipAddr,Map<String,String> values,boolean overwrite)
        {
            this(cacheKey,ipAddr,values);
            this.overwrite = overwrite;
        }

        @Override
        public void run() {
            String field = null;
            String json = null;

            if(MapUtils.isNotEmpty(values)) {
                int effected = 0;

                for (Map.Entry<String, String> entry : values.entrySet()) {
                    field = entry.getKey();
                    json = entry.getValue();

                    String cacheId = String.format("%s:%s",cacheKey,ipAddr);

                    //当需要覆盖/删除存在的键
                    if (overwrite && existCache(cacheId, field)) {
                        removeCache(cacheId, field);
                    }
                    addInternal(cacheId, field, json);
                    log.debug("更新缓存,key:{}, value:{}",field,json);
                    effected++;
                }
                log.debug("更新{}条缓存记录", effected);
            }
        }
    }

    class CacheTask implements Runnable
    {

        String cacheId;
        @Deprecated
        Long nodeId;
        String ipAddr;
        Map<String,String> values;
        boolean overwrite;
        List data;

        @Deprecated
        public CacheTask(String cacheId, List data) {
            this.cacheId = cacheId;
            this.data = data;
        }

        @Deprecated
        public CacheTask(String cacheId, Long nodeId, Map<String, String> values) {
            this.cacheId = cacheId;
            this.nodeId = nodeId;
            this.values = values;
        }

        @Deprecated
        public CacheTask(String cacheId, Long nodeId, Map<String, String> values,boolean overwrite) {
            this.cacheId = cacheId;
            this.nodeId = nodeId;
            this.values = values;
            this.overwrite = overwrite;
        }

        public CacheTask(String cacheId, String ipAddr, Map<String, String> values,boolean overwrite) {
            this.cacheId = cacheId;
            this.nodeId = nodeId;
            this.values = values;
            this.overwrite = overwrite;
        }

//
//        @Override
//        public Integer call(){
//
//            String key = String.format("%s:node%d",nodeId);
//            String field = null;
//            String json = null;
//
//            int effected = 0;
//            for (Map.Entry<String,String> entry : values.entrySet())
//            {
//                field = entry.getKey();
//                json = entry.getValue();
//                addInternal(key,field,json);
//                effected++;
//            }
//            log.debug("更新{}条缓存记录",effected);
//            return effected;
//        }


        @Override
        public void run() {
            String field = null;
            String json = null;

            if(MapUtils.isNotEmpty(values)) {
                int effected = 0;

                for (Map.Entry<String, String> entry : values.entrySet()) {
                    field = entry.getKey();
                    json = entry.getValue();

                    //当需要覆盖/删除存在的键
                    if (overwrite && existCache(cacheId, field)) {
                        removeCache(cacheId, field);
                    }
                    addInternal(cacheId, field, json);
                    effected++;
                }
                log.debug("更新{}条缓存记录", effected);
            }
        }
    }


    class ListSnapshotTask<T> implements Runnable
    {
        String snapshotKey;
        String ipAddr;
        List<T> data;
        boolean overwrite;
        /**
         * 默认工作模式(保留近期的数据)
         */
        boolean keepRecentlySnapshotInMem = true;

        public ListSnapshotTask(String snapshotKey,String ipAddr,List<T> data)
        {
            this.snapshotKey = snapshotKey;
            this.ipAddr = ipAddr;
            this.data = data;
            keepRecentlySnapshotInMem = true;
        }


        public ListSnapshotTask(String snapshotKey,String ipAddr,String value)
        {
            this(snapshotKey,ipAddr, (List<T>) Arrays.asList(value));
        }

        public ListSnapshotTask(String snapshotKey,String ipAddr,List<T> data,boolean overwrite)
        {
            this.snapshotKey = snapshotKey;
            this.ipAddr = ipAddr;
            this.data = data;
            this.overwrite = overwrite;

            if(overwrite)
            {
                String snapshotId = String.format("%s.%s",snapshotKey,ipAddr);
                redisTemplate.delete(snapshotId);
            }
        }

        @Override
        public void run() {

            String snapshotId = String.format("%s.%s",snapshotKey,ipAddr);

            for(T target:data)
            {
                //在队尾插入一个元素
                enQueue(snapshotId,target);

                if(keepRecentlySnapshotInMem) {

                    if(hasExpired(snapshotId,target))
                    {
                        deQueue(snapshotId);
                    }
                }else
                {
                    //队头元素出列
                    deQueue(snapshotId);
                }
            }
        }

        /***
         * 判断快照数据是否过期
         * @param snapshotId
         * @param target
         * @return
         */
        boolean hasExpired(String snapshotId, T target)
        {
            Long size = getCachedListSize(snapshotId);
            if (size == 0)
            {
                return false;
            }else
            {
                List<String> jsons = getCachedListByRange(snapshotId,0L,0L);
                //LinkedList top 即队列头
                String top = jsons.get(0);
                Object instance = JSON.parseObject(top, target.getClass());

                LocalDateTime createTime = getFieldValue(instance,"createTime",LocalDateTime.class);

                LocalDateTime now = LocalDateTime.now(ZoneId.of("+8"));

                Duration duration = Duration.between(createTime,now);

                //如果是24小时之前的数据,则该数据已经过期
                return duration.toHours() >= 24;
            }
        }
    }

    class SnapshotTask implements Runnable
    {
        String snapshotId;
        List<ReadNodeIdable> data;

        public SnapshotTask(List data)
        {
            this.data = data;
        }

        public SnapshotTask(String snapshotId, List data) {
            this.snapshotId = snapshotId;
            this.data = data;
        }

        @Override
        public void run(){

            for(ReadNodeIdable target:data)
            {
                String key;

                if(snapshotId != null){
                    key = getSnapshotKey(snapshotId,target);
                }else
                {
                    key = getSnapshotKey(target);
                }
                //在队尾插入一个元素
                enQueue(key,target);
                //队头元素出列
                deQueue(key);
            }


            //LocalDateTime now = LocalDateTime.now();
            //TODO:如果程序应该中断呢？，以下代码
//                Date nextTime = getNextFireTime(cronExpression);
//
//                Instant instant = nextTime.toInstant();
//                ZoneId zoneId = ZoneId.systemDefault();
//                LocalDateTime nextLocalDateTime = instant.atZone(zoneId).toLocalDateTime();
//
//                //两次执行时间差n毫秒
//                long delta = Duration.between(now, nextLocalDateTime).toMillis();
//
//
//                //2、判断队头的数据是否超过设定的数据有效期
//                String periodChar = chartDtaPeriod.substring(chartDtaPeriod.length()-1);
//                Integer n = Integer.valueOf(chartDtaPeriod.substring(0,chartDtaPeriod.length()-1));
//
//                long period = 0;
//
//                //hour 小时
//                if("h".equals(periodChar))
//                {
//                    period = n * 3600 * 1000;
//                }
//
//                //day 天
//                if("d".equals(periodChar))
//                {
//                    period = n * 24 * 3600 * 1000;
//                }
//
//                //week 周
//                if("w".equals(periodChar))
//                {
//                    period = n * 7 * 24 * 3600 * 1000;
//                }
//
//                //每个周期里有多少分数据快照
//                long count = period / delta;
        }
    }
}
