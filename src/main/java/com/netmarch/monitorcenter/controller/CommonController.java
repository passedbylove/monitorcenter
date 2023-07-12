package com.netmarch.monitorcenter.controller;

import com.netmarch.monitorcenter.bean.RequestData;
import com.netmarch.monitorcenter.bean.Resources;
import com.netmarch.monitorcenter.bean.StaticObj;
import com.netmarch.monitorcenter.bean.Users;
import com.netmarch.monitorcenter.service.ResourcesService;
import com.netmarch.monitorcenter.service.UsersService;
import com.netmarch.monitorcenter.util.CkeckSession;
import com.netmarch.monitorcenter.util.CryptoUtils;
import com.netmarch.monitorcenter.util.Login;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@Slf4j
public class CommonController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private ResourcesService resourcesService;

    @GetMapping("index")
    public String index(HashMap<String, Object> map, HttpServletRequest request){
        CkeckSession.REMOVE_USER_SESSION(request.getSession());
        return "index";
    }

    /**
     * 跳转公共方法
     * @return
     */
    @GetMapping("pages")
    public String pages(@RequestParam String pages){

        return pages;
    }


    /**
     * 密码修改
     * @return
     */
    @PostMapping("modifyPwd")
    @ResponseBody
    public String modifyPwd(HttpServletRequest request,String oldPsw, String pwd ){

        Users user = CkeckSession.GET_USER_SESSION(request.getSession());

        try {

            Users getUser = usersService.getUsersByCodeAndPassword(user.getCode(),oldPsw);
            if(getUser ==null ){
                return "原密码不正确";
            }


            if(!pwd.matches("[a-zA-Z]+")){
                return "新密码请输入全英文字符";
            }

            usersService.updatePwd(user.getId(),pwd);

            CkeckSession.REMOVE_USER_SESSION(request.getSession());

            return "保存成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "修改失败";
        }

    }


    @GetMapping("login")
    @ResponseBody
    public RequestData login(HttpServletRequest request, Users users){
        RequestData requestData = new RequestData();
        requestData.setCode(StaticObj.R_STATUS_SUCCESS);
        requestData.setMsg(StaticObj.R_STATUS_SUCCESS_MSG);
        Users seesionUser = null;
        try {
            seesionUser = usersService.getUsersByCodeAndPassword(users.getCode(), CryptoUtils.encodeMD5(users.getPassword()));
        } catch (Exception e) {
            requestData.setCode(StaticObj.R_STATUS_ERROR);
            requestData.setMsg(StaticObj.R_SQL_ERROR+e.getMessage());
            log.info(requestData.loginfo());
            return requestData;
        }
        if(seesionUser == null){
            requestData.setCode(StaticObj.R_STATUS_ERROR);
            requestData.setMsg(StaticObj.R_STATUS_ERROR_MSG);
        }
        CkeckSession.SET_USER_SESSION(request.getSession(),seesionUser);
        return requestData;

    }

    @Login
    @GetMapping("main")
    public String main(HttpServletRequest request, Model model){
        Users bean = CkeckSession.GET_USER_SESSION(request.getSession());
        List<Resources> list = resourcesService.getAll();
        List<Resources> mu = new ArrayList<>();
        List<Resources> mu1 = new ArrayList<>();
        for(Resources obj:list){
            if(obj.getPid() == 1)mu.add(obj);
            else mu1.add(obj);
        }
        model.addAttribute("mu",mu);
        model.addAttribute("mu1",mu1);
        model.addAttribute("users",bean);
        return "main";
    }

    @GetMapping("/")
    public String index(){
        return "redirect:/index";
    }

    @GetMapping("indexs")
    public String indexs(){
        return "index/index";
    }
}
