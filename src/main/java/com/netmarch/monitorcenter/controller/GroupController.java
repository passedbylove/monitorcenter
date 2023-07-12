package com.netmarch.monitorcenter.controller;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.RequestData;
import com.netmarch.monitorcenter.service.GroupService;
import com.netmarch.monitorcenter.util.Login;
import com.netmarch.monitorcenter.util.RequestDataUtils;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: lining
 * @Description: GroupController
 */
@Controller
public class GroupController {

    private final static Logger logger = LoggerFactory.getLogger(GroupController.class);
    @Autowired
    private GroupService groupService;

    @Login
    @RequestMapping("group/list")
    public String list(HashMap<String, Object> map, String type, Integer pageNo){
        Page<Group> list = groupService.listGroupsByType(type, pageNo);
        List<String> typeList = groupService.listAllType();
        map.put("typeList",typeList);
        map.put("list",list);
        map.put("type",type);
        return "group/list";
    }

    @Login
    @RequestMapping("group/add")
    public String add(){
        return "group/add";
    }

    @Login
    @RequestMapping("group/save")
    @ResponseBody
    public RequestData save(Group group){
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            if(!groupService.saveGroup(group)){
                return RequestDataUtils.ERROR_RED("编码code不唯一");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,logger);
        }
        return requestData;
    }

    @Login
    @RequestMapping("group/edit/{id}")
    public String edit(HashMap<String, Object> map, @PathVariable int id){
        Group group =  groupService.getGroupById(id);
        map.put("group",group);
        return "group/edit";
    }

    @Login
    @RequestMapping("group/update")
    @ResponseBody
    public RequestData update(Group group){
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            if(!groupService.updateGroup(group)){
                return RequestDataUtils.ERROR_RED("编码code不唯一");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,logger);
        }
        return requestData;
    }

    @Login
    @RequestMapping("group/delete/{id}")
    @ResponseBody
    public RequestData delete(@PathVariable int id){
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            if(!groupService.deleteGroup(id)){
                return RequestDataUtils.ERROR_RED("分组在用,不可删除");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,logger);
        }
        return requestData;
    }
}
