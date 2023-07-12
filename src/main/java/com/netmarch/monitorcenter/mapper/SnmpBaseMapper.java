package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public interface SnmpBaseMapper {
    /***
     * 节点负载 load1,load5,load15
     * @param laLoad
     * @return
     */
    int saveLoads(List<SnmpLoad> laLoad);

    LinkedList<SnmpLoad> queryLoad(Map<String, Object> condition);

    //cpu使用率
    int saveCPUUsage(List<SnmpCPUUsage> cpuUsages);

    LinkedList<SnmpCPUUsage> queryCPUUsage(Map<String, Object> condition);

    //===========================虚拟内存、内存硬件信息相关===============================

    /***
     * 根据条件筛选内存条信息
     * @param condition
     * @return
     */
    LinkedList<SnmpMemory> queryMemory(Map<String,Object> condition);

    /***
     * 根据节点id查询内存信息
     * @param nodeId
     * @return
     */
    SnmpMemory getMemoryByNode(Long nodeId);

    /***
     * 根据主键查询内存信息
     * @param id
     * @return
     */
    SnmpMemory getMemoryById(Long id);

    /***
     * 保存内存信息
     * @param memory
     * @return
     */

    long saveMemory(SnmpMemory memory);

    /***
     * 批量保存内存信息
     * @param memories
     * @return
     */
    int saveMemories(List<SnmpMemory> memories);

    /***
     * 更新内存信息
     * @param memory
     * @return
     */
    int updateMemory(SnmpMemory memory);

    /***
     * 批量更新内存信息
     * @param memories
     * @return
     */
    int updateMemories(List<SnmpMemory> memories);

    //=============================内存使用率相关=====================================

    /***
     * 保存使用率信息
     * @param memoryUsages
     * @return
     */
    int saveMemoryUsages(List<SnmpMemoryUsage> memoryUsages);

    LinkedList<SnmpMemoryUsage> queryMemoryUsage(Map<String,Object> condition);

//    /***
//     * 根据节点id查询内存使用率
//     * @param nodeId
//     * @return
//     */
//    LinkedList<SnmpMemoryUsage> queryMemoryUsageByNode(Long nodeId);
//
//    /***
//     * 根据内存主键查询使用率
//     * @param memId
//     * @return
//     */
//    LinkedList<SnmpMemoryUsage> queryMemoryUsageByMemId(Long memId);



    //===========================网卡硬件信息相关=================================

    LinkedList<SnmpIfCard> queryIfCard(Map<String,Object> condition);

    long saveIfCard(SnmpIfCard ifCard);

    int saveIfCards(List<SnmpIfCard> theIfCards);

    LinkedList<SnmpIfCard> queryIfCardByNode(Long nodeId);

    SnmpIfCard getIfCardById(Long id);

    int updateIfCard(SnmpIfCard ifCard);

    int updateIfCards(List<SnmpIfCard> theIfCards);

    //==========================网卡流量相关====================================

    int saveIfCardTraffic(List<SnmpIfCardTraffic> traffics);

    LinkedList<SnmpIfCardTraffic> queryIfCardTraffic(Map<String,Object> condition);

    LinkedList<SnmpJobState> loadJobState();

    int saveJobState(@Param("state") Map<String, Object> state);

    LinkedList<SnmpNode> queryNode(Map<String,Object> condition);


    //==========================文件系统=========================================
    LinkedList<SnmpFileSystem> queryFileSystem(Map<String,Object> condition);

    SnmpFileSystem getFileSystemById(Long id);

//    LinkedList<SnmpFileSystem> queryFileSystemByNode(Long nodeId);

    /***
     * 保存文件系统信息(返回主键id)
     * @param fileSystem
     * @return
     */
    long saveFileSystem(SnmpFileSystem fileSystem);

    int saveFileSystems(List<SnmpFileSystem> fileSystems);

    /***
     * 更新文件系统信息
     * @param fileSystem
     * @return
     */
    int updateFileSystem(SnmpFileSystem fileSystem);

    int updateFileSystems(List<SnmpFileSystem> fileSystems);

    //============================文件系统使用率====================================

    /***
     * 查询文件系统使用率
     * @param condition
     * @return
     */
    LinkedList<SnmpFileSystemUsage> queryFileSystemUsage(Map<String,Object> condition);

    /***
     * 保存文件系统使用率
     * @param fileSystemUsages
     * @return
     */
    int saveFileSystemUsages(List<SnmpFileSystemUsage> fileSystemUsages);
}
