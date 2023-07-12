package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/***
 * 网卡流量类
 */
@Data
@NoArgsConstructor
public class SnmpIfCardTraffic extends SnmpIfCard{

    private Long ifCardId;

    /***
     * 接收字节数
     */
    private Long ifInOctets;

    /***
     * 下行字节数
     */
    private Long ifDownLinkBytes = 0L;

    /***
     * 接收组播数
     */
    private Long ifInUcastPkts;

    /***
     * 接收非组播数
     * This object is deprecated in favour of ifInMulticastPkts and
     * ifInBroadcastPkts.
     */
    private Long ifInNUcastPkts;

    /***
     * 接收丢弃数
     */
    private Long ifInDiscards;

    /***
     * 接收错误数
     */
    private Long ifInErrors;

    /***
     * 接收未知协议数
     */
    private Long ifInUnknownProtos;

    /***
     * 输出字节数
     */
    private Long ifOutOctets;

    /***
     * 上行字节数
     */
    private Long ifUpLinkBytes = 0L;

    /***
     * 输出组播数
     */
    private Long ifOutUcastPkts;


    /***
     * 输出非组播数
     * This object is deprecated in favour of ifOutMulticastPkts
     * and ifOutBroadcastPkts.
     */
    private Long ifOutNUcastPkts;

    /***
     * 输出丢弃数
     */
    private  Long ifOutDiscards;

    /***
     * 输出错误数
     */
    private Long ifOutErrors;

    /***
     * 输出包队列长度
     */
    private Long ifOutQLen;

    /***
     * 别名
     */
    private String ifAlias;

    /***
     * 最后改变时间TimeTicks
     */
    private String ifLastChange;

    /***
     * 冗余字段:插入数据时候的时间戳,方便根据时间排序用
     */
    private long stamp;

    private SnmpIfCardTraffic(Builder builder) {
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
        setIfCardId(builder.ifCardId);
        setIfInOctets(builder.ifInOctets);
        setIfDownLinkBytes(builder.ifDownLinkBytes);
        setIfInUcastPkts(builder.ifInUcastPkts);
        setIfInNUcastPkts(builder.ifInNUcastPkts);
        setIfInDiscards(builder.ifInDiscards);
        setIfInErrors(builder.ifInErrors);
        setIfInUnknownProtos(builder.ifInUnknownProtos);
        setIfOutOctets(builder.ifOutOctets);
        setIfUpLinkBytes(builder.ifUpLinkBytes);
        setIfOutUcastPkts(builder.ifOutUcastPkts);
        setIfOutNUcastPkts(builder.ifOutNUcastPkts);
        setIfOutDiscards(builder.ifOutDiscards);
        setIfOutErrors(builder.ifOutErrors);
        setIfOutQLen(builder.ifOutQLen);
        setIfAlias(builder.ifAlias);
        setIfLastChange(builder.ifLastChange);
    }

    public Long getIfCardId() {
        return ifCardId;
    }

    public void setIfCardId(Long ifCardId) {
        this.ifCardId = ifCardId;
    }

    public Long getIfInOctets() {
        return ifInOctets;
    }

    public void setIfInOctets(Long ifInOctets) {
        this.ifInOctets = ifInOctets;
    }

    public Long getIfInUcastPkts() {
        return ifInUcastPkts;
    }

    public void setIfInUcastPkts(Long ifInUcastPkts) {
        this.ifInUcastPkts = ifInUcastPkts;
    }

    public Long getIfInNUcastPkts() {
        return ifInNUcastPkts;
    }

    public void setIfInNUcastPkts(Long ifInNUcastPkts) {
        this.ifInNUcastPkts = ifInNUcastPkts;
    }

    public Long getIfInDiscards() {
        return ifInDiscards;
    }

    public void setIfInDiscards(Long ifInDiscards) {
        this.ifInDiscards = ifInDiscards;
    }

    public Long getIfInErrors() {
        return ifInErrors;
    }

    public void setIfInErrors(Long ifInErrors) {
        this.ifInErrors = ifInErrors;
    }

    public Long getIfInUnknownProtos() {
        return ifInUnknownProtos;
    }

    public void setIfInUnknownProtos(Long ifInUnknownProtos) {
        this.ifInUnknownProtos = ifInUnknownProtos;
    }

    public Long getIfOutOctets() {
        return ifOutOctets;
    }

    public void setIfOutOctets(Long ifOutOctets) {
        this.ifOutOctets = ifOutOctets;
    }

    public Long getIfOutUcastPkts() {
        return ifOutUcastPkts;
    }

    public void setIfOutUcastPkts(Long ifOutUcastPkts) {
        this.ifOutUcastPkts = ifOutUcastPkts;
    }

    public Long getIfOutNUcastPkts() {
        return ifOutNUcastPkts;
    }

    public void setIfOutNUcastPkts(Long ifOutNUcastPkts) {
        this.ifOutNUcastPkts = ifOutNUcastPkts;
    }

    public Long getIfOutDiscards() {
        return ifOutDiscards;
    }

    public void setIfOutDiscards(Long ifOutDiscards) {
        this.ifOutDiscards = ifOutDiscards;
    }

    public Long getIfOutErrors() {
        return ifOutErrors;
    }

    public void setIfOutErrors(Long ifOutErrors) {
        this.ifOutErrors = ifOutErrors;
    }

