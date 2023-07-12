package com.netmarch.monitorcenter.service;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.Logs;
import com.netmarch.monitorcenter.query.LogsQuery;

/**
 * @ClassName LogsService
 * @Description 操作日志服务
 * @Author 王顶奎
 * @Date 2018/12/1216:47
 * @Version 1.0
 **/
public interface LogsService {

    /**
     * 查询日志
     * @param logsQuery
     * @return
     */
    public Page<Logs> query(LogsQuery logsQuery);

    /**
     * 操作日志保存
     * @param logs
     * @throws Exception
     */
    public void save(Logs logs);

    /**
     * 查询
     * @param 根据ID查询
     * @throws Exception
     */
    public Logs queryById(Long id);


}
