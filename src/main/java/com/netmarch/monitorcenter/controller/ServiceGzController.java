package com.netmarch.monitorcenter.controller;

import com.alibaba.fastjson.JSONObject;
import com.netmarch.monitorcenter.bean.BeanFactory;
import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.RequestData;
import com.netmarch.monitorcenter.bean.ServerGz;
import com.netmarch.monitorcenter.enums.GroupType;
import com.netmarch.monitorcenter.query.ServerGzQuery;
import com.netmarch.monitorcenter.service.GroupService;
import com.netmarch.monitorcenter.service.ServerGzService;
import com.netmarch.monitorcenter.util.Login;
import com.netmarch.monitorcenter.util.RequestDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @ClassName InstallManageController
 * @Description TODO
 * @Author 王顶奎
 * @Date 2018/12/1411:09
 * @Version 1.0
 **/
@Controller
@Login
@Slf4j
@RequestMapping("serviceGz")
public class ServiceGzController {
    private final static Logger logger = LoggerFactory.getLogger(ServiceGzController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private ServerGzService serviceGzService;

    @Value("${upload.baseUrl}")
    private String uploadUrl;

    @GetMapping("index")
    public ModelAndView index(ServerGzQuery query){

        ModelAndView mv = BeanFactory.getModelAndView();
        query.setType(GroupType.AZBLX.getValue());

        List<Group> typeList = groupService.getCodeByType(GroupType.AZBLX.getValue());

        mv.addObject("typeList",typeList);
        mv.addObject("query", query);
        mv.setViewName("serviceGz/list");
        return  mv;
    }

    @GetMapping("add")
    public ModelAndView add(){
        ModelAndView mv = BeanFactory.getModelAndView();
        List<Group> typeList = groupService.getCodeByType(GroupType.AZBLX.getValue());

        mv.addObject("typeList",typeList);
        mv.setViewName("serviceGz/add");
        return mv;
    }

    @PostMapping("addAction")
    @ResponseBody
    public RequestData addAction(ServerGz serverGz){

        //新增目录parentId默认0 组装父类信息
        serverGz.setParentId("0");
        serverGz.setGzUrl(uploadUrl + serverGz.getAbbreviate());
        serverGz.setCreateTime(new Date());

        try {

            //创建路径文件夹
            File file=new File(serverGz.getGzUrl());
            if(!file.exists())
                file.mkdirs();
            serviceGzService.save(serverGz);
            return RequestDataUtils.SUCESSES_RED_DATA(serverGz.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return  RequestDataUtils.EXP_RED(e,logger);
        }

    }

    @GetMapping("edit/{id}")
    public ModelAndView edit(@PathVariable("id") String id){

        ModelAndView mv = BeanFactory.getModelAndView();

        ServerGz serverGz = serviceGzService.queryById(Long.valueOf(id));
        mv.addObject("serverGz" ,serverGz);
        mv.setViewName("serviceGz/edit");

        return mv;
    }

    @GetMapping("uploadFile/{id}")
    public ModelAndView uploadFile(@PathVariable("id") String id){

        ModelAndView mv = BeanFactory.getModelAndView();

        ServerGz serverGz = serviceGzService.queryById(Long.valueOf(id));
        mv.addObject("serverGz" ,serverGz);
        mv.setViewName("serviceGz/uploadFile");

        return mv;
    }

    @PostMapping("editUpdate")
    @ResponseBody
    public RequestData editUpdate( String id,String name){

        try {
            serviceGzService.editUpdate(name,Long.valueOf(id));
        } catch (Exception e) {
            e.printStackTrace();
            RequestDataUtils.EXP_RED(e,logger);
        }

        return RequestDataUtils.SUCESSES_RED();

    }

    @PostMapping("getTree")
    @ResponseBody
    public JSONObject getTree(ServerGzQuery query){
        query.setType(GroupType.AZBLX.getValue());
        JSONObject notes = serviceGzService.queryList(query);
        return  notes;
    }

    @PostMapping("del")
    @ResponseBody
    public RequestData del(String id){

         RequestData requestData=null;
        try {
            requestData =  serviceGzService.delete(Long.valueOf(id));

        } catch (Exception e) {
            e.printStackTrace();
            return RequestDataUtils.EXP_RED(e,logger);
        }
        return requestData;
    }

    /**
     * 上传
     * @param serverGz
     * @return
     */
    @PutMapping
    @ResponseBody
    public RequestData uploadUpdate(ServerGz serverGz, MultipartHttpServletRequest request){

        return serviceGzService.uploadServer(serverGz,request);
    }

}
