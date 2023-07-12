package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.ReadNodeIdable;
import com.netmarch.monitorcenter.bean.SnmpIfCard;
import com.netmarch.monitorcenter.bean.SnmpNode;
import org.springframework.data.redis.core.ScanOptions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public interface CacheService{

    /***
     * 缓存键名前缀
     */
    String CACHE_BASE_PREFIX = "snmp.cached.";

    /***
     * 数据快照键名前缀
     */
    String SNAPSHOT_BASE = "snmp.snapshot.";

    String MEM_USAGE = "mem.usage";

    String LOAD = "load";

    String CPU_USAGE = "cpu.usage";

    String IFCARD_TRAFFIC = "ifcard.traffic";

    String FILESYSTEM_USAGE = "filesystem.usage";



    //=================固件信息、预装机信息缓存========================

    /***
     * 主机信息
     */
    String CACHED_NODE = CACHE_BASE_PREFIX +"node";

    /***
     * 内存
     */
    String CACHED_MEM = CACHE_BASE_PREFIX + "mem";

    /**
     * 网卡
     */
    String CACHED_IFCARD = CACHE_BASE_PREFIX + "ifcard";

    /**
     * 文件系统
     */
    String CACHED_FILESYSTEM = CACHE_BASE_PREFIX +"filesystem";


    //==========================数据快照===========================
    //快照,意为take a snapshot(capture a status) of that moment，抓取/捕获那一个时刻的（运行）状态
    /***
     * 内存使用快照数据键名
     */
    String SNAPSHOT_MEMORY_USAGE = SNAPSHOT_BASE + MEM_USAGE;

    /***
     * 负载使用快照数据键名
     */

    String SNAPSHOT_SERVER_LOAD = SNAPSHOT_BASE + LOAD;

    /***
     * cpu使用率数据快照键名
     */

    String SNAPSHOT_CPU_USAGE = SNAPSHOT_BASE + CPU_USAGE;

    /***
     * 网卡流量使用率数据快照键名
     */

    String SNAPSHOT_IFCARD_TRAFFIC = SNAPSHOT_BASE + IFCARD_TRAFFIC;

    /***
     * 文件系统使用率数据快照键名
     */
    String SNAPSHOT_FILESYSTEM_USAGE = SNAPSHOT_BASE + FILESYSTEM_USAGE;


    /***
     * cpu计数器前缀
     */
    String STATUS_CPU_COUNTER = "status.cpu.counter";

    /***
     * 网口流量计数器前缀
     */
    String STATUS_TRAFFIC_COUNTER = "status.traffic.counter";



    //采集任务运行时状态
    //String STATUS


    Set<SnmpNode> getNodes();

    List<SnmpNode> getCachedNodes();

    SnmpIfCard getCachedIfCard(Long nodeId);

    Map<Object,Object> getHashMap(String key);
    <T> List<T> getHashValues(String cacheId, Class<T> targetClass);

    List<String> getCachedList(String key);

    void addInternal(String key, String field, String value/*boolean sort?*/);
    void addInternal(String key, String field, String value, boolean overwrite);

    /**
     * @param key 键名
     *      * @param value 键值
     *      * @return
     */
    boolean addNameValueCacheAsync(String key, String value);

    <T> Map<String, T> getCachedKV(String cachedId,String hashKey, Class<T> targetClass);



    <T> List<T> getCachedListFromHash(String cacheId, Class<T> targetClass, ScanOptions scanOptions);

    <T> List<T> getCachedListFromHash(String cacheId, Long nodeId, Class<T> targetClass, ScanOptions scanOptions);

    <T> List<T> getCachedListFromHash(String cacheId, Long nodeId, Class<T> targetClass);

    <T> T getCachedObjectFromList(String cacheId, Class<T> targetClass);



    /***
     * 读取线性表数据结构的缓存
     * @param cacheId
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedListFromHash(String cacheId, Class<T> targetClass);

    /***
     * 键序列化的json存到redis
     * @param cacheId 缓存键名
     * @param nodeId 节点id
     * @param values <实体数据库主键,实体json>
     */
    void addKVCacheAsync(String cacheId, Long nodeId, Map<String, String> values);

    /***
     *
     * @param cacheId
     * @param nodeId
     * @param values
     * @param overwrite 覆盖旧的数据
     */
    void addKVCacheAsync(String cacheId, Long nodeId, Map<String, String> values, boolean overwrite);

    /***
     * 添加hash散列类型数据结构的缓存
     * @param statusKey
     * @param hashKey
     * @param nodeId
     * @param value
     */
    void addStatus(String statusKey, String hashKey, Long nodeId,String value);

    void addStatus(String statusKey, String hashKey, String ipAddr,String value);

    <T> List<Map<String,T>> getCachedKV(String cachedId, Class<T> targetClass);

    <T> List<T> getCachedKV2(String cachedId, Class<T> targetClass);

    Long getCachedListSize(String key);

    List<String> getCachedListByRange(String key, Long start, Long end);

    <T> List<T> getCachedList(List<String> jsons, Class<T> targetClass);

    /***
     * 通过数据快照唯一id查询List缓存
     * @param snapshotId
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedList(String snapshotId, Class<T> targetClass);

    /***
     * 通过ip和数据快照键查询List缓存, ip + 数据快照键名 = snapshotId
     * @param snapshotKey
     * @param ipAddr
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedList(String snapshotKey, String ipAddr, Class<T> targetClass);

    /***
     * 通过数据快照键名和节点id查询List缓存
     * @param snapshotKey
     * @param nodeId
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedList(String snapshotKey, Long nodeId, Class<T> targetClass);

    /***
     * 通过数据快照唯一id和lambda数据过滤表达式查询List缓存
     * @param snapshotId
     * @param filter
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedList(String snapshotId, final Predicate<? super T> filter, Class<T> targetClass);

    /***
     * 通过ip和数据快照键和lambda数据过滤表达式查询List缓存, ip + 数据快照键名 = snapshotId
     * @param snapshotKey
     * @param ipAddr
     * @param filter
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> List<T> getCachedList(String snapshotKey, String ipAddr, final Predicate<? super T> filter, Class<T> targetClass);




    <T> T getCachedKV(String cachedKey, final Predicate<? super T> filter, Class<T> targetClass);

    <T> Map<String,T> getCachedKV(String cachedKey, String hashKey, Long nodeId, Class<T> targetClass);

    <T> T  getCachedKV2(String cachedKey, String hashKey, Long nodeId, Class<T> targetClass);

    /***
     * 读取hash散列类型的数据结构缓存
     * @param statusKey
     * @param hashKey
     * @param nodeId
     * @param targetClass
     * @param <T>
     * @return
     */
    <T> T getStatus(String statusKey, String hashKey, Long nodeId, Class<T> targetClass);

    <T> T getStatus(String statusKey, String hashKey, String ipAddr, Class<T> targetClass);

    /***
     * 更新hash散列类型的数据结构的缓存
     * @param statusKey
     * @param hashKey
     * @param nodeId
     * @param value
     */
    void updateStatus(String statusKey, String hashKey,Long nodeId, String value);

    void updateStatus(String statusKey, String hashKey,String ipAddr, String value);

    /***
     * 获取状态值
     * @param statusId
     * @param hashKey
     * @param targetClass
     * @param <T>
     */
    <T> T getStatus(String statusId, String hashKey, Class<T> targetClass);


    <T> T getCachedKVObject(String cachedId, String hashKey, Class<T> targetClass);

    <T> T loadStatus(String name, T defaultValue);


    <T> void updateSnapshot(String snapShotKey,String ipAddr,List<T> list);


    //=======================工具方法==============================

    long[] computeDelta(long[] last, long[] current);

    void addKVCacheAsync(String cacheKey, String ipAddr, Map<String, String> values);

    void addKVCacheAsync(String cacheKey, String ipAddr, Map<String, String> values, boolean overwrite);

    void updateKVCacheAsync(String cachedIfcard, String ipAddr, Map<String, String> values);

    Long getLongValue(Object target, String fieldName);

    <T> T getFieldValue(Object target, String fieldName, Class<T> typeName);


    /***
     * json格式输出list内的对象
     * @param data
     * @return
     */
    String friendlyText(List data);

    /***
     * json形式输出单个对象
     * @param target
     * @return
     */
    String friendlyText(Object target);

    /***
     * 转成json字符串
     * @param target
     * @return
     */
    String toJsonString(Object target);

    /***
     * 向列表/队列缓存中添加数据
     * @param cacheKey
     * @param entity
     * @param <T>
     */
    <T> void addListCache(String cacheKey,String ipAddr, T entity);

    <T> void addListCache(String cacheKey,String ipAddr, String value);

    <T> void addListCache(String cacheKey,String ipAddr, List<T> list);

    <T> void addListCache(String cacheKey,String ipAddr, List<T> list,boolean overwrite);

    <T> void addListCache(String cacheKey, List<T> list);

    <T extends ReadNodeIdable> void updateListCache(String cacheKey, T entity);



    void updateListSnapshotAsync(List list);

    void updateListSnapshotAsync(String snapShotId, List list);
}
