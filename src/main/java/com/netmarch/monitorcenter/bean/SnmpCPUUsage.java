package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/***
 * cpu使用率(采集来的数据是整数类型,入库整数类型实际意义不大,入库前将其转换成浮点类型，方便统计分析显示，减少不必要的百分率计算)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SnmpCPUUsage extends SnmpCPU implements ReadNodeIdable{

    private Long id;

    private Long nodeId;

    /***
     * cpu_idle
     */
    private Long ssCpuRawIdle;

    /***
     * cpu_user
     */
    private Long ssCpuRawUser;

    /***
     * cpu_wait
     */
    private Long ssCpuRawWait;

    /***
     * cpu_sys
     */
    private Long ssCpuRawSystem;


    private Float cpuIdle = 0.0f;

    private Float cpuUser = 0.0f;

    private Float cpuWait = 0.0f;

    private Float cpuSys = 0.0f;

    /***
     * 采集时间(北京时间)
     */
    private LocalDateTime createTime =LocalDateTime.now(ZoneId.of("UTC+8"));

    /***
     * 冗余字段:插入数据时候的时间戳,方便根据时间排序用
     */
    private long stamp;

    public SnmpCPUUsage(Long nodeId, Long ssCpuRawIdle, Long ssCpuRawUser, Long ssCpuRawWait, Long ssCpuRawSystem) {
        this.nodeId = nodeId;
        this.ssCpuRawIdle = ssCpuRawIdle;
        this.ssCpuRawUser = ssCpuRawUser;
        this.ssCpuRawWait = ssCpuRawWait;
        this.ssCpuRawSystem = ssCpuRawSystem;
    }
}
