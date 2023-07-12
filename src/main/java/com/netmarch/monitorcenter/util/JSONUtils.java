package com.netmarch.monitorcenter.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/** 
* @Description: JSONUtils 跟json处理相关工具
* @Author: fengxiang
* @Date: 2018/10/25 8:46
*/ 
public class JSONUtils {
    public static JSONObject parseJSONObject(Object javaBean){
        return  JSON.parseObject(JSON.toJSONString(javaBean, SerializerFeature.WriteMapNullValue));
    }

}
