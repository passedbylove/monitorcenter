package com.netmarch.monitorcenter.util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 拦截器公共方法
 */
public class InterUtils {
    /**
     * 返回弹出框
     * @param request
     * @param response
     * @param msg
     * @throws Exception
     */
    public static void returnAlert(HttpServletRequest request, HttpServletResponse response, String msg) throws Exception{
        response.setContentType("text/html; charset=UTF-8"); //转码
        PrintWriter out = response.getWriter();
        out.flush();
        out.println("<script type='text/javascript' src='"+request.getContextPath()+"/jquery/jquery.min.js'></script>");
        out.println("<script src='"+request.getContextPath()+"/layer/layer.js'></script>");
        out.println("<script>");

        String strJs ="$(function(){ "
                + "layer.alert('"+msg+"', {"
                + "	    skin: 'layui-layer-molv'"
                + "	    ,closeBtn: 0"
                + "	    ,anim: 2 "
                + "	  },"
                + "	function(){parent.parent.location.href='"+request.getContextPath()+"/';});"
                + "});"
                + "</script>";

        out.println(strJs);
    }
}
