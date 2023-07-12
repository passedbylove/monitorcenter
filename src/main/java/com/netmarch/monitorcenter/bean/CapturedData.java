package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/***
 * 抓取来各种指标数据的收纳器(container)
 */
@Data
@NoArgsConstructor
public class CapturedData {

    Long nodeId;
    String ifIpAddr;
    SnmpMemoryUsage memoryUsage;
    List<SnmpIfCardTraffic> traffics;
    List<SnmpFileSystemUsage> fsUsage;
    SnmpLoad load;
    SnmpCPUUsage cpuUsage;

    public CapturedData(Long nodeId, String ifIpAddr) {
        this.nodeId = nodeId;
        this.ifIpAddr = ifIpAddr;
    }


    public boolean isNotEmpty()
    {
        return (
                memoryUsage!=null
                        &&load != null
                        && cpuUsage != null
                &&CollectionUtils.isNotEmpty(traffics)
                && CollectionUtils.isNotEmpty(fsUsage)
        );
    }
}
