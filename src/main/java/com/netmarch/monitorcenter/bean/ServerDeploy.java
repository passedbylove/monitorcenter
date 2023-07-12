package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author: lining
 * @Description: server_deploy
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ServerDeploy extends JSON {
    //id
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    //服务器Id
    private String serverId;
    //服务器Ip地址
    private String serverIp;
    //版本
    private String version;
    //创建时间
    private Date createTime;
    //部署时间
    private Date deployTime;
    //状态
    private Integer status;
    //安装包id
    private Integer gzId;
    //服务名称
    private String serverName;
    //服务器启动服务端口号，目前日志下载需要使用
    private Integer serverPort;
    //服务路径
    private String deployPath;
    //服务器详细信息
    @Transient
    private ServerInfo serverInfo;
    //服务安装包信息
    @Transient
    private  ServerGz serverGz;
    // 服务运行状态
    private Integer runStatus;
    //部署脚本
    private Integer deployScriptId;
    //卸载脚本
    private Integer uninstallScriptId;
    //启动脚本
    private Integer startScriptId;
    //停止脚本
    private Integer stopScriptId;
}
