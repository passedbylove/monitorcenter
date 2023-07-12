package com.netmarch.monitorcenter.controller;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.ServerDeploy;
import com.netmarch.monitorcenter.service.ServerDeployService;
import com.netmarch.monitorcenter.service.ServerManageService;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

/**
 * @Author:xieqiang
 * @Date:2018/12/29
 */
@Controller
@Slf4j
@RequestMapping("serverManage")
public class ServerManageController {
    @Autowired
    private ServerDeployService serverDeployService;

    @Autowired
    private ServerManageService serverManageService;

    @Login
    @GetMapping("list")
    public String list(HashMap<String, Object> map, Integer pageNo, String serverName, String serverIp){
        Page<ServerDeploy> list = serverDeployService.listServerDeploysByNameAndIp(pageNo, serverName, serverIp);
        map.put("list",list);
        map.put("pageNo",pageNo);
        map.put("serverName",serverName);
        map.put("serverIp",serverIp);
        return "serverManage/list";
    }

    /**
     * 启动服务
     * @param id
     * @return
     */
    @Login
    @RequestMapping("start/{id}")
    public ResponseEntity startServer(@PathVariable Integer id) {
        try {
            boolean result = serverManageService.startServer(id);
            if (result) {
                return ResponseEntity.ok().body("启动成功！");
            }else {
                return ResponseEntity.status(500).body("启动失败，请检查日志");
            }
        } catch (Exception e) {
            log.error(e.toString());
            serverDeployService.updateDeployStatus(id);
            return ResponseEntity.status(500).body("启动失败，请检查日志：" + e.getMessage());
        }
    }


    /**
     * 停止服务
     * @param id
     * @return
     */
    @Login
    @RequestMapping("stop/{id}")
    public ResponseEntity stopServer(@PathVariable Integer id) {
        try {
            boolean result = serverManageService.stopServer(id);
            if (result) {
                return ResponseEntity.ok().body("停止成功！");
            }else {
                return ResponseEntity.status(500).body("停止失败，请检查日志");
            }
        } catch (Exception e) {
            log.error(e.toString());
            serverDeployService.updateDeployStatus(id);
            return ResponseEntity.status(500).body("停止失败，请检查日志：" + e.getMessage());
        }
    }

    /**
     * 停止服务
     * @param id
     * @return
     */
    @Login
    @RequestMapping("restart/{id}")
    public ResponseEntity restart(@PathVariable Integer id) {
        try {
            boolean result = serverManageService.restart(id);
            if (result) {
                return ResponseEntity.ok().body("停止成功！");
            }else {
                return ResponseEntity.status(500).body("停止失败，请检查日志");
            }
        } catch (Exception e) {
            log.error(e.toString());
            serverDeployService.updateDeployStatus(id);
            return ResponseEntity.status(500).body("停止失败，请检查日志：" + e.getMessage());
        }
    }

    /**
     * 检查服务状态
     * @param id
     * @return
     */
    @Login
    @RequestMapping("check/{id}")
    public ResponseEntity checkStatus(@PathVariable Integer id) {
        try {
            boolean result = serverManageService.checkStatus(id);
            if (result) {
                return ResponseEntity.ok().body("服务状态正常！");
            }else {
                return ResponseEntity.status(500).body("服务状态异常！");
            }
        } catch (Exception e) {
            log.error(e.toString());
            serverDeployService.updateDeployStatus(id);
            return ResponseEntity.status(500).body("服务器连接异常：" + e.getMessage());
        }
    }

    /**
     * 开放端口
     * @param id
     * @return
     */
    @Login
    @RequestMapping("openFireWall/{id}")
    public void openFireWall(@PathVariable Integer id) {
        try {
            serverManageService.openFireWall(id);
        } catch (Exception e) {
            log.error(e.toString());
            serverDeployService.updateDeployStatus(id);
        }
    }


}
