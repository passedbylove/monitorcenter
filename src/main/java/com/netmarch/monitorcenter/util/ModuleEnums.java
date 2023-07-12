package com.netmarch.monitorcenter.util;

/**
 * @ClassName EnumsUtil
 * @Description TODO
 * @Author 王顶奎
 * @Date 2018/12/1316:08
 * @Version 1.0
 **/
public enum  ModuleEnums implements IEnum {
    COMMONCONTROLLER("CommonController", "公共模块"),
    GROUPCONTROLLER("GroupController", "分组管理模块"),
    SERVEREXCEPTIONCONTROLLER("ServerExceptionController", "服务器异常报警模块"),
    SERVERINFOCONTROLLER("ServerInfoController", "服务器配置模块"),
    SERVERPERFORMANCECONTROLLER("ServerPerformanceController", "服务器性能"),
    SERVICEGZCONTROLLER("ServiceGzController", "安装包管理模块"),
    SERVERDEPLOYCONTROLLER("ServerDeployController", "服务器部署发布模块"),
    SERVERLOGSCONTROLLER("ServerLogsController", "服务日志管理"),
    SERVERMONITORCONTROLLER("ServerMonitorController", "服务监控"),
    SERVERMANAGECONTROLLER("ServerManageController", "服务管理"),
    SERVERSCRIPTCONTROLLER("ServerScriptController", "脚本管理模块"),
    LOGSCONTROLLER("LogsController", "操作日志模块");


    private String value;
    private String description;

    ModuleEnums(String value, String description) {
        this.value = value;
        this.description = description;
    }


    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
