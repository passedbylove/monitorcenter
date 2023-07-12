package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
public class SnmpLoad implements ReadNodeIdable{

    private Long id;

    private Long nodeId;
    /***
     * 1分钟负载
     */
    private Float load1;
    /***
     * 5分钟负载
     */
    private Float load5;
    /***
     * 15分钟负载
     */
    private Float load15;
    /***
     * 采集时间(北京时间)
     */
    private LocalDateTime createTime =LocalDateTime.now(ZoneId.of("UTC+8"));

    /***
     * 冗余字段:插入数据时候的时间戳,方便根据时间排序用
     */
    private long stamp;

    public SnmpLoad(Long nodeId, Float load1, Float load5, Float load15) {
        this.nodeId = nodeId;
        this.load1 = load1;
        this.load5 = load5;
        this.load15 = load15;
    }
}
