package com.netmarch.monitorcenter.service.common;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
* @Description: MessageQueueMarker 功能内容描述
* @Author: fengxiang
* @Date: 2019/1/10 12:45
*/ 
public class MessageQueueMarker {
    public static Queue<String> obtain(String queueName){
        Queue<String> queue = ExpiredMapContainer.get(queueName);
        if (queue==null || !(queue instanceof  LinkedBlockingQueue)){
            queue = new LinkedBlockingQueue();
            ExpiredMapContainer.put(queueName,queue);
        }
        return  queue;
    }
}
