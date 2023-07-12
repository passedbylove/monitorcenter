package com.netmarch.monitorcenter.bean;

import lombok.Data;

/***
 * snmp执行任务的状态记录
 */
@Data
public class SnmpJobState {
    private Long id;

    /***
     * 键名
     */
    private String name;

    /***
     * 键值
     */
    private Object value;

}
