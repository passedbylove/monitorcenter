package com.netmarch.monitorcenter.mapper;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.Logs;
import com.netmarch.monitorcenter.query.LogsQuery;

/**
 * @ClassName LogsMapper
 * @Description 操作日志映射接口
 * @Author 王顶奎
 * @Date 2018/12/1216:51
 * @Version 1.0
 **/
public interface LogsMapper {
    /**
     * 日志查询
     * @param pageNo
     * @return
     */
    Page<Logs> logsQuery(LogsQuery logsQuery);

    /**
     * 保存操作日志
     * @param logs
     */
    void logsSave(Logs logs);

    /**
     * 根据Id查询
     * @param id
     */
    Logs queryById(Long id);
}
