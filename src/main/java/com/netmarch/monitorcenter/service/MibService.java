package com.netmarch.monitorcenter.service;

import jnetman.snmp.SnmpClient;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

/***
 * 提供Mib树相关查询服务
 */
public interface MibService extends OIDService{


    /**
     * 将oid翻译成对应的文本语义 eg. .1.3.6.1.4.1.2021.9.1.6 ->dskTotal
     * @param oid
     * @return
     */
    String[] translate2Label(OID oid);

    /***
     * 将文本语义的OID翻译成数字形式 eg. dskTotal -> .1.3.6.1.4.1.2021.9.1.6
     * @param label textual form of an object id
     * @return
     */
    OID translateOID(String label);

    OID[] translateOID(String [] labels);

    /***
     *
     * @param labels
     * @param append 后缀 例如 .0
     * @return
     */
    OID[] translateOID(String [] labels, String append);

    OID[] translateOID(String [] labels, int append);


    VariableBinding getNext(String address, OID oid);

    VariableBinding[] getNext(String address, OID[] oids);

    VariableBinding get(String address, OID oid);

    VariableBinding[] get(String address, OID[] oids);

    VariableBinding[] walk(String address, OID oid);

    @Deprecated
    SnmpClient getSnmpClient(String address);

//    void test2();
//
//    void test() throws DuplicateElementException, LinkAlreadyConnectedException, UnknownHostException, TimeoutException, SnmpErrorException;
}
