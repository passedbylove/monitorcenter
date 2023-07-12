package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.service.MibService;
import com.netmarch.monitorcenter.service.OIDService;
import fr.jrds.smiextensions.MibTree;
import jnetman.network.Network;
import jnetman.network.Node;
import jnetman.snmp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Primary
public class MibServiceImpl extends OIDServiceImpl implements MibService {

    /***
     *使用static 防止多次加载mibstree.txt，产生不必要的磁盘IO
     */
    static MibTree resolver = new MibTree();

    @Deprecated
    ConcurrentHashMap<String,SnmpClient> snmpClients = new ConcurrentHashMap<>();


    /***
     * {@inheritDoc}
     */
    @Override
    public String[] translate2Label(OID oid)
    {
        Object[] text = resolver.parseIndexOID(oid);

        if(ArrayUtils.isNotEmpty(text))
        {
            String [] t = new String[text.length];
            for (int i = 0; i < text.length; i++) {
                log.debug("名称:{} 数据类型:{}",text[i],text[i].getClass());
                t[i]=String.valueOf(text[i]);
            }
            return t;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public OID translateOID(String label)
    {
        OID[] oids = translateOID(new String[]{label});
        if(ArrayUtils.isNotEmpty(oids))
        {
            return oids[0];
        }
        return null;
    }

    @Override
    public OID[] translateOID(String [] labels)
    {
        return translateOID(labels,null);
    }

    @Override
    public OID[] translateOID(String [] labels, String append)
    {
        if(labels ==null || labels.length ==0)
        {
            return null;
        }

        OID [] oids = new OID[labels.length];
        for (int i = 0; i < oids.length; i++) {
            int [] tmp = resolver.getFromName(labels[i]);
            if(tmp == null || tmp.length==0){
                log.error("无效的别名", labels[i]);
            }
            OID baseOID = new OID(tmp);
            if(StringUtils.isNotBlank(append)) {
                baseOID.append(append);
            }
            oids[i] =baseOID;
        }
        return oids;
    }

    @Override
    public OID[] translateOID(String [] labels, int append)
    {
        throw  new NotImplementedException("方法尚未实现");
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public VariableBinding getNext(String address, OID oid)
    {
        VariableBinding []varbs = getNext(address,new OID[]{oid});
        if(ArrayUtils.isNotEmpty(varbs))
        {
            return varbs[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBinding[] getNext(String address, OID[] oids)
    {
        SnmpHelper helper= new SnmpHelper(getSnmpClient(address));
        Table table = helper.getTable(translateOID("dskEntry"));
        try {
            return getSnmpClient(address).getNext(oids);
        } catch (TimeoutException e) {
            log.error("主机请求超时:",e);
        } catch (SnmpErrorException e) {
            log.error("系统错误:",e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBinding get(String address, OID oid)
    {
        VariableBinding[] varbs = get(address, new OID[]{oid});
        if(ArrayUtils.isNotEmpty(varbs))
        {
            return varbs[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBinding[] get(String address, OID[] oids)
    {
        try {
            return getSnmpClient(address).get(oids);
        } catch (TimeoutException e) {
            log.error("主机请求超时:",e);
        } catch (SnmpErrorException e) {
            log.error("系统错误:",e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableBinding[] walk(String address, OID oid)
    {
        return getSnmpClient(address).walk(oid);
    }

    /***
     * #TODO：每次都构造一个新的对象是否有必要（每次InetAddress.getByName开销等）
     * 1、网络节点波动（wlan、wan不通？服务器宕机、161端口故障（防火墙策略更改等）？重启？）
     * 2、启动时一次性实例化，每添加一个节点把snmpclient实例放内存
     * @param address ip或者主机名
     * @return
     * @throws UnknownHostException
     */
    @Override
    public SnmpClient getSnmpClient(String address){
        SnmpClient snmpClient = snmpClients.get(address);

        if(snmpClient!=null) {
            return snmpClient;
        }

        try {
            Node node= new Node("snmpNode",new Network());
            node.setAddress(InetAddress.getByName(address));
            snmpClient = new SnmpClient(node);
            snmpClients.put(address,snmpClient);
            return snmpClient;
        } catch (UnknownHostException e) {
            log.error("主机解析失败",e);
        }
        return null;
    }
}
