package com.netmarch.monitorcenter.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.mapper.ServerGzMapper;
import com.netmarch.monitorcenter.mapper.ServerInfoMapper;
import com.netmarch.monitorcenter.mapper.ServerScriptMapper;
import com.netmarch.monitorcenter.service.ServerScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerScriptServiceImpl implements ServerScriptService {

    @Autowired
    private ServerScriptMapper serverScriptMapper;

    @Autowired
    private ServerGzMapper serverGzMapper;

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Override
    public Page<ServerScript> selectByCondition(ServerScript serverScript, Integer pageNo) {
        PageHelper.startPage(pageNo == null? 1:pageNo, PagesStatic.PAGES_SIZE);
        return serverScriptMapper.selectByCondition(serverScript);
    }

    @Override
    public int deleteById(Integer id) {
        return serverScriptMapper.deleteByPrimaryKey(id);
    }

    @Override
    public ServerScript getById(Integer id) {
        return serverScriptMapper.selectByPrimaryKey(id);
    }

    @Override
    public int insert(ServerScript serverScript) {
        return serverScriptMapper.insertSelective(serverScript);

    }

    @Override
    public int update(ServerScript serverScript) {

        return serverScriptMapper.updateByPrimaryKeySelective(serverScript);
    }

    @Override
    public List<ServerScript> getAll() {

        return serverScriptMapper.selectByCondition(null);
    }

    @Override
    public List<ServerGz> getAllGz() {
        return serverGzMapper.getAllGz();
    }

    @Override
    public List<ServerInfo> getAllServer() {
        return serverInfoMapper.queryServerInfoByConditions(null);
    }
}
