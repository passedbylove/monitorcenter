package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.ServerException;

import java.util.List;
import java.util.Map;

public interface ServerExceptionService {

    int save(ServerException serverException);

    List<ServerException> selectAll(Integer pageNo, Map<String,Object> param);

    int updateState();

    ServerException getById(Integer id);

    void updateById(ServerException serverException);
}
