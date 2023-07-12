package com.netmarch.monitorcenter.controller;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.BeanFactory;
import com.netmarch.monitorcenter.bean.Logs;
import com.netmarch.monitorcenter.query.LogsQuery;
import com.netmarch.monitorcenter.service.LogsService;
import com.netmarch.monitorcenter.util.DateUtil;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

/**
 * @ClassName LogsController
 * @Description 操作日志Controller
 * @Author 王顶奎
 * @Date 2018/12/1216:43
 * @Version 1.0
 **/
@Controller
@Login
@Slf4j
@RequestMapping(value = "logs")
public class LogsController {

    @Autowired
    public LogsService logsService;

    @Login
    @GetMapping("list")
    public String list(HashMap<String, Object> map, LogsQuery logsQuery,Boolean flag){

        if(flag){
            logsQuery.setStartTime(DateUtil.dayOfBefore(30));
            logsQuery.setEndTime(DateUtil.getDate());
        }

        Page<Logs> list = logsService.query(logsQuery);
        map.put("list",list);
        map.put("logsQuery",logsQuery);
        return "logs/list";
    }

    @Login
    @GetMapping("check/{id}")
    public ModelAndView check(@PathVariable Long id){

        Logs logs = logsService.queryById(id);

        ModelAndView mv = BeanFactory.getModelAndView();
        mv.addObject("logs",logs);
        mv.setViewName("logs/check");
        return mv;

    }


}
