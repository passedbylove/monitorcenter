package com.netmarch.monitorcenter.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

//有几个参数过时http://net-snmp.sourceforge.net/docs/mibs/interfaces.html
@Data
@NoArgsConstructor
public class SnmpIfCard implements ReadNodeIdable{
    private Long id;

    private Long nodeId;

    /***
     * 索引
     */
    private Integer ifIndex;
    /***
     * 描述
     */
    private String ifDescr;
    /***
     * 类型
     * INTEGER  { other ( 1 ) , regular1822 ( 2 ) , hdh1822 ( 3 ) , ddn-x25 ( 4 ) , rfc877-x25 ( 5 ) , ethernet-csmacd ( 6 ) , iso88023-csmacd ( 7 ) , iso88024-tokenBus ( 8 ) , iso88025-tokenRing ( 9 ) , iso88026-man ( 10 ) , starLan ( 11 ) , proteon-10Mbit ( 12 ) , proteon-80Mbit ( 13 ) , hyperchannel ( 14 ) , fddi ( 15 ) , lapb ( 16 ) , sdlc ( 17 ) , ds1 ( 18 ) , e1 ( 19 ) , basicISDN ( 20 ) , primaryISDN ( 21 ) , propPointToPointSerial ( 22 ) , ppp ( 23 ) , softwareLoopback ( 24 ) , eon ( 25 ) , ethernet-3Mbit ( 26 ) , nsip ( 27 ) , slip ( 28 ) , ultra ( 29 ) , ds3 ( 30 ) , sip ( 31 ) , frame-relay ( 32 ) }
     */
    private Integer ifType;


    private Long ifMtu;

    /***
     * 速度
     */
    private Long ifSpeed;

    /***
     * MAC
     */
    private String ifPhysAddress;

    /***
     * 采集时间(北京时间)
     */
    private LocalDateTime createTime =LocalDateTime.now(ZoneId.of("UTC+8"));

    /***
     * 网卡信息最后一次状态采集时间(北京时间)
     */
    @JSONField(serialize = false)
    private LocalDateTime lastUpdateTime =LocalDateTime.now(ZoneId.of("UTC+8"));


    /***
     * IP地址
     */
    private String ifIpAddr;

    /***
     * 子网掩码
     */
    private String ifIpNetMask;

    /***
     * 管理状态:{ up ( 1 ) , down ( 2 ) , testing ( 3 ) }
     */
    private Integer ifAdminStatus;

    /***
     * 操作状态:{ up ( 1 ) , down ( 2 ) , testing ( 3 ) }
     */
    private Integer ifOperStatus;

    /***
     * 规范
     */
    private String ifSpecific;

    private SnmpIfCard(Builder builder) {
        setNodeId(builder.nodeId);
        setIfIndex(builder.ifIndex);
        setIfDescr(builder.ifDescr);
        setIfType(builder.ifType);
        setIfMtu(builder.ifMtu);
        setIfSpeed(builder.ifSpeed);
        setIfPhysAddress(builder.ifPhysAddress);
        setCreateTime(builder.createTime);
        setLastUpdateTime(builder.lastUpdateTime);
        setIfIpAddr(builder.ifIpAddr);
        setIfIpNetMask(builder.ifIpNetMask);
        setIfAdminStatus(builder.ifAdminStatus);
        setIfOperStatus(builder.ifOperStatus);
        setIfSpecific(builder.ifSpecific);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpIfCard ifCard = (SnmpIfCard) o;
        return ifAdminStatus == ifCard.ifAdminStatus &&
                ifOperStatus == ifCard.ifOperStatus &&
                nodeId.equals(ifCard.nodeId) &&
                ifIndex.equals(ifCard.ifIndex) &&
                ifDescr.equals(ifCard.ifDescr) &&
                ifType.equals(ifCard.ifType) &&
                ifMtu.equals(ifCard.ifMtu) &&
                ifSpeed.equals(ifCard.ifSpeed) &&
                Objects.equals(ifIpAddr, ifCard.ifIpAddr) &&
                Objects.equals(ifIpNetMask, ifCard.ifIpNetMask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, ifIndex, ifDescr, ifType, ifMtu, ifSpeed, ifIpAddr, ifIpNetMask, ifAdminStatus, ifOperStatus);
    }

    public static final class Builder {
        private Long nodeId;
        private Integer ifIndex;
        private String ifDescr;
        private Integer ifType;
        private Long ifMtu;
        private Long ifSpeed;
        private String ifPhysAddress;
        private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private LocalDateTime lastUpdateTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private String ifIpAddr;
        private String ifIpNetMask;
        private Integer ifAdminStatus;
        private Integer ifOperStatus;
        private String ifSpecific;

        public Builder() {
        }

        public Builder(SnmpIfCard copy) {
            this.nodeId = copy.getNodeId();
            this.ifIndex = copy.getIfIndex();
            this.ifDescr = copy.getIfDescr();
            this.ifType = copy.getIfType();
            this.ifMtu = copy.getIfMtu();
            this.ifSpeed = copy.getIfSpeed();
            this.ifPhysAddress = copy.getIfPhysAddress();
            this.createTime = copy.getCreateTime();
            this.lastUpdateTime = copy.getLastUpdateTime();
            this.ifIpAddr = copy.getIfIpAddr();
            this.ifIpNetMask = copy.getIfIpNetMask();
            this.ifAdminStatus = copy.getIfAdminStatus();
            this.ifOperStatus = copy.getIfOperStatus();
            this.ifSpecific = copy.getIfSpecific();
        }

        public Builder withNodeId(Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder withIfIndex(Integer ifIndex) {
            this.ifIndex = ifIndex;
            return this;
        }

        public Builder withIfDescr(String ifDescr) {
            this.ifDescr = ifDescr;
            return this;
        }

        public Builder withIfType(Integer ifType) {
            this.ifType = ifType;
            return this;
        }

        public Builder withIfMtu(Long ifMtu) {
            this.ifMtu = ifMtu;
            return this;
        }

        public Builder withIfSpeed(Long ifSpeed) {
            this.ifSpeed = ifSpeed;
            return this;
        }

        public Builder withIfPhysAddress(String ifPhysAddress) {
            this.ifPhysAddress = ifPhysAddress;
            return this;
        }

        public Builder withCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder withIfIpAddr(String ifIpAddr) {
            this.ifIpAddr = ifIpAddr;
            return this;
        }

        public Builder withIfIpNetMask(String ifIpNetMask) {
            this.ifIpNetMask = ifIpNetMask;
            return this;
        }

        public Builder withIfAdminStatus(Integer ifAdminStatus) {
            this.ifAdminStatus = ifAdminStatus;
            return this;
        }

        public Builder withIfOperStatus(Integer ifOperStatus) {
            this.ifOperStatus = ifOperStatus;
            return this;
        }

        public Builder withIfSpecific(String ifSpecific) {
            this.ifSpecific = ifSpecific;
            return this;
        }

        public SnmpIfCard build() {
            return new SnmpIfCard(this);
        }
    }
}
