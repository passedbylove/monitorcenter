package com.netmarch.monitorcenter.controller;

import com.alibaba.fastjson.JSON;
import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.ServerInfo;
import com.netmarch.monitorcenter.bean.StaticObj;
import com.netmarch.monitorcenter.exception.RepeatException;
import com.netmarch.monitorcenter.service.GroupService;
import com.netmarch.monitorcenter.service.ServerInfoService;
import com.netmarch.monitorcenter.util.ConnectionUtil;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author:xieqiang
 * @Date:2018/12/12
 */
@Slf4j
@RequestMapping("info")
@Controller
public class ServerInfoController {
    @Autowired
    private ServerInfoService serverInfoService;

    @Autowired
    private GroupService groupService;

    /**
     * 该接口用于查询符合条件的服务器信息
     *
     * @param condition 查询条件
     * @param pageNo    当前页码
     * @return 状态及数据
     */
    @GetMapping("list")
    @Login
    public String queryServerInfoByConditions(ServerInfo condition, Map<String, Object> map,
                                                    @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo) {
        List<ServerInfo> serverInfoList = serverInfoService.queryServerInfoByConditions(condition, pageNo);
        List<Group> groups = groupService.listAllGroupsByType(StaticObj.SERVER_GROUP_TYPE);
        map.put("list", serverInfoList);
        map.put("groups", groups);
        return "info/list";
    }

    /**
     * 该接口用于新增服务器信息
     *
     * @param serverInfo 新增的服务器信息
     * @return 状态及数据
     */
    @PostMapping
    @ResponseBody
    @Login
    public ResponseEntity insertServerInfo(ServerInfo serverInfo) {
        // 测试连接，连接没问题后，获取服务器数据，插入服务器信息
        Boolean testResult = null;
        try {
            testResult = ConnectionUtil.serverConnectionTest(serverInfo.getIpAddress(), serverInfo.getLinkPort(), serverInfo.getUserName(), serverInfo.getPwd());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("服务器无法正常连接，请检查服务器！");
        }
        if (testResult) {
            try {
                boolean result = false;
                try {
                    result = serverInfoService.insertServerInfo(serverInfo);
                } catch (RepeatException e) {
                    log.info("添加服务器信息失败，" + e.getMessage() + serverInfo.getIpAddress());
                    return ResponseEntity.badRequest().body(e.getMessage());
                }
                if (result) {
                    log.info("添加服务器信息成功，" + serverInfo.toString());
                    return ResponseEntity.ok("添加成功");
                } else {
                    log.info("添加服务器信息失败，" + serverInfo.toString());
                    return ResponseEntity.ok("添加失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("添加服务器信息失败：" + e.toString());
                return ResponseEntity.status(500).body("服务器错误，请查看日志！");
            }
        } else {
            return ResponseEntity.badRequest().body("服务器无法正常连接，请检查服务器！");
        }
    }

    /**
     * 该接口用于修改服务器信息
     *
     * @param serverInfo 要修改成的服务器信息
     * @return 状态及数据
     */
    @ResponseBody
    @PutMapping
    @Login
    public ResponseEntity updateServerInfo(ServerInfo serverInfo) {
        Boolean testResult = null;
        try {
            testResult = ConnectionUtil.serverConnectionTest(serverInfo.getIpAddress(), serverInfo.getLinkPort(), serverInfo.getUserName(), serverInfo.getPwd());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("测试连接失败，服务器信息修改失败！");
        }
        if (!testResult) {
            return ResponseEntity.badRequest().body("测试连接失败，服务器信息修改失败！");
        }
        try {
            boolean result = serverInfoService.updateServerInfo(serverInfo);
            if (result) {
                log.info("修改服务器信息成功，" + serverInfo.toString());
                return ResponseEntity.ok("修改成功");
            } else {
                log.info("修改服务器信息失败，" + serverInfo.toString());
                return ResponseEntity.ok("修改失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("修改服务器信息失败：" + e.toString());
            return ResponseEntity.status(500).body("服务器错误，请查看日志！");
        }
    }

    /**
     * 该接口用于删除服务器信息（逻辑删除，改变服务器信息状态）
     *
     * @param id 要修改的服务器信息主键Id
     * @return 状态及数据
     */
    @ResponseBody
    @DeleteMapping("{id}")
    @Login
    public ResponseEntity deleteServerInfo(@PathVariable Integer id) {
        try {
            boolean result = serverInfoService.deleteServerInfo(id);
            if (result) {
                log.info("删除服务器信息成功，id=" + id);
                return ResponseEntity.ok("删除成功");
            } else {
                log.info("删除服务器信息失败，id=" + id);
                return ResponseEntity.ok("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除服务器信息失败：" + e.toString());
            return ResponseEntity.status(500).body("服务器错误，请查看日志！");
        }
    }

    /**
     * 该接口用于回显服务器信息
     *
     * @param id 服务器信息ID
     * @return 带数据的编辑页面
     */
    @GetMapping("{id}")
    @Login
    public ModelAndView queryServerInfoById(@PathVariable Integer id, ModelAndView mv) {
        try {
            ServerInfo serverInfo = serverInfoService.queryServerInfoById(id);
            List<Group> groups = groupService.listAllGroupsByType(StaticObj.SERVER_GROUP_TYPE);
            List<Map<String,String>> list = JSON.parseObject(serverInfo.getNetworkInfo(), List.class);
            mv.addObject("info", serverInfo);
            mv.addObject("groups", groups);
            mv.addObject("networkList", list);
            mv.setViewName("info/edit");
        } catch (Exception e) {
            log.error("回显服务器信息异常" + e.toString());
        }
        return mv;
    }

    /**
     * 跳转添加服务器信息页面
     *
     * @return
     */
    @GetMapping("add")
    @Login
    public String returnAddPage(Map<String,Object> map) {
        List<Group> groups = groupService.getCodeByType(StaticObj.SERVER_GROUP_TYPE);
        map.put("groups", groups);
        return "info/add";
    }

    /**
     * 用于对特定服务器状态检查、更新
     *
     * @param id 服务器信息ID
     * @return 处理结果
     */
    @GetMapping("checkStatus/{id}")
    @Login
    public ResponseEntity checkServiceStatus(@PathVariable Integer id) {
        try {
            boolean result = serverInfoService.checkServiceStatus(id);
            if (result) {
                return ResponseEntity.ok("服务器状态正常！");
            } else {
                return ResponseEntity.ok("服务器状态异常！请及时确认");
            }
        } catch (Exception e) {
            log.error("检查服务器状态异常" + e.toString());
            return ResponseEntity.ok("服务器错误，请查看日志");
        }
    }
}
