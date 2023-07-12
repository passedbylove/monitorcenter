package com.netmarch.monitorcenter.service.common;

import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ExpiredMapContainer {
//    private static final String NULL_VALUE= null;
    private static final Long EXPIRED_MINUTES = 10L;
    /**
     * expiration(3, TimeUnit.SECONDS)设置超时时间为10分钟
     *
     */
    private static volatile ConcurrentMap<String,Object> dataMap = null;
    static {
        dataMap = new MapMaker()
                .expiration(EXPIRED_MINUTES, TimeUnit.MINUTES)
                .makeMap();
    }
    public static void put(String key,Object value){
        dataMap.put(key,value);
    }
    public static <T> T get(String key){
        return (T) dataMap.get(key);
    }
}
