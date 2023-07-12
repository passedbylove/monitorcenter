package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SnmpNode implements Comparable{
    private Long id;
    /***
     * 主机IP、hostname或者域名
     */
    private String address;
    /***
     * 系统名称
     */
    private String sysName;
    /***
     * 系统描述
     */
    private String sysDescr;
    /***
     * 系统为主(次要字段)
     */
    private String sysLocation;
    /***
     * 系统联系人
     */
    private String sysContact;


    @Override
    public int compareTo(Object o) {
        SnmpNode target = (SnmpNode)o;
        return (int)(this.id.longValue()-target.getId().longValue());
    }
}
