package com.netmarch.monitorcenter.query;

import com.netmarch.monitorcenter.bean.PagesStatic;

import java.io.Serializable;

/**
 * @ClassName LogsQuery
 * @Description 操作日志查询条件
 * @Author ZGD
 * @Date 2018/12/1217:25
 * @Version 1.0
 **/
public class LogsQuery  extends PagesStatic implements Serializable {

    /*开始时间*/
    public String startTime;

    /*结束时间*/
    public String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
