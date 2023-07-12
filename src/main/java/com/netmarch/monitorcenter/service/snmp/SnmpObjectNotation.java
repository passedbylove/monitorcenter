package com.netmarch.monitorcenter.service.snmp;

import org.snmp4j.smi.OID;

/***
 * translate object identifier between dotted string and textual form(s)
 * @author daiqk
 */
public class SnmpObjectNotation {

    public static final String [] hrStorageEntryFields = new String[]
            {
                    /*文件系统索引*/
                    "hrStorageIndex",
                    /*文件系统类型*/
                    "hrStorageType",
                    /*文件系统mounted名称*/
                    "hrStorageDescr",
                    /*文件系统空间单位*/
                    "hrStorageAllocationUnits",
                    /*文件系统大小*/
                    "hrStorageSize",
                    /*文件系统已用*/
                    "hrStorageUsed"
            };
    public static final String[] ipFieldNames = new String[]{
            //IP地址
            "ifIpAddr",
            //网口索引
            "ifIndex",
            //子网掩码
            "ifIpNetMask"
    };

    public static final String [] memoryFieldNames = new String[]{
            /*/虚拟内存大小*/
            "memTotalSwap",
            /*空闲虚拟内存大小*/
            "memAvailSwap",
            /*物理内存大小*/
            "memTotalReal",
            /*空闲物理内存*/
            "memAvailReal",
            /*共享内存*/
            "memShared",
            /*内存缓冲大小*/
            "memBuffer",
            /*加载到内存中数据大小*/
            "memCached"

    };


    /***
     * 系统状态信息字段(cpu使用率等)
     */
    public static final String [] systemStatFields = new String[]
            {
                    "ssCpuRawIdle",
                    "ssCpuRawUser",
                    "ssCpuRawWait",
                    "ssCpuRawSystem"
            };


    public static final String[] ipAddrEntry = new String[]{
            //ip地址
            "ipAdEntAddr",
            //网口索引
            "ipAdEntIfIndex",
            //子网掩码
            "ipAdEntNetMask"
    };


    /***
     * 网口流量相关-RFC1213 mib中 snmp v3协议中定义了22个网口相关的参数
     */
    public static final String [] trafficFieldNames = new String[]
            {
                    "ifIndex", "ifDescr", "ifType",
                    "ifMtu", "ifSpeed", "ifPhysAddress",
                    "ifAdminStatus", "ifOperStatus", "ifLastChange",
                    "ifInOctets", "ifInUcastPkts", "ifInNUcastPkts",
                    "ifInDiscards", "ifInErrors", "ifInUnknownProtos",
                    "ifOutOctets", "ifOutUcastPkts", "ifOutNUcastPkts",
                    "ifOutDiscards", "ifOutErrors", "ifOutQLen", "ifSpecific"
            };
}
