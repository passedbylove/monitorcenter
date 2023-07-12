package com.netmarch.monitorcenter.util;


import com.netmarch.monitorcenter.bean.StaticObj;
import com.netmarch.monitorcenter.bean.Users;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 验证session类和操作sesion的类
 */
public class CkeckSession {

    /**
     * 校验用户是否存在session中
     * @param session
     * @return
     */
    public static boolean CHECK_USER_SESSION(HttpSession session){
        Users users = (Users) session.getAttribute(StaticObj.SESSION_KEY);
        if(users == null){
            return false;
        }
        return true;
    }

    /**
     * 用户保存在session中
     * @param session
     * @param users
     */
    public static void SET_USER_SESSION(HttpSession session, Users users){
        session.setAttribute(StaticObj.SESSION_KEY,users);
    }

    /**
     * 获取session中的用户信息
     * @param session
     * @return
     */
    public static Users GET_USER_SESSION(HttpSession session){
        return (Users) session.getAttribute(StaticObj.SESSION_KEY);
    }

    /**
     * 系统模块保存在session中
     * @param session
     * @param map
     */
    public static void SET_MAP_MODULE_SESSION(HttpSession session, Map<String, String> map){
        session.setAttribute(StaticObj.SESSION_MODULE,map);
    }

    /**
     * 取出session中系统模块
     * @param session
     * @param map
     */
    public static Map<String, String> GET_MAP_MODULE_SESSION(HttpSession session){
        return  (Map<String, String>)session.getAttribute(StaticObj.SESSION_MODULE);
    }

    /**
     * 清空session中的用户信息
     * @param session
     */
    public static void REMOVE_USER_SESSION(HttpSession session){
        session.removeAttribute(StaticObj.SESSION_KEY);
    }
}
