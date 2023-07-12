package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.SnmpNode;
import jnetman.snmp.SnmpClient;

import java.util.LinkedList;

public interface SnmpNodeService {
    LinkedList<SnmpNode> loadAllNodes();

    SnmpClient getSnmpClient(String address);
}
