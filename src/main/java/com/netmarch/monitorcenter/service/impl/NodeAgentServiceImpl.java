package com.netmarch.monitorcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.mapper.SnmpBaseMapper;
import com.netmarch.monitorcenter.service.CacheService;
import com.netmarch.monitorcenter.service.NodeAgentService;
import com.netmarch.monitorcenter.service.snmp.Pair;
import com.netmarch.monitorcenter.service.snmp.TimeScope;
import jnetman.snmp.SnmpClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NodeAgentServiceImpl extends MibServiceImpl implements NodeAgentService {

    @Autowired
    private SnmpBaseMapper snmpBaseMapper;

    @Autowired
    CacheService cacheService;


    ThreadLocal<Boolean> cacheLoaded = new ThreadLocal<Boolean>();

    /***
     * 图表数据时间段
     */
    @Value("${redis.chart.data.period}")
    String chartDtaPeriod;

    void add2Mapping(Object target,String ipAddr,String cachedKey,boolean refresh)
    {

        if(target ==null)
        {
            return;
        }

        ConcurrentHashMap<String,String> values = null;

        //将集合批量转成json
        if(target instanceof List)
        {
            List entries = ((List) target);

            values = new ConcurrentHashMap<>(entries.size());
            values.putAll(toJsonMapping(entries));
        }else {

            //单个实体转成json封送
            values = new ConcurrentHashMap<>(1);
            String id = cacheService.getFieldValue(target,"id",Long.class).toString();
            values.put(id,cacheService.toJsonString(target));
        }

        if(MapUtils.isNotEmpty(values)) {
            cacheService.addKVCacheAsync(cachedKey, ipAddr, values,refresh);
        }
    }
    @Deprecated
    void add2Mapping(Object target,Long nodeId,String cacheId,boolean refresh)
    {
        ConcurrentHashMap<String,String> values = null;

        String cachedKey = String.format("%s:nodeId%d",cacheId,nodeId);

        //将集合批量转成json
        if(target instanceof List)
        {
            List entries = ((List) target);

            values = new ConcurrentHashMap<>(entries.size());
            values.putAll(toJsonMapping(entries));
        }else {

            //单个实体转成json封送
            values = new ConcurrentHashMap<>(1);
            String id = cacheService.getFieldValue(target,"id",Long.class).toString();
            values.put(id,cacheService.toJsonString(target));
        }

        if(MapUtils.isNotEmpty(values)) {
            cacheService.addKVCacheAsync(cachedKey, nodeId, values,refresh);
        }
    }

    /***
     * 把单个实体或者集合转成json对象、数组存到redis缓存
     * 存储结构 <实体主键id,json>
     * @param target
     * @param nodeId
     * @param cacheId
     */
    @Deprecated
    void add2Mapping(Object target,Long nodeId,String cacheId)
    {
        add2Mapping(target,nodeId,cacheId,false);
    }

    void add2Mapping(Object target,String ipAddr,String cacheKey)
    {
        add2Mapping(target,ipAddr, cacheKey,false);
    }

    void loadSnapShot(Pair<Timestamp, Timestamp> scope)
    {
        Timestamp startTime = scope.getFirst();
        Timestamp endTime = scope.getSecond();

        log.info("加载缓存及数据快照");

        Map<String, Object> condition = QueryConditionBuilder.newBuilder()
                .withStartTime(LocalDateTime.ofInstant(startTime.toInstant(),ZoneId.of("UTC+8")))
                .withEndTime(LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.of("UTC+8")))
                .build().toFilter();

        LinkedList<SnmpIfCardTraffic> traffics = snmpBaseMapper.queryIfCardTraffic(condition);

        addSnapshot(CacheService.SNAPSHOT_IFCARD_TRAFFIC,traffics);

        LinkedList<SnmpMemoryUsage> memUsages = snmpBaseMapper.queryMemoryUsage(condition);

        addSnapshot(CacheService.SNAPSHOT_MEMORY_USAGE,memUsages);

        LinkedList<SnmpLoad> loads = snmpBaseMapper.queryLoad(condition);

        addSnapshot(CacheService.SNAPSHOT_SERVER_LOAD,loads);

        LinkedList<SnmpFileSystemUsage> fsUsages = snmpBaseMapper.queryFileSystemUsage(condition);

        addSnapshot(CacheService.SNAPSHOT_FILESYSTEM_USAGE,fsUsages);

        LinkedList<SnmpCPUUsage> cpuUsages = snmpBaseMapper.queryCPUUsage(condition);

        addSnapshot(CacheService.SNAPSHOT_CPU_USAGE,cpuUsages);

    }

    <T extends ReadNodeIdable> void addSnapshot(String snapshotKey,List<T> list)
    {
        Instant start = Instant.now();
        //Map<Long, List<T>> mapping = list.parallelStream().collect(Collectors.groupingByConcurrent(entity -> cacheService.getLongValue(entity, "nodeId")));
        Map<Long, List<T>> mapping = list.parallelStream().collect(Collectors.groupingByConcurrent(entity -> entity.getNodeId()));
        Instant groupBy = Instant.now();
        log.debug("数据分组耗时:{}毫秒",Duration.between(start,groupBy).toMillis());

        for (Map.Entry<Long, List<T>> entry:mapping.entrySet())
        {
            Long nodeId = entry.getKey();
            List<T> values = entry.getValue();
            //parallelStream、groupingByConcurrent并行情况下会打乱已有的数据库排序，在此重新排序

            Instant startSort = Instant.now();
            values.sort((previous,next)->{
                    Long prev_stamp = cacheService.getLongValue(previous, "stamp");
                    Long next_stamp = cacheService.getLongValue(next, "stamp");
                    return (int)(prev_stamp - next_stamp);
            });

            Instant endSort = Instant.now();
            log.debug("nodeId:{},排序耗时:{}毫秒",nodeId,Duration.between(startSort,endSort).toMillis());

            Instant cacheStart = Instant.now();
            SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
            Instant cacheEnd = Instant.now();
            log.debug("nodeId:{}查询缓存耗时:{}毫秒",nodeId,Duration.between(cacheStart,cacheEnd).toMillis());
            String ipAddr = cachedIfCard.getIfIpAddr();
            Instant addCacheStart = Instant.now();
            cacheService.addListCache(snapshotKey,ipAddr,values,true);
            Instant addCacheEnd = Instant.now();
            log.debug("nodeId:{}覆写缓存耗时:{}毫秒",nodeId,Duration.between(addCacheStart,addCacheEnd).toMillis());
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.debug("执行耗时:{}毫秒",duration.toMillis());
    }

    void loadCache()
    {
        Set<SnmpNode> nodes = cacheService.getNodes();

        nodes.stream().forEach(node -> {
            String id = String.valueOf(node.getId());
            cacheService.addInternal(CacheService.CACHED_NODE,id,cacheService.toJsonString(node));
        });

        LinkedList<SnmpIfCard> ifCards = queryAllIfCards();

        LinkedList<SnmpMemory> memories = queryMemories();

        LinkedList<SnmpFileSystem> fs = queryFileSystems();

        for (SnmpNode node:nodes)
        {
            Long nodeId = node.getId();
            String ipAddr = node.getAddress();
            //网卡缓存
            add2Mapping(filter(ifCards,nodeId),ipAddr,CacheService.CACHED_IFCARD);

            //内存缓存
            add2Mapping(filter(memories,nodeId),ipAddr,CacheService.CACHED_MEM);

            //文件系统缓存
            add2Mapping(filter(fs,nodeId),ipAddr,CacheService.CACHED_FILESYSTEM);
        }
    }


    /***
     * <数据主键id,实体json>
     * @param list
     * @param <T>
     * @return
     */
    <T> Map<String,String> toJsonMapping(List<T> list)
    {
        //TODO:如果数量级达到万级别,考了试会用并行?
        //return list.parallelStream().collect(Collectors.toConcurrentMap(id -> String.valueOf(cacheService.getLongValue(id,"id")), entity -> cacheService.toJsonString(entity)));
        return list.stream().collect(Collectors.toMap(id -> String.valueOf(cacheService.getLongValue(id,"id")), entity -> cacheService.toJsonString(entity)));
    }

    @PostConstruct
    public void init(){
        if(cacheLoaded.get() == null||!cacheLoaded.get()) {
            loadCache();
            loadSnapShot(TimeScope.computeDate(chartDtaPeriod));
            Map<String, Object> jobStatus = loadJobStatus();
            cacheLoaded.set(true);
        }

    }


    /***
     * {@inheritDoc}
     */
    @Override
    public int saveLoad(List<SnmpLoad> laLoad) {
        if(CollectionUtils.isNotEmpty(laLoad))
        {
            int effected = snmpBaseMapper.saveLoads(laLoad);
            return effected;
        }
     return 0;
    }


    @Override
    public int saveLoad(SnmpLoad laLoad)
    {
        int effected = snmpBaseMapper.saveLoads(Arrays.asList(laLoad));

        Long nodeId = laLoad.getNodeId();
        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        String ipAddr = cachedIfCard.getIfIpAddr();

        cacheService.addListCache(CacheService.SNAPSHOT_SERVER_LOAD,ipAddr,Arrays.asList(laLoad));

        if(effected > 0)
        {
            log.debug("采集到{}条负载信息:{}",effected,cacheService.friendlyText(laLoad));
        }
        return effected;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpLoad> queryLoad(long nodeId) {
        return snmpBaseMapper.queryLoad(QueryConditionBuilder.newBuilder().withNodeId(nodeId).build().toFilter());
    }



    @Override
    public int saveMemoryUsage(SnmpMemoryUsage memoryUsage) {

        int effected = snmpBaseMapper.saveMemoryUsages(Arrays.asList(memoryUsage));

        Long nodeId = memoryUsage.getNodeId();

        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        String ipAddr = cachedIfCard.getIfIpAddr();

        cacheService.addListCache(CacheService.SNAPSHOT_MEMORY_USAGE,ipAddr,cacheService.toJsonString(memoryUsage));


        if (effected > 0) {
            log.debug("新增{}个内存使用率信息@{}", effected, cacheService.friendlyText(memoryUsage));
        }
        return effected;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int saveMemoryUsage(List<SnmpMemoryUsage> memoryUsages) {

        if(CollectionUtils.isEmpty(memoryUsages)) {
            return 0;
        }

        //待更新的内存信息
        List<SnmpMemory> memForUpdate = new LinkedList<>();

        //采集来的内存信息
        List<SnmpMemory> theComingMemories = extractMemory(memoryUsages);
        /***
         * 已经缓存的内存信息
         */
        Map<Long, SnmpMemory> memMapping = filterMem(theComingMemories, true);

        //此处以采集来的内存信息为主，即使有主机离线、无法连接也不在处理
        theComingMemories.parallelStream().forEach(memory->{

            Long nodeId = memory.getNodeId();

            SnmpMemory cached = memMapping.get(nodeId);
            //如果缓存中没有这个文件系统的信息
            if(cached==null)
            {
                saveMemory(memory);
            }else
            {
                if(!cached.equals(memory))
                {
                    memory.setId(cached.getId());
                    //更新数据库
                    memForUpdate.add(memory);
                }
            }
            fillWithMemId(memoryUsages,memory);
        });

        if(CollectionUtils.isNotEmpty(memForUpdate))
        {
            updateMemory(memForUpdate);
        }

        int effected = snmpBaseMapper.saveMemoryUsages(memoryUsages);
        log.debug("采集到{}条内存使用率信息",effected);
        return effected;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpMemoryUsage> queryMemoryUsageByNode(Long nodeId) {
        return snmpBaseMapper.queryMemoryUsage(QueryConditionBuilder.newBuilder().withNodeId(nodeId).build().toFilter());
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpMemoryUsage> queryMemoryUsageByMemId(Long memId)
    {
        return snmpBaseMapper.queryMemoryUsage(QueryConditionBuilder.newBuilder().withMemId(memId).build().toFilter());
    }

    @Override
    public int saveCpuUsage(List<SnmpCPUUsage> cpuUsages) {

        if(CollectionUtils.isNotEmpty(cpuUsages))
        {
            int effected = snmpBaseMapper.saveCPUUsage(cpuUsages);
            return effected;
        }
        return 0;
    }


    @Override
    public int saveCpuUsage(SnmpCPUUsage cpuUsage)
    {
        int effected = snmpBaseMapper.saveCPUUsage(Arrays.asList(cpuUsage));
        Long nodeId = cpuUsage.getNodeId();
        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        String ipAddr = cachedIfCard.getIfIpAddr();
        cacheService.addListCache(CacheService.SNAPSHOT_CPU_USAGE,ipAddr,cpuUsage);
        return effected;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpCPUUsage> queryCPUUsage(Long nodeId) {
        return snmpBaseMapper.queryCPUUsage(QueryConditionBuilder.newBuilder().withNodeId(nodeId).build().toFilter());
    }

    @Deprecated
    public int saveIfCardTraffic(SnmpIfCardTraffic traffic)
    {
        return saveIfCardTraffic(Arrays.asList(traffic));
    }

    @Override
    public int saveIfCardTraffics(List<SnmpIfCardTraffic> traffics)
    {
        int effected = snmpBaseMapper.saveIfCardTraffic(traffics);

        for(SnmpIfCardTraffic traffic:traffics)
        {
            String ipAddr = traffic.getIfIpAddr();
            cacheService.addListCache(CacheService.SNAPSHOT_IFCARD_TRAFFIC,ipAddr,traffic);
        }
        return effected;
    }

    @Override
    @Deprecated
    public int saveIfCardTraffic(List<SnmpIfCardTraffic> traffics) {

        //遍历
        traffics.stream().forEach(traffic->{

            String ipAddr = traffic.getIfIpAddr();

            String cachedId = String.format("%s:%s",CacheService.CACHED_IFCARD,ipAddr);

            List<SnmpIfCard> list = cacheService.getCachedKV2(cachedId, SnmpIfCard.class);


            Map<String, SnmpIfCard> ifCards = new ConcurrentHashMap<>(list.size());

            if(CollectionUtils.isNotEmpty(list)) {
                ifCards.putAll(list.stream().collect(Collectors.toMap(SnmpIfCard::getIfIpAddr, Function.identity())));
            }

            SnmpIfCard memCached = ifCards.get(ipAddr);

            //从采集的信息里提取网卡信息
            SnmpIfCard ifCard = new SnmpIfCard.Builder(traffic).build();

            if(memCached == null)
            {
                long primaryKey = saveIfCard(ifCard);
                if(primaryKey>0)
                {
                    traffic.setIfCardId(primaryKey);
                }
            }else
            {
                Long ifCardId = memCached.getId();
                traffic.setIfCardId(ifCardId);
                ifCard.setId(ifCardId);

                //如果缓存里面的网卡信息与数据库不一致,更新数据库，减少不必要的磁盘IO
                if(!memCached.equals(ifCard)) {
                    updateIfCard(ifCard);
                }
            }
        });

        if(CollectionUtils.isNotEmpty(traffics)) {
            int effected = saveIfCardTraffics(traffics);
            return effected;
        }
        return 0;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpIfCardTraffic> queryTraffic(Long nodeId) {
        return snmpBaseMapper.queryIfCardTraffic(QueryConditionBuilder.newBuilder().withNodeId(nodeId).build().toFilter());
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public void saveJobStatus(Map<String, Object> state) {
        for (String key : state.keySet()) {
            Object value = state.get(key);
            if(value instanceof LocalDateTime)
            {
                LocalDateTime theDate = (LocalDateTime) value;
                String raw = theDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String localDateTime = String.format("%s:LocalDateTime",raw);
                state.put(key, localDateTime);
            }
            if(value instanceof LocalDate)
            {
                LocalDate theDate = (LocalDate) value;
                String raw = theDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String localDate = String.format("%s:LocalDate",raw);
                state.put(key, localDate);
            }
            if(value instanceof Long)
            {
                Long longValue = (Long)value;
                String raw = String.format("%s:L",longValue);
                state.put(key, raw);
            }

            if(value instanceof Integer)
            {
                Integer intValue = (Integer)value;
                String raw = String.format("%s:I", intValue);
                state.put(key, raw);
            }
        }
        snmpBaseMapper.saveJobState(state);
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> loadJobStatus() {
        List<SnmpJobState> ltStates = snmpBaseMapper.loadJobState();
        if(CollectionUtils.isNotEmpty(ltStates))
        {

            Map<String,Object> convertedStates = new ConcurrentHashMap<>(ltStates.size());

            //将list根据SnmpJobState的name属性转map
            Map<String, Object> theMap = ltStates.stream().collect(Collectors.toMap(SnmpJobState::getName, SnmpJobState::getValue));

            //根据数据库存储的值转换成对应的类实例
            theMap.forEach((key, rawState)->{
                String rawValue = (String) rawState;
                int offset = rawValue.indexOf(":");
                //if not found ':',ignore
                if(offset >= 0 ) {
                    String identifier = rawValue.substring(offset);
                    String value = rawValue.substring(0, offset);
                    convertedStates.put(key, convert(identifier, value));
                }
            });
            return convertedStates;
        }
        return Collections.synchronizedMap(Collections.emptyMap());
    }

    @Override
    public SnmpClient getSnmpClient(String address) {
        return null;
    }

    Object convert(String identifier, String value)
    {

        switch (identifier)
        {
            case "LocalDateTime":
                return LocalDateTime.parse(value,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("UTC+8"));
            case "LocalDate":
                return LocalDate.parse(value,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //means type Integer
            case "I":
                return Integer.valueOf(value);
            //means type Long
            case "L":
                return Long.valueOf(value);
        }
        return null;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpIfCard> queryAllIfCards()
    {
        return Optional.ofNullable(snmpBaseMapper.queryIfCard(null)).orElse(new LinkedList<>());
    }

    @Override
    public LinkedList<SnmpNode> queryAllNode()
    {
        return Optional.ofNullable(snmpBaseMapper.queryNode(null)).orElse(new LinkedList<>());
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public int saveIfCards(List<SnmpIfCard> ifCards)
    {
        return snmpBaseMapper.saveIfCards(ifCards);
    }

    @Override
    public long saveIfCard(SnmpIfCard ifCard)
    {
        long primaryKey = snmpBaseMapper.saveIfCard(ifCard);
        if(primaryKey>0) {
            String ipAddr = ifCard.getIfIpAddr();
            Long id = ifCard.getId();
            Map<String, String> values = Collections.singletonMap(String.valueOf(id), cacheService.toJsonString(ifCard));
            cacheService.addKVCacheAsync(CacheService.CACHED_IFCARD,ipAddr,values);
        }
        return primaryKey;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int updateIfCards(List<SnmpIfCard> ifCards)
    {
        return snmpBaseMapper.updateIfCards(ifCards);
    }

    @Override
    public int updateIfCard(SnmpIfCard ifCard)
    {
        int effected = snmpBaseMapper.updateIfCard(ifCard);
        if(effected > 0)
        {
            String ipAddr = ifCard.getIfIpAddr();
            String interfaceName = ifCard.getIfDescr();

            cacheService.updateKVCacheAsync(CacheService.CACHED_IFCARD,ipAddr,Collections.singletonMap(interfaceName,cacheService.toJsonString(ifCard)));

        }
        return effected;
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public SnmpMemory queryMemory(Long nodeId) {
        return snmpBaseMapper.getMemoryByNode(nodeId);
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpMemory> queryMemories() {
        return snmpBaseMapper.queryMemory(null);
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public int saveMemory(List<SnmpMemory> memories) {

        int effected = snmpBaseMapper.saveMemories(memories);
        if (effected > 0) {
            log.debug("新增{}个内存信息@{}", effected, cacheService.friendlyText(memories));
        }

        Map<Long, SnmpMemory> theNotExisted = filterMem(memories, false);
        updateMemoryCache(theNotExisted);

        return effected;
    }

    void updateMemoryCache(Map<Long, SnmpMemory> mapping) {
        for (Map.Entry<Long, SnmpMemory> entry: mapping.entrySet())
        {
            Long nodeId = entry.getKey();

            SnmpMemory memory = entry.getValue();

            Map<String,String> values = new HashMap<>(1);
            values.put(String.valueOf(nodeId),cacheService.toJsonString(memory));

            //更新缓存
            cacheService.addKVCacheAsync(CacheService.CACHED_MEM,nodeId,values);
        }
    }

    @Override
    public long saveMemory(SnmpMemory memory)
    {
        long primaryKey = snmpBaseMapper.saveMemory(memory);
        if (primaryKey > 0) {
            log.debug("新增内存信息@{}", cacheService.friendlyText(memory));
        }

        Long nodeId = memory.getNodeId();
        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
        String ipAddr = cachedIfCard.getIfIpAddr();

        Map<String, String> values = Collections.singletonMap(String.valueOf(memory.getId()), cacheService.toJsonString(memory));

        cacheService.addKVCacheAsync(CacheService.CACHED_MEM,ipAddr,values);
        return primaryKey;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int updateMemory(SnmpMemory memory) {

        return updateMemory(Arrays.asList(new SnmpMemory[]{memory}));
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int updateMemory(List<SnmpMemory> memories) {

        int effected = snmpBaseMapper.updateMemories(memories);
        if (effected > 0) {
            log.debug("内存信息产生变化,更新{}个内存信息@{}", effected, cacheService.friendlyText(memories));
        }
        /***
         * 查找已经存在的内存信息进行更新
         */
        Map<Long, SnmpMemory> theNotExisted = filterMem(memories, true);
        updateMemoryCache(theNotExisted);

        return effected;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int saveFileSystem(List<SnmpFileSystem> fileSystems) {

        int effected = snmpBaseMapper.saveFileSystems(fileSystems);

        //过滤出缓存中不存在的并刷新
//        Map<Long, List<SnmpFileSystem>> theNotExisted = parsed
//                .stream()
//                .filter(fileSystem -> {
//                    Long nodeId = fileSystem.getNodeId();
//                    List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
//                    return CollectionUtils.isNotEmpty(cached);
//                }).collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
        Map<Long, List<SnmpFileSystem>> theNotExisted = filter(fileSystems,false);

        updateFileSystemCache(theNotExisted);

        if (effected > 0) {
            log.debug("采集到{}个的新的文件系统信息,载点@{}", effected, cacheService.friendlyText(fileSystems));
        }
        return effected;
    }


    /***
     * 更新内存及redis缓存
     * @param map
     */
    void updateFileSystemCache(Map<Long,List<SnmpFileSystem>> map)
    {
        for (Map.Entry<Long, List<SnmpFileSystem>> entry: map.entrySet())
        {
            Long nodeId = entry.getKey();
            List<SnmpFileSystem> list = entry.getValue();

            Map<String, String> values = list.stream()
                    .collect(
                            Collectors.toMap(fs -> String.valueOf(fs.getId()),
                                    fs -> cacheService.toJsonString(fs))
                    );

            //更新缓存
            cacheService.addKVCacheAsync(CacheService.CACHED_FILESYSTEM,nodeId,values);
        }
    }

    @Override
    public long saveFileSystem(SnmpFileSystem fileSystem)
    {

        long primaryKey = snmpBaseMapper.saveFileSystem(fileSystem);

        List<SnmpNode> nodes = cacheService.getCachedNodes();

        Long nodeId = fileSystem.getNodeId();
        SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);

        String ipAddr = cachedIfCard.getIfIpAddr();
        Map<String, String> values = Collections.singletonMap(String.valueOf(fileSystem.getId()), cacheService.toJsonString(fileSystem));
        cacheService.addKVCacheAsync(CacheService.CACHED_FILESYSTEM,ipAddr,values);


        if (primaryKey > 0) {
            log.debug("新增文件系统信息@{}", cacheService.friendlyText(fileSystem));
        }
        return primaryKey;
    }

    /***
     * 从文件系统使用率中提取文件系统信息
     * @param usages
     * @return
     */
    public List<SnmpFileSystem> extractFileSystem(List<SnmpFileSystemUsage> usages)
    {
        if(CollectionUtils.isEmpty(usages))
        {
            return Collections.synchronizedList(Collections.emptyList());
        }

        return usages.parallelStream().map(usage-> new SnmpFileSystem.Builder(usage).build()).collect(Collectors.toList());
    }


    public List<SnmpIfCard> extractIfCard(List<SnmpIfCardTraffic> traffics)
    {
        if(CollectionUtils.isEmpty(traffics))
        {
            return Collections.synchronizedList(Collections.emptyList());
        }
        return traffics.parallelStream().map(traffic->new SnmpIfCard.Builder(traffic).build()).collect(Collectors.toList());
    }

    public List<SnmpMemory> extractMemory(List<SnmpMemoryUsage> usages)
    {
        if(CollectionUtils.isEmpty(usages))
        {
            return Collections.synchronizedList(Collections.emptyList());
        }
        return usages.parallelStream().map(usage -> new SnmpMemory.Builder(usage).build()).collect(Collectors.toList());
    }


    /***
     * {@inheritDoc}
     */
    @Override
    public int updateFileSystem(List<SnmpFileSystem> fileSystems) {


        //int effected =  snmpBaseMapper.updateFileSystems(parsed);

//        /***
//         * 挂载点的文件系统信息产生了变化（挂载节点的顺序、存储类型、节点存储空间大小）即刷新数据库
//         */
//        int effected = fileSystems.stream().mapToInt(fileSystem -> snmpBaseMapper.updateFileSystem(fileSystem)).sum();
//        if (effected > 0) {
//            log.debug("文件系统信息产生变化,更新{}个挂载点@{}", effected, cacheService.friendlyText(fileSystems));
//        }
//
//        //先过滤出缓存中已经存在这个节点文件系统预设信息的记录
//        Map<Long, List<SnmpFileSystem>> theExisted = fileSystems
//                .stream()
//                .filter(fileSystem -> {
//            Long nodeId = fileSystem.getNodeId();
//            List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
//            return CollectionUtils.isNotEmpty(cached);
//        }).collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
//
//        updateFileSystemCache(theExisted);
//
////        for (Map.Entry<Long, List<SnmpFileSystem>> entry: theExisted.entrySet())
////        {
////            Long nodeId = entry.getKey();
////            List<SnmpFileSystem> list = entry.getValue();
////
////            cachedFileSystems.put(nodeId,list);
////
////
////            //遍历list,将list转成<SnmpFileSystem.getId().toString,SnmpFileSystem json串>
////            Map<String, String> values = list.stream()
////                    .collect(
////                            Collectors.toConcurrentMap(fs -> String.valueOf(fs.getId()),
////                                    fs -> cacheService.toJsonString(fs))
////                    );
////
////            //更新缓存
////            cacheService.updateCacheAsync(CacheService.CACHED_FILESYSTEM,nodeId,values);
////        }
//
//        return effected;
        return 0;
    }



    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpFileSystem> queryFileSystems() {
        return snmpBaseMapper.queryFileSystem(null);
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpFileSystem> queryFileSystem(Long nodeId) {

        return snmpBaseMapper.queryFileSystem(QueryConditionBuilder
                .newBuilder()
                .withNodeId(nodeId)
                .build()
                .toFilter());
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public SnmpFileSystem getFileSystemById(Long id)
    {
        return snmpBaseMapper.getFileSystemById(id);
    }


    Map<Long,SnmpNode> toMap(List<SnmpNode> nodes)
    {
        if(CollectionUtils.isEmpty(nodes))
        {
            return Collections.synchronizedMap(Collections.emptyMap());
        }
        return nodes.stream().collect(Collectors.toConcurrentMap(SnmpNode::getId,Function.identity()));
    }

    /***
     * 将文件系统list转map
     * @param fileSystems
     * @param existedInCache 缓存中是否存在相关nodeId的文件系统缓存
     * @return
     */
    Map<Long,List<SnmpFileSystem>> filter(List<SnmpFileSystem> fileSystems,boolean existedInCache)
    {
//        if(CollectionUtils.isEmpty(fileSystems))
//        {
//            return Collections.synchronizedMap(Collections.emptyMap());
//        }
//
//        //先过滤出缓存中已经存在这个节点文件系统预设信息的记录
//        return fileSystems.stream().filter(fileSystem -> {
//            Long nodeId = fileSystem.getNodeId();
//            List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
//
//            //如果存在这个节点的文件系统缓存,则Cache不为null(isNotEmpty)
//            if(existedInCache) {
//                return CollectionUtils.isNotEmpty(cached);
//            }
//            //如果不存在这个缓存则Cache为null(isNotEmpty)
//            return CollectionUtils.isEmpty(cached);
//
//        }).collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
        return null;
    }

//    Map<Long,List<SnmpMemory>> filterMem(List<SnmpMemory> memories,boolean existedInCache)
//    {
//        if(CollectionUtils.isNotEmpty(memories))
//        {
//            return Collections.synchronizedMap(Collections.emptyMap());
//        }
//
//        return memories.stream().filter(mem->mem.getId()>0).collect(Collectors.groupingByConcurrent(SnmpMemory::getNodeId));
//    }

    /***
     * 将内存信息list转map
     * @param memories
     * @param existedInCache
     * @return
     */
    Map<Long,SnmpMemory> filterMem(List<SnmpMemory> memories,boolean existedInCache)
    {
        if(CollectionUtils.isEmpty(memories))
        {
            return Collections.synchronizedMap(Collections.emptyMap());
        }

        //先过滤出缓存中已经存在这个节点的记录
        return memories.stream().filter(mem -> {
            Long nodeId = mem.getNodeId();
            //根据节点nodeId来查找内存信息
            Optional<SnmpMemory> cached = cacheService.getCachedList(CacheService.CACHED_MEM, SnmpMemory.class)
                                            .stream().filter(curr -> curr.getNodeId().equals(nodeId)).findFirst();

            //如果存在这个缓存,则Cache不为null(isNotEmpty)
            if(existedInCache) {
                return cached.isPresent();
            }
            //如果不存在这个缓存则Cache为null(isNotEmpty)
            return !cached.isPresent();

        }).collect(Collectors.toConcurrentMap(SnmpMemory::getNodeId,Function.identity()));
    }

    /***
     * 网卡信息转Map
     * @param ifCards
     * @return
     */
    Map<Long,List<SnmpIfCard>> ifCard2Map(List<SnmpIfCard> ifCards)
    {
        if(CollectionUtils.isEmpty(ifCards))
        {
            return Collections.synchronizedMap(Collections.emptyMap());
        }
        return ifCards.stream().collect(Collectors.groupingByConcurrent(SnmpIfCard::getNodeId));
    }

    /***
     * 从结果集中根据节点id提取数据
     * @param list
     * @param nodeId
     * @param <T>
     * @return
     */
    <T> LinkedList<T> filter(LinkedList<T> list,Long nodeId)
    {
        if(CollectionUtils.isEmpty(list))
        {
            return null;
        }
        return list.parallelStream()
                .filter(instance->cacheService.getLongValue(instance,"id").equals(nodeId))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /***
     * 文件系统集合转Map
     * @param fss
     * @return
     */
    Map<Long,List<SnmpFileSystem>> fs2Map(List<SnmpFileSystem> fss)
    {
        if(CollectionUtils.isEmpty(fss)) {
            return Collections.synchronizedMap(Collections.emptyMap());
        }

        return fss.parallelStream().collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
    }


    /***
     * 内存信息转Map
     * @param memories
     * @return
     */
    Map<Long,SnmpMemory> mem2Map(List<SnmpMemory> memories)
    {
        if(CollectionUtils.isEmpty(memories)) {
            return Collections.synchronizedMap(Collections.emptyMap());
        }
        //根据节点ID分组转
        return memories.parallelStream()
                .collect(
                        Collectors.toConcurrentMap(SnmpMemory::getNodeId,Function.identity(),(exist,old)->exist)
                );
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public int saveFileSystemUsage(List<SnmpFileSystemUsage> fileSystemUsages)
    {
        int effected = 0;
        fileSystemUsages.stream()
                .collect(Collectors.groupingBy(SnmpFileSystem::getNodeId))
                .forEach((nodeId,usages)->{

                    int rows = snmpBaseMapper.saveFileSystemUsages(usages);
                    if (rows > 0) {
                        log.debug("新增{}个文件系统使用信息@{}", effected, cacheService.friendlyText(usages));
                    }

                    SnmpIfCard cachedIfCard = cacheService.getCachedIfCard(nodeId);
                    String ipAddr = cachedIfCard.getIfIpAddr();
                    cacheService.addListCache(CacheService.SNAPSHOT_FILESYSTEM_USAGE,ipAddr,usages);
                });

        return effected;
    }


//    public int saveFileSystemUsage2(List<SnmpFileSystemUsage> fileSystemUsages)
//    {
//
//        List<SnmpFileSystemUsage> filtered = fileSystemUsages
//                .stream()
//                //1、排除无效的挂载点
//                .filter(usage->{
//                            String name = usage.getHrStorageDescr();
//                            Long totalSize = usage.getHrStorageSize();
//                            String storageType = usage.getHrStorageType();
//
//                                    //挂载点名称为null
//                            return StringUtils.isNotBlank(name)
//                                    //存储空间为0则视为无效挂载
//                                    &&totalSize>0
//                                    //存储空间类型为null
//                                    &&StringUtils.isNotBlank(storageType);
//                        }
//                ).map(usage->{
//                    String oidType = usage.getHrStorageType();
//                    usage.setHrStorageType(translateStorageType(oidType));
//                    return usage;
//                })
//                .collect(Collectors.toList());
//
//        //待保存的文件系统使用率(存储处理过的数据)
//        List<SnmpFileSystemUsage> usagesForSave = new LinkedList<>();
//
//            //3、从文件系统使用率信息里剥离出文件系统基础信息
//
//        if(CollectionUtils.isNotEmpty(filtered))
//        {
//            List<SnmpFileSystem> ltFileSystem = extractFileSystem(filtered);
//
//
//            //待更新的内存信息
//            //实体机:如果机器有一天新插入一张内存条、拔掉一根内存条
//            //虚拟机:如果有一天给虚拟机扩展内存
//            List<SnmpFileSystem> fileSystemForUpdate = new LinkedList<>();
//
//            //4、文件系统：查缓存、更新缓存、刷新redis、保存文件系统信息
//            //根据节点id筛选出对应的文件系统,进行比较
//
//            //先过滤出缓存中已经存在这个节点文件系统预设信息的记录
////            Map<Long, List<SnmpFileSystem>> theExistFileSystems = ltFileSystem.stream().filter(fileSystem -> {
////                Long nodeId = fileSystem.getNodeId();
////                List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
////                return CollectionUtils.isNotEmpty(cached);
////            }).collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
//
//            //根据节点nodeId提取内存中已经缓存的文件系统信息
//            Map<Long, List<SnmpFileSystem>> memCachedFs = filter(ltFileSystem,true);
//
//            //对新采集的数据进行保存并入库、并刷新缓存
//            for (Map.Entry<Long,List<SnmpFileSystem>> entry : memCachedFs.entrySet())
//            {
//                Long nodeId = entry.getKey();
//
//                /***
//                 * 当前采集到的文件系统
//                 */
//                List<SnmpFileSystem> currentFss = entry.getValue();
//
//                List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
//
//                //TODO:如果文件系统挂载节点有变动,某一个节点被删除,则该挂载节点采集来的使用率信息，最后一条创建时间肯定低于采集周期n秒,
//                // 根据使用率信息获得文件系统id，则认为该文件系统已经不存在，从redis和数据库删除他
//
//
//                currentFss.forEach(currFs ->{
//
//                    //Comparator<SnmpFileSystem> cmpMounted = Comparator.comparing(SnmpFileSystem::getHrStorageDescr);
//                    //根据挂载节点名称去查询该节点下的缓存是否存在这个挂载点
//                    Optional<SnmpFileSystem> snapshot = cached.stream()
////                            .filter(fileSystem -> Comparator.comparing(fileSystem::getHrStorageDescr,existFileSystem::getHrStorageDescr)))
//                            .filter(fileSystem -> fileSystem.getHrStorageDescr().equalsIgnoreCase(currFs.getHrStorageDescr()))
//                            .findFirst();
//
//                    //如果存在这个挂载点
//                    if(snapshot.isPresent())
//                    {
//                        SnmpFileSystem cachedObj = snapshot.get();
//
//                        Optional<SnmpFileSystemUsage> processedUsage = fillWithFsId(filtered,cachedObj);
//
//                        //采集来的文件系统使用率添加文件系统id
//                        if(processedUsage.isPresent())
//                        {
//                            usagesForSave.add(processedUsage.get());
//                        }
//
//                        //如果采集来的文件系统信息与缓存不一致
//                        //刷新内存缓存:先踢后加
//                        if(!currFs.equals(cachedObj)) {
//                            currFs.setId(cachedObj.getId());
//                            cached.remove(cachedObj);
//                            currFs.setId(cachedObj.getId());
//                            cached.add(currFs);
//                            cachedFileSystems.put(nodeId, cached);
//                            //更新数据库
//                            fileSystemForUpdate.add(currFs);
//                        }
//                    }else
//                    {
//                        //如果缓存中不存在这个挂载点,插入数据库、更新缓存
//                        long id = saveFileSystem(currFs);
//
//                        Optional<SnmpFileSystemUsage> processedUsage = fillWithFsId(filtered, currFs);
//
//                        if(processedUsage.isPresent()) {
//                            usagesForSave.add(processedUsage.get());
//                        }
//                        cached.add(currFs);
//                        //cachedFileSystems.put(nodeId,cached);
//                        log.debug("新采集到文件系统挂载点信息:{}", currFs);
//                    }
//
//                });
//            }
//
////            if(CollectionUtils.isNotEmpty(fileSystemForUpdate)) {
////                //挂载点的文件系统信息产生了变化（挂载节点的顺序、存储类型、节点存储空间大小）即刷新数据库
////                int effected = fileSystemForUpdate.stream().mapToInt(fileSystem -> snmpBaseMapper.updateFileSystem(fileSystem)).sum();
//////                int effected = 0;
//////                for (SnmpFileSystem fileSystem:fileSystemForUpdate)
//////                {
//////                    effected +=snmpBaseMapper.updateFileSystem(fileSystem);
//////                }
//////                int effected = snmpBaseMapper.updateFileSystems(fileSystemForUpdate);
////
////                if (effected > 0) {
////                    String friendlyText = fileSystemForUpdate.stream()
////                            .reduce(new StringBuilder(), (buffer, fileSystem) -> buffer.append(fileSystem).append(','), StringBuilder::append).toString();
////                    log.debug("文件系统信息产生变化,更新{}个挂载点@{}", effected, friendlyText);
////                }
////            }
//
//            if(CollectionUtils.isNotEmpty(fileSystemForUpdate))
//            {
//                updateFileSystem(fileSystemForUpdate);
//            }
//
//            //如果是刚采集来的主机内存预设信息,根据nodeId进行分组
////            Map<Long, List<SnmpFileSystem>> theNewComingFileSystems =
////                    ltFileSystem.stream().filter(fileSystem -> {
////                Long nodeId = fileSystem.getNodeId();
////                List<SnmpFileSystem> cached = cachedFileSystems.get(nodeId);
////                return CollectionUtils.isNotEmpty(cached);
////            }).collect(Collectors.groupingByConcurrent(SnmpFileSystem::getNodeId));
//            Map<Long, List<SnmpFileSystem>> theComingFss = filter(ltFileSystem,false);
//
//            //对新采集的数据进行保存并入库、并刷新缓存
//            for (Map.Entry<Long,List<SnmpFileSystem>> entry : theComingFss.entrySet())
//            {
//                Long nodeId = entry.getKey();
//                List<SnmpFileSystem> list = entry.getValue();
//                int effected = saveFileSystem(list);
//                log.debug("采集到{}个挂载节点的文件系统信息",effected);
//                cachedFileSystems.put(nodeId,list);
//                //把新采集来的主机文件系统信息入库后的主键id更新到文件系统使用率的fsId
//                list.parallelStream().forEach(fileSystem -> fillWithFsId(filtered,fileSystem));
//            }
//
//            //5、文件系统使用率:保存、更新redis快照
//            if(CollectionUtils.isNotEmpty(usagesForSave)) {
//                int effected = snmpBaseMapper.saveFileSystemUsages(usagesForSave);
//                //cacheService.updateListSnapshotAsync(CacheService.SNAPSHOT_FILESYSTEM_USAGE, usagesForSave);
//                return effected;
//            }
//        }
//        return 0;
//    }


    /***
     * 给采集来的内存使用率填充memId字段
     * @param usages
     * @param memory
     */
    void fillWithMemId(List<SnmpMemoryUsage> usages,SnmpMemory memory)
    {
        long nodeId = memory.getNodeId();
        long memId = memory.getId();

        usages.parallelStream().filter(usage -> usage.getNodeId().equals(nodeId))
                .forEach(usage -> {
                    usage.setMemId(memId);
                });
    }
    /***
     * //从采集来的数据中根据节点nodeId和挂载点名称匹配文件系统id,SnmpFileSystemUsage.setFsId()
     * @param usages
     * @param fileSystem
     * @return
     */
    Optional<SnmpFileSystemUsage> fillWithFsId(List<SnmpFileSystemUsage> usages,SnmpFileSystem fileSystem)
    {

        Long nodeId = fileSystem.getNodeId();
        //挂载节点名称
        String mounted = fileSystem.getHrStorageDescr();
        Long fsId = fileSystem.getId();

        //从采集来的数据中根据节点nodeId和挂载点名称匹配文件系统id,SnmpFileSystemUsage.setFsId()
        return usages.stream().filter(usage ->
                usage.getNodeId().equals(nodeId)
                        && usage.getHrStorageDescr().equals(mounted)
        ).map(usage -> {
            usage.setFsId(fsId);
            return usage;
        }).findFirst();

    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpFileSystemUsage> queryFileSystemUsageByNode(Long nodeId) {

        return snmpBaseMapper.queryFileSystemUsage(QueryConditionBuilder.newBuilder().withNodeId(nodeId).build().toFilter());
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public LinkedList<SnmpFileSystemUsage> queryFileSystemUsageByFsId(Long fsId)
    {
        return snmpBaseMapper.queryFileSystemUsage(QueryConditionBuilder.newBuilder().withFsId(fsId).build().toFilter());
    }

    /***
     * 用于快速构建查询参数的map
     */
    @Data
    @NoArgsConstructor
    public static class QueryConditionBuilder
    {
        LocalDateTime startTime;
        LocalDateTime endTime;
        Long nodeId;
        Long fsId;
        Long memId;
        Long ifCardId;

        private QueryConditionBuilder(Builder builder) {
            setStartTime(builder.startTime);
            setEndTime(builder.endTime);
            setNodeId(builder.nodeId);
            setFsId(builder.fsId);
            setMemId(builder.memId);
            setIfCardId(builder.ifCardId);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static QueryConditionBuilder newEmptyBuilder() {
            return new Builder().build();
        }


        public Map<String,Object> toFilter()
        {
            ObjectMapper mapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            //https://github.com/networknt/light-4j/issues/82
            mapper.registerModule(module);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            Map<String,Object> filter = mapper.convertValue(this, Map.class);
            log.debug("查询条件:{}",JSON.toJSONString(filter));
            return filter;
        }

        public static final class Builder {
            private LocalDateTime startTime;
            private LocalDateTime endTime;
            private Long nodeId = null;
            private Long fsId = null;
            private Long memId =null;
            private Long ifCardId = null;

            private Builder() {
            }

            public Builder withStartTime(LocalDateTime startTime) {
                this.startTime = startTime;
                return this;
            }

            public Builder withEndTime(LocalDateTime endTime) {
                this.endTime = endTime;
                return this;
            }

            public Builder withNodeId(Long nodeId) {
                this.nodeId = nodeId;
                return this;
            }

            public Builder withFsId(Long fsId) {
                this.fsId = fsId;
                return this;
            }

            public Builder withMemId(Long val) {
                memId = val;
                return this;
            }

            public Builder withIfCardId(Long val) {
                ifCardId = val;
                return this;
            }

            public QueryConditionBuilder build() {
                return new QueryConditionBuilder(this);
            }
        }
    }
}
