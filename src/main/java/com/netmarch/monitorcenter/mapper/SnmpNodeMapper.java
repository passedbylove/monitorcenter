package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.SnmpNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface SnmpNodeMapper {
    LinkedList<SnmpNode> queryNodes(Map<String,Object> condition);
}
