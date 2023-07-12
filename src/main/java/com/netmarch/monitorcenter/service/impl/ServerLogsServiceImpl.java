package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.mapper.ServerLogsMapper;
import com.netmarch.monitorcenter.service.ServerLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author:xieqiang
 * @Date:2018/12/15
 */
@Service
public class ServerLogsServiceImpl implements ServerLogsService {
    @Autowired
    private ServerLogsMapper serverLogsMapper;

}
