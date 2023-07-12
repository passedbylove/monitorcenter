package com.netmarch.monitorcenter.controller;

import com.netmarch.monitorcenter.bean.PageBean;
import com.netmarch.monitorcenter.service.ServerDeployService;
import com.netmarch.monitorcenter.service.ServerMonitorService;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author:xieqiang
 * @Date:2018/12/15
 */
@Controller
@RequestMapping("monitor")
@Slf4j
public class ServerMonitorController {
    @Autowired
    private ServerMonitorService serverMonitorService;

    @Autowired
    private ServerDeployService serverDeployService;

    /**
     * 列表，包含查询条件
     */
    @GetMapping("list")
    @Login
    public ModelAndView toList(ModelAndView modelAndView, Integer pageNo, String serverName, String serverIp){
        modelAndView.setViewName("serverMonitor/list");
        PageBean pageBean = serverDeployService.query(pageNo,serverName,serverIp);
        modelAndView.addObject("list",pageBean);
        return  modelAndView;
    }

    /**
     * 检查所选Deploy Id所部署的服务器状态，根据服务器状态，则更新部署状态
     *
     * @param id
     */
    @GetMapping("checkStatus/{id}")
    @ResponseBody
    @Login
    public String checkServerStatus(@PathVariable Integer id) {
        try {
            return serverMonitorService.checkServerStatus(id);
        } catch (Exception e) {
            log.error("检查id为" + id + "的服务器状态失败" + e.toString());
            return "服务器错误";
        }
    }
}
