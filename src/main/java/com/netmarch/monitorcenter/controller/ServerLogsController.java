package com.netmarch.monitorcenter.controller;

import com.netmarch.monitorcenter.bean.PageBean;
import com.netmarch.monitorcenter.service.ServerDeployService;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 服务日志管理
 *
 * @Author:xieqiang
 * @Date:2018/12/15
 */
@Controller
@Slf4j
@RequestMapping("serverLogs")
public class ServerLogsController {
    @Autowired
    private ServerDeployService serverDeployService;

    @GetMapping("list")
    @Login
    public ModelAndView queryServerDeployListByConditions(ModelAndView modelAndView, Integer pageNo, String serverName, String serverIp) {
        modelAndView.setViewName("serverLogs/list");
        PageBean pageBean = serverDeployService.query(pageNo, serverName, serverIp);
        modelAndView.addObject("list", pageBean);
        return modelAndView;
    }

}