    public Long getIfOutQLen() {
        return ifOutQLen;
    }

    public void setIfOutQLen(Long ifOutQLen) {
        this.ifOutQLen = ifOutQLen;
    }

    public String getIfAlias() {
        return ifAlias;
    }

    public void setIfAlias(String ifAlias) {
        this.ifAlias = ifAlias;
    }


    public String getIfLastChange() {
        return ifLastChange;
    }

    public void setIfLastChange(String ifLastChange) {
        this.ifLastChange = ifLastChange;
    }

    public Long getIfDownLinkBytes() {
        return ifDownLinkBytes;
    }

    public void setIfDownLinkBytes(Long ifDownLinkBytes) {
        this.ifDownLinkBytes = ifDownLinkBytes;
    }

    public Long getIfUpLinkBytes() {
        return ifUpLinkBytes;
    }

    public void setIfUpLinkBytes(Long ifUpLinkBytes) {
        this.ifUpLinkBytes = ifUpLinkBytes;
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
        private Long ifCardId;
        private Long ifInOctets;
        private Long ifDownLinkBytes;
        private Long ifInUcastPkts;
        private Long ifInNUcastPkts;
        private Long ifInDiscards;
        private Long ifInErrors;
        private Long ifInUnknownProtos;
        private Long ifOutOctets;
        private Long ifUpLinkBytes;
        private Long ifOutUcastPkts;
        private Long ifOutNUcastPkts;
        private Long ifOutDiscards;
        private Long ifOutErrors;
        private Long ifOutQLen;
        private String ifAlias;
        private String ifLastChange;

        public Builder() {
        }

        public Builder(SnmpIfCardTraffic copy) {
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
            this.ifCardId = copy.getIfCardId();
            this.ifInOctets = copy.getIfInOctets();
            this.ifDownLinkBytes = copy.getIfDownLinkBytes();
            this.ifInUcastPkts = copy.getIfInUcastPkts();
            this.ifInNUcastPkts = copy.getIfInNUcastPkts();
            this.ifInDiscards = copy.getIfInDiscards();
            this.ifInErrors = copy.getIfInErrors();
            this.ifInUnknownProtos = copy.getIfInUnknownProtos();
            this.ifOutOctets = copy.getIfOutOctets();
            this.ifUpLinkBytes = copy.getIfUpLinkBytes();
            this.ifOutUcastPkts = copy.getIfOutUcastPkts();
            this.ifOutNUcastPkts = copy.getIfOutNUcastPkts();
            this.ifOutDiscards = copy.getIfOutDiscards();
            this.ifOutErrors = copy.getIfOutErrors();
            this.ifOutQLen = copy.getIfOutQLen();
            this.ifAlias = copy.getIfAlias();
            this.ifLastChange = copy.getIfLastChange();
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

        public Builder withIfCardId(Long ifCardId) {
            this.ifCardId = ifCardId;
            return this;
        }

        public Builder withIfInOctets(Long ifInOctets) {
            this.ifInOctets = ifInOctets;
            return this;
        }

        public Builder withIfDownLinkBytes(Long ifDownLinkBytes) {
            this.ifDownLinkBytes = ifDownLinkBytes;
            return this;
        }

        public Builder withIfInUcastPkts(Long ifInUcastPkts) {
            this.ifInUcastPkts = ifInUcastPkts;
            return this;
        }

        public Builder withIfInNUcastPkts(Long ifInNUcastPkts) {
            this.ifInNUcastPkts = ifInNUcastPkts;
            return this;
        }

        public Builder withIfInDiscards(Long ifInDiscards) {
            this.ifInDiscards = ifInDiscards;
            return this;
        }

        public Builder withIfInErrors(Long ifInErrors) {
            this.ifInErrors = ifInErrors;
            return this;
        }

        public Builder withIfInUnknownProtos(Long ifInUnknownProtos) {
            this.ifInUnknownProtos = ifInUnknownProtos;
            return this;
        }

        public Builder withIfOutOctets(Long ifOutOctets) {
            this.ifOutOctets = ifOutOctets;
            return this;
        }

        public Builder withIfUpLinkBytes(Long ifUpLinkBytes) {
            this.ifUpLinkBytes = ifUpLinkBytes;
            return this;
        }

        public Builder withIfOutUcastPkts(Long ifOutUcastPkts) {
            this.ifOutUcastPkts = ifOutUcastPkts;
            return this;
        }

        public Builder withIfOutNUcastPkts(Long ifOutNUcastPkts) {
            this.ifOutNUcastPkts = ifOutNUcastPkts;
            return this;
        }

        public Builder withIfOutDiscards(Long ifOutDiscards) {
            this.ifOutDiscards = ifOutDiscards;
            return this;
        }

        public Builder withIfOutErrors(Long ifOutErrors) {
            this.ifOutErrors = ifOutErrors;
            return this;
        }

        public Builder withIfOutQLen(Long ifOutQLen) {
            this.ifOutQLen = ifOutQLen;
            return this;
        }

        public Builder withIfAlias(String ifAlias) {
            this.ifAlias = ifAlias;
            return this;
        }

        public Builder withIfLastChange(String ifLastChange) {
            this.ifLastChange = ifLastChange;
            return this;
        }

        public SnmpIfCardTraffic build() {
            return new SnmpIfCardTraffic(this);
        }
    }
}
