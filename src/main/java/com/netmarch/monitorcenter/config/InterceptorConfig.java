package com.netmarch.monitorcenter.config;

import com.netmarch.monitorcenter.bean.StaticObj;
import com.netmarch.monitorcenter.util.CkeckSession;
import com.netmarch.monitorcenter.util.InterUtils;
import com.netmarch.monitorcenter.util.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class InterceptorConfig implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(InterceptorConfig.class);

    /**
     * 进入controller层之前拦截请求
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        HttpSession session = httpServletRequest.getSession();
        //return true;
        if (o instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) o;
            Login methodLogin = handlerMethod.getMethodAnnotation(Login.class);
            Login classLogin = handlerMethod.getBeanType().getAnnotation(Login.class);

            // 没有声明权限,放行
            if (null == methodLogin && null == classLogin) {
                return true;
            }
            //seeesion过期
            if(!CkeckSession.CHECK_USER_SESSION(session)){
                InterUtils.returnAlert(httpServletRequest,httpServletResponse, StaticObj.INTER_ALERT_SESSION_NULL);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //log.info("--------------处理请求完成后视图渲染之前的处理操作---------------");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //log.info("---------------视图渲染之后的操作-------------------------");
    }
}
