package com.netmarch.monitorcenter.service.impl;


import com.netmarch.monitorcenter.bean.Resources;
import com.netmarch.monitorcenter.config.db.DatabaseContextHolder;
import com.netmarch.monitorcenter.config.db.DatabaseType;
import com.netmarch.monitorcenter.mapper.ResourcesMapper;
import com.netmarch.monitorcenter.service.ResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourcesServiceImpl implements ResourcesService {
    @Autowired
    private ResourcesMapper resourcesMapper;

    public List<Resources> getAll(){
        DatabaseContextHolder.close();
        DatabaseContextHolder.setDatabaseType(DatabaseType.ONEDB);
        return resourcesMapper.getAll();
    }
}
