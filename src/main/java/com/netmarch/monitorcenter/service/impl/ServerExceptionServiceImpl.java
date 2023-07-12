package com.netmarch.monitorcenter.service.impl;

import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.PagesStatic;
import com.netmarch.monitorcenter.bean.ServerException;
import com.netmarch.monitorcenter.mapper.ServerExceptionMapper;
import com.netmarch.monitorcenter.service.ServerExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ServerExceptionServiceImpl implements ServerExceptionService {

    @Autowired
    private ServerExceptionMapper serverExceptionMapper;

    @Override
    public int save(ServerException serverException) {
        return serverExceptionMapper.insertSelective(serverException);
    }

    @Override
    public List<ServerException> selectAll(Integer pageNo, Map<String,Object> param) {
        PageHelper.startPage(pageNo == null ?1:pageNo, PagesStatic.PAGES_SIZE);
        return serverExceptionMapper.getAll(param);
    }

    @Override
    public int updateState() {
        return serverExceptionMapper.updateState();
    }

    @Override
    public ServerException getById(Integer id) {
        return serverExceptionMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateById(ServerException serverException) {
        serverExceptionMapper.updateByPrimaryKey(serverException);
    }
}
