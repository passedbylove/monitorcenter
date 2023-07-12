package com.netmarch.monitorcenter.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.Logs;
import com.netmarch.monitorcenter.bean.PagesStatic;
import com.netmarch.monitorcenter.config.db.DatabaseContextHolder;
import com.netmarch.monitorcenter.config.db.DatabaseType;
import com.netmarch.monitorcenter.mapper.LogsMapper;
import com.netmarch.monitorcenter.query.LogsQuery;
import com.netmarch.monitorcenter.service.LogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName LogsService
 * @Description 操作日志实现方法
 * @Author 王顶奎
 * @Date 2018/12/1216:51
 * @Version 1.0
 **/
@Service("logsService")
public class LogsServiceImpl implements LogsService {
    private final static Logger logger = LoggerFactory.getLogger(LogsServiceImpl.class);

    @Autowired
    LogsMapper logsMapper;

    @Override
    public Page<Logs> query(LogsQuery logsQuery) {
        DatabaseContextHolder.close();
        DatabaseContextHolder.setDatabaseType(DatabaseType.ONEDB);
        PageHelper.startPage(logsQuery.getPageNo() == null ? 1 : logsQuery.getPageNo(), PagesStatic.PAGES_SIZE);

        return logsMapper.logsQuery(logsQuery);
    }

    @Override
    public void save(Logs logs) {
        logsMapper.logsSave(logs);
    }

    @Override
    public Logs queryById(Long id) {
        return  logsMapper.queryById(id);
    }
}
