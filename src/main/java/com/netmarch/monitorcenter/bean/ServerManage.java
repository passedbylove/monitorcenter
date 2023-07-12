package com.netmarch.monitorcenter.bean;

import lombok.Data;

/**
 * @Author:xieqiang
 * @Date:2018/12/29
 */
@Data
public class ServerManage {
    private String deployUrl;
    private Integer runStatus;
    private String gzUrl;
    private Integer deployPort;
    private String username;
    private String password;
    private Integer linkPort;
    private String ipAddress;
}
