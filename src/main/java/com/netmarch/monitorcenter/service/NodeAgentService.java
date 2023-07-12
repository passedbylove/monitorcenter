package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.*;
import jnetman.snmp.SnmpClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
 * 包含host resources(rfc2790),rfc1213,ucd相关mib服务
 */
public interface NodeAgentService extends MibService{


    //============================固件、出厂设置、预装机信息==============================

    /***
     * 查询所有网卡信息
     * @return
     */
    LinkedList<SnmpIfCard> queryAllIfCards();

    LinkedList<SnmpNode> queryAllNode();

    /***
     * 保存网卡信息
     * @param theIfCards
     * @return
     */
    int saveIfCards(List<SnmpIfCard> theIfCards);

    long saveIfCard(SnmpIfCard ifCard);

    /***
     * 更新网卡信息
     * @param theIfCards
     * @return
     */

    int updateIfCards(List<SnmpIfCard> theIfCards);


    int updateIfCard(SnmpIfCard ifCard);

    /***
     * 查询节点下的内存信息(含虚拟内存)
     * @param nodeId
     * @return
     */
    SnmpMemory queryMemory(Long nodeId);

    /***
     * 查询内存信息(含虚拟内存)
     * @return
     */
    LinkedList<SnmpMemory> queryMemories();

    /***
     * 保存内存信息
     * @param memories
     * @return
     */
    int saveMemory(List<SnmpMemory> memories);

    long saveMemory(SnmpMemory memory);

    /***
     * 更新内存信息
     * @param memory
     * @return
     */
    int updateMemory(SnmpMemory memory);


    int updateMemory(List<SnmpMemory> memories);

    /***
     * 保存文件系统信息(包括Swap分区)
     * @param fileSystems
     * @return
     */
    int saveFileSystem(List<SnmpFileSystem> fileSystems);

    long saveFileSystem(SnmpFileSystem fileSystem);

    /***
     * 更新文件系统信息
     * @param fileSystems
     * @return
     */
    int updateFileSystem(List<SnmpFileSystem> fileSystems);

    /***
     * 查询文件系统信息
     * @return
     */
    LinkedList<SnmpFileSystem> queryFileSystems();


    /***
     * 根据主键查询
     * @param id
     * @return
     */
    SnmpFileSystem getFileSystemById(Long id);

    /***
     * 查询节点下的文件系统信息
     * @param nodeId
     * @return
     */
    LinkedList<SnmpFileSystem> queryFileSystem(Long nodeId);



    //===============================计数器、实时信息====================================

    /***
     * 保存负载信息
     * @param laLoad
     * @return
     */
    int saveLoad(List<SnmpLoad> laLoad);

    int saveLoad(SnmpLoad laLoad);

    /***
     * 查询节点下的负载信息(1分钟、5分钟、15分钟)
     * @param nodeId
     * @return
     */
    LinkedList<SnmpLoad> queryLoad(long nodeId);

    int saveMemoryUsage(SnmpMemoryUsage memoryUsage);

    /***
     * 保存内存使用量信息
     * @param memoryUsages
     * @return
     */
    int saveMemoryUsage(List<SnmpMemoryUsage> memoryUsages);

    /***
     * 查询节点下的内存使用率信息
     * @param nodeId
     * @return
     */
    LinkedList<SnmpMemoryUsage> queryMemoryUsageByNode(Long nodeId);

    /***
     * 根据内存主键查询使用率
     * @param memId
     * @return
     */
    LinkedList<SnmpMemoryUsage> queryMemoryUsageByMemId(Long memId);


    /***
     * 保存cpu使用率信息
     * @param cpuUsages
     * @return
     */
    int saveCpuUsage(List<SnmpCPUUsage> cpuUsages);

    int saveCpuUsage(SnmpCPUUsage cpuUsage);

    /***
     * 查询节点下的cpu使用率信息
     * @param nodeId
     * @return
     */
    LinkedList<SnmpCPUUsage> queryCPUUsage(Long nodeId);


    int saveIfCardTraffics(List<SnmpIfCardTraffic> traffics);

    /***
     * 保存网口流量信息
     * @param traffics
     * @return
     */
    int saveIfCardTraffic(List<SnmpIfCardTraffic> traffics);

    /***
     * 查询节点下的网口的流量信息
     * @param nodeId
     * @return
     */
    LinkedList<SnmpIfCardTraffic> queryTraffic(Long nodeId);


    /***
     * 保存文件系统使用率信息
     */
    int saveFileSystemUsage(List<SnmpFileSystemUsage> fileSystemUsages);

    /***
     * 查询节点下的文件系统使用信息
     * @param nodeId
     * @return
     */
    LinkedList<SnmpFileSystemUsage> queryFileSystemUsageByNode(Long nodeId);

    /***
     * 根据文件系统挂载节点主键查询使用率
     * @param fsId
     * @return
     */
    LinkedList<SnmpFileSystemUsage> queryFileSystemUsageByFsId(Long fsId);



    //========================计划任务状态维持=========================================

    void saveJobStatus(Map<String,Object> state);

    Map<String,Object> loadJobStatus();


    SnmpClient getSnmpClient(String address);

}
