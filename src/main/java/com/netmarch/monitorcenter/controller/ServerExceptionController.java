package com.netmarch.monitorcenter.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.ServerException;
import com.netmarch.monitorcenter.service.ServerExceptionService;
import com.netmarch.monitorcenter.util.BeanUtil;
import com.netmarch.monitorcenter.util.DateUtil;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @Description: controller
 * @Author: dinggan
 * @Date: 2018/12/14 0014
 */
@Controller
@Slf4j
@RequestMapping("serverException")
public class ServerExceptionController {

    @Autowired
    private ServerExceptionService serverExceptionService;

    @RequestMapping("writeException/{serverException}")
    @ResponseBody
    public Map<String, Object> save(@PathVariable("serverException") String serverExceptionStr) {
        ServerException serverException = JSONObject.parseObject(serverExceptionStr, ServerException.class);
        serverException.setCreateTime(new Date());
        serverException.setStatus(0);
        Map<String, Object> result = new HashMap<>();
        result.put("code", "0");
        try {
            int count = serverExceptionService.save(serverException);
            if (count <= 0) {
                result.put("code", "1");
                return result;
            }
        } catch (Exception e) {
            log.info(e.toString());
            result.put("code", "2");
            return result;
        }
        return result;
    }

    @RequestMapping("list")
    @Login
    public String list(Integer pageNo, Model model, String startTime, String endTime, Integer status) {
        try {
            Date startTimeDate = null;
            Date endTimeDate = null;
            if (BeanUtil.isEmpty(startTime) && BeanUtil.isEmpty(endTime)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startTimeDate = calendar.getTime();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endTimeDate = calendar.getTime();

            } else {
                startTimeDate = DateUtil.parseStrToDate(startTime, "yyyy-MM-dd HH:mm:ss");
                endTimeDate = DateUtil.parseStrToDate(endTime, "yyyy-MM-dd HH:mm:ss");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("startTime", startTimeDate);
            param.put("endTime", endTimeDate);
            param.put("status", status);
            Page<ServerException> list = (Page<ServerException>) serverExceptionService.selectAll(pageNo, param);
            model.addAttribute("list", list);
            model.addAttribute("startTime", startTimeDate);
            model.addAttribute("endTime", endTimeDate);
            model.addAttribute("status", status);

        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.toString());
        }

        return "serverException/list";
    }

    @RequestMapping("update")
    @Login
    public String update() {
        try {
            int count = serverExceptionService.updateState();
        } catch (Exception e) {
            log.info(e.toString());
        }
        return "redirect:/serverException/list";
    }

    @RequestMapping("view")
    @Login
    public String view(Integer id, Model model) {
        ServerException serverException = null;
        try {
            serverException = serverExceptionService.getById(id);
            serverException.setStatus(1);
            serverExceptionService.updateById(serverException);
        } catch (Exception e) {
            log.info(e.toString());
        }
        model.addAttribute("bean", serverException);
        return "serverException/view";
    }
}
