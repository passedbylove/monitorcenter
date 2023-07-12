package com.netmarch.monitorcenter.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName EnumsUtil
 * @Description TODO
 * @Author wangdk
 * @Date 2018/12/148:57
 * @Version 1.0
 **/
public class EnumsUtil {

    /*遍历枚举*/
    public static Map<String, String> getEnumValues(String className)throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Class clazz = Class.forName(className);
            if(clazz!=null){
                for (Object obj : clazz.getEnumConstants()) {
                    Method m = obj.getClass().getDeclaredMethod("values", null);
                    Object[] results = (Object[]) m.invoke(obj, null);
                    for (Object result : results) {
                        Method md = result.getClass().getDeclaredMethod("getValue", null);
                        String value = (String) md.invoke(result, null);
                        Method md2 = result.getClass().getDeclaredMethod("getDescription", null);
                        String description = (String) md2.invoke(result, null);
                        map.put(value, description);
                    }
                    return map;
                }
            }
        } catch (Exception e) {

        }
        return null;
    }


}
