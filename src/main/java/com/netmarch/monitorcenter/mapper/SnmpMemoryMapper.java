package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.SnmpMemory;
import com.netmarch.monitorcenter.bean.SnmpMemoryUsage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnmpMemoryMapper {
    SnmpMemory getByNode(Long nodeId);
    int saveUsages(List<SnmpMemoryUsage> usageList);
}
