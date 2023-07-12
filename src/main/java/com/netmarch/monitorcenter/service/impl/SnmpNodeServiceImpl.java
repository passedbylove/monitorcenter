package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.bean.SnmpNode;
import com.netmarch.monitorcenter.mapper.SnmpNodeMapper;
import com.netmarch.monitorcenter.service.SnmpNodeService;
import jnetman.network.Network;
import jnetman.network.Node;
import jnetman.snmp.SnmpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class SnmpNodeServiceImpl implements SnmpNodeService {

    @Autowired
    private SnmpNodeMapper snmpNodeMapper;

    ConcurrentHashMap<String,SnmpClient> snmpClients = new ConcurrentHashMap<>();

    @Override
    public LinkedList<SnmpNode> loadAllNodes() {
        return snmpNodeMapper.queryNodes(null);
    }


    public SnmpClient getClient(SnmpNode node)
    {
        String ipAddr = node.getAddress();
        String hostName = node.getSysName();
        String kernel = node.getSysDescr();

        SnmpClient client = snmpClients.get(ipAddr);

        if(client != null)
        {
            return client;
        }

        try {
            Network network = new Network();
            Node _node = new Node(hostName.concat(kernel),network);
            _node.setAddress(InetAddress.getByName(ipAddr));
            client = new SnmpClient(_node);
            snmpClients.put(ipAddr,client);
            return client;
        } catch (UnknownHostException e) {
            log.error("主机解析失败",e);
            return null;
        }
    }

    @Override
    public SnmpClient getSnmpClient(String address) {

        SnmpClient client = snmpClients.get(address);

        if(client !=null) {
            return client;
        }

        try {
            Node node= new Node("snmpNode",new Network());
            node.setAddress(InetAddress.getByName(address));
            client = new SnmpClient(node);
            snmpClients.put(address, client);
            return client;
        } catch (UnknownHostException e) {
            log.error("主机解析失败",e);
        }
        return null;
    }
}
