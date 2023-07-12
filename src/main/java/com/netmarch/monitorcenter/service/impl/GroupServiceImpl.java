package com.netmarch.monitorcenter.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.PagesStatic;
import com.netmarch.monitorcenter.mapper.GroupMapper;
import com.netmarch.monitorcenter.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * @Author: lining
 * @Description: GroupServiceImpl
 */
@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    GroupMapper groupMapper;

    @Override
    public Page<Group> listGroupsByType(String type, Integer pageNo) {
        PageHelper.startPage(pageNo == null ? 1 : pageNo, PagesStatic.PAGES_SIZE);
        return groupMapper.listGroupsByType(type);
    }

    @Override
    public List<Group> getCodeByType(String type) {
        return groupMapper.getCodeByType(type);
    }

    @Override
    public List<String> listAllType() {
        List<String> list = groupMapper.listAllType();
        LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    @Override
    public boolean saveGroup(Group group) {
        Group checkGroup = groupMapper.getGroupByCode(group.getCode());
        if(checkGroup!=null){
            return false;
        }
        groupMapper.saveGroup(group);
        return true;
    }

    @Override
    public Group getGroupById(int id) {
        return groupMapper.getGroupById(id);
    }

    @Override
    public boolean updateGroup(Group group) {
        Group checkGroup = groupMapper.getGroupByCode(group.getCode());
        if(checkGroup!=null&&!group.getId().equals(checkGroup.getId())){
            return false;
        }
        groupMapper.updateGroup(group);
        return true;
    }

    @Override
    public boolean deleteGroup(int id) {
        Group group = groupMapper.getGroupById(id);
        if(groupMapper.countServerInfoByCode(group.getCode())!=0||groupMapper.countServerScriptByCode(group.getCode())!=0){
            return false;
        }
        groupMapper.deleteGroup(id);
        return true;
    }

    @Override
    public List<Group> getAllGroups() {
        return null;
    }

    @Override
    public List<Group> listAllGroupsByType(String type) {
        return groupMapper.listGroupsByType(type);
    }


}
