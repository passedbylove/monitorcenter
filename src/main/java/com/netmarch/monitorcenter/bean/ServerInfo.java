package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Id;

/**
 * 服务器配置类
 * @Author:xieqiang
 * @Date:2018/12/12
 */
@Data
@ToString
public class ServerInfo {
    @Id
    private Integer id; //ID
    private String ipAddress;//IP地址
    private Integer linkPort;//端口
    private String userName;//用户名
    private String pwd;//密码
    private String groupCode;//分组Code
    private Integer status;//服务器状态 0:正常,1:不正常
    private String memory;//内存大小
    private String cpuName;//CPU名称
    private String cpuTotalCares;//Cpu总核数
    private String cpuMaxHz;//Cpu最高Hz
    private String sysName;//系统名称
    private String description;//描述
    private String networkInfo;//网卡名称、网卡带宽Map的Json字符串
    private Integer networkCardNum;//网卡数
}
