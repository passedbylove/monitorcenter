package jnetman.snmp;

import java.net.InetAddress;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;

public class MIB {

	/*
	 * MIB-II Root
	 */
	public static final OID Mib2Root = new OID(".1.3.6.1.2.1");

	/***
	 * UCD SNMP Root
	 */
	public static final OID UCDSnmpRoot = new OID(".1.3.6.1.4.1.2021");

	/***
	 * Host Resource Root
	 */
	public static final OID HostResourcesRoot = new OID(Mib2Root).append(25);


	/*
	 * System
	 */
	public static final OID System = new OID(Mib2Root).append(1);
	public static final OID sysDescr = new OID(System).append(".1.0");
	public static final OID sysUptime = new OID(System).append(".3.0");
	/*
	 * MIB-II Interfaces
	 */
	public static final OID Interfaces = new OID(Mib2Root).append(2);
	public static final OID IfNumber = new OID(Interfaces).append(".1.0");
	public static final OID IfTable = new OID(Interfaces).append(2);
	public static final OID IfTableEntry = new OID(IfTable).append(1);
	public static final OID IfIndex = new OID(IfTableEntry).append(1);
	public static final OID IfDescr = new OID(IfTableEntry).append(2);
	public static final OID IfType = new OID(IfTableEntry).append(3);
	public static final OID IfMtu = new OID(IfTableEntry).append(4);
	public static final OID IfSpeed = new OID(IfTableEntry).append(5);
	public static final OID IfPyhsAddress= new OID(IfTableEntry).append(6);
	public static final OID IfAdminStatus= new OID(IfTableEntry).append(7);
	public static final OID IfOperStatus = new OID(IfTableEntry).append(8);
	public static final OID IfLastChange = new OID(IfTableEntry).append(9);
	public static final OID IfInOctets = new OID(IfTableEntry).append(10);
	public static final OID IfInUcastPkts = new OID(IfTableEntry).append(11);
	public static final OID IfInNUcastPkts = new OID(IfTableEntry).append(12);
	public static final OID IfInDiscards = new OID(IfTableEntry).append(12);
	public static final OID IfInErrors = new OID(IfTableEntry).append(14);
	public static final OID IfInUnknownProtos = new OID(IfTableEntry).append(15);
	public static final OID IfOutOctets = new OID(IfTableEntry).append(16);
	public static final OID IfOutUcastPkts = new OID(IfTableEntry).append(17);
	public static final OID IfOutNUcastPkts = new OID(IfTableEntry).append(18);
	public static final OID IfOutDiscards = new OID(IfTableEntry).append(19);
	public static final OID IfOutErrors = new OID(IfTableEntry).append(20);
	public static final OID IfOutQLen = new OID(IfTableEntry).append(21);
	public static final OID IfSpecific = new OID(IfTableEntry).append(22);
	/*
	 * MIB-II Ip
	 */
	public static final OID Ip = new OID(Mib2Root).append(4);
	public static final OID IpOutNoRoutes = new OID(Ip).append(".12.0");
	public static final OID IpAddrTable = new OID(Ip).append(20);
	public static final OID IpAddrEntry = new OID(IpAddrTable).append(1);
	public static final OID IpAdEntAddr = new OID(IpAddrEntry).append(1);
	public static final OID IpAdEntIfIndex = new OID(IpAddrEntry).append(2);
	public static final OID IpAdEntNetMask = new OID(IpAddrEntry).append(3);

	/***
	 * MIB-II Ip Address Table
	 */

	/*
	 * MIB-II Ip = new OID(.1.3.6.1.2.1.4
	 */
	public static final OID Ospf = new OID(Mib2Root).append(14);
	public static final OID ospfIfMetricTable = new OID(Ospf).append(8);
	public static final OID ospfIfMetricEntry = new OID(ospfIfMetricTable)
			.append(1);
	public static final OID ospfIfMetricValue = new OID(ospfIfMetricEntry)
			.append(4);


	/***
	 * Host Resources - hrSystem
	 */
	public static final OID hrSystem = new OID(HostResourcesRoot).append(1);

	/***
	 * Host Resources - HrStorage
	 */
	public static final OID HrStorage = new OID(HostResourcesRoot).append(2);

	public static final OID  hrStorageTypes = new OID(HrStorage).append(1);
	public static final OID  hrMemorySize = new OID(HrStorage).append(2);
	public static final OID  hrStorageTable = new OID(HrStorage).append(3);
	public static final OID  hrStorageEntry = new OID(hrStorageTable).append(1);

	/***
	 * 内存相关字段信息
	 * text:memory
	 */
	public static final OID memory = new OID(UCDSnmpRoot).append(4);


	//服务器负载相关
	public static final OID laTable = new OID(UCDSnmpRoot).append(10);

	public static final OID laEntry = new OID(laTable).append(1);

	public static final OID laLoad = new OID(laEntry).append(3);
	//Load-1
	public static final OID Load1 = new OID(laLoad).append(1);
	//Load-5
	public static final OID Load5 = new OID(laLoad).append(2);
	//Load-15
	public static final OID Load15 = new OID(laLoad).append(3);


	/***
	 * 机器负载(1,5,15分钟)
	 */
	public static final OID [] linuxLoadOID = new OID[]{
			Load1,Load5,Load15
	};

	//系统运行状态信息
	public static final OID systemStats = new OID(UCDSnmpRoot).append(11);



	/**
	 * Get the corresponding OID of the ospfIfMetricTable for the passed indexes
	 * 
	 * @param entryOid
	 *            ospfIfMetricEntry OID
	 * @param ipAddress
	 *            ospfIfMetricIpAddress
	 * @param addressLessIf
	 *            ospfIfMetricAddressLessIf
	 * @param ifMetricTos
	 *            ospfIfMetricTOS
	 * @return The ospfIfMetricEntry OID completed with the indexes
	 */
	public static final OID getOspfIfMetricEntryOID(OID entryOid,
			InetAddress ipAddress, int addressLessIf, int ifMetricTos) {
		IpAddress addr = new IpAddress(ipAddress);
		Integer32 lessIf = new Integer32(addressLessIf);
		Integer32 tos = new Integer32(ifMetricTos);

		return new OID(entryOid).append(addr.toSubIndex(false))
				.append(lessIf.toSubIndex(false)).append(tos.toSubIndex(false));

	}
}
