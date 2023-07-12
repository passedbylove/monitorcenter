package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.ServerException;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface ServerExceptionMapper extends Mapper<ServerException> {

    int updateState();

    List<ServerException> getAll(Map<String,Object> param);
}
