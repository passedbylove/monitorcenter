package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.CapturedData;
import com.netmarch.monitorcenter.bean.SnmpNode;
import jnetman.snmp.SnmpClient;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface LinuxHostService{

    LinkedList<SnmpNode> queryAllHosts();

    /***
     * 保存抓取来的信息
     * @param captured
     * @return
     */
    int saveCaptured(CapturedData captured);
    int saveCaptured(List<CapturedData> captured);
    SnmpClient getSnmpClient(String ipAddr);
    void saveJobStatus(ConcurrentHashMap<String, Object> snapShot);
}
