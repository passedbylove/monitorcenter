package com.netmarch.monitorcenter.bean;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BeanFactory
 * @Description TODO
 * @Author 王顶奎
 * @Date 2018/12/138:30
 * @Version 1.0
 **/
public class BeanFactory {

    private static Map<String, Object> maps = new HashMap<>();

    static {
        maps.put("logs",new Logs());
    }

    public  static <T>T getBean(String bean,Class<T> valueType){

        return  (T)maps.get(bean);
    }

    public  static Logs getLogs(){

        return  new Logs();
    }

    public  static StringBuffer getStringBuffer(){

        return  new StringBuffer();
    }

    public  static ModelAndView getModelAndView(){

        return  new ModelAndView();
    }

    public  static ServerGz getServerGz(){

        return  new ServerGz();
    }

    public  static JSONObject getJSONObject(){

        return  new JSONObject();
    }
    public  static  <T>List<T> getArrayList(Class<T> list){

        return  new ArrayList<T>();
    }

}
