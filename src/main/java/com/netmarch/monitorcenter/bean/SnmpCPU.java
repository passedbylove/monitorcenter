package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * cpu固件信息,例如主频、厂商
 */
@Data
@NoArgsConstructor
public class SnmpCPU {

    /***
     * 厂商及主频信息
     */
    private String model;
    /***
     * 内核数
     */
    private int cores;
}
