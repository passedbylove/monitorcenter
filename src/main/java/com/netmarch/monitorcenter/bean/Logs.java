package com.netmarch.monitorcenter.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName Logs
 * @Description 操作日志实体
 * @Author 王顶奎
 * @Date 2018/12/1216:40
 * @Version 1.0
 **/
@Data
public class Logs implements Serializable {
    private long id;
    private String module;
    private String action;
    private String result;
    private String detail;
    private Date executeTime;
    private String ip;

}
