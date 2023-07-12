package com.netmarch.monitorcenter.bean;

public class StaticObj {

    //##############################用户session的key静态变量#######################################
    /**用户SESSION-KEY*/
    public static String SESSION_KEY = "USERSSESSION";
    //##############################返回状态静态变量#######################################
    /**成功*/
    public static String R_STATUS_SUCCESS ="SUCCESS";

    /**系统模块功能SESSION-KEY*/
    public static String SESSION_MODULE = "SYSMODULE";
    /**失败*/
    public static String R_STATUS_ERROR ="ERROR";
    //##############################提示信息静态变量#######################################
    public static String R_STATUS_SUCCESS_MSG ="登陆成功！";
    public static String R_STATUS_ERROR_MSG ="登陆失败，用户名密码不存在！";
    public static String R_SQL_ERROR ="失败数据库操作异常，异常信息如下：";
    public static String INTER_ALERT_SESSION_NULL = "用户登录超时，请重新登录！";
    public static String UPDATE_MSG_SUCCESS = "操作成功！";
    public static String UPDATE_MSG_ERROR = "操作失败！";
    public static String FORM_ADD_AZBLX_SERVERGZ = "新增目录！";



    public static String SERVER_GROUP_TYPE = "FWQLX";
    public static final String SCRIPT_TYPE = "JBLX";
    public static final String SCRIPT_PATH = "/script/";
    public static final String CONTECT_GZ = "contect";
    public static final String SERVER_GZ = "server";


    public static final int IS_NOT_RUNNING = 2;
    public static final int IS_RUNNING = 1;

}
