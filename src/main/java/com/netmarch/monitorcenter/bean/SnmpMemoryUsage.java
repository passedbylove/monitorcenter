package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SnmpMemoryUsage extends SnmpMemory implements ReadNodeIdable{

    /***
     * snmp memory id
     */
    private Long memId;

    /***
     * 可用虚拟内存
     */
    private Long memAvailSwap;

    /***
     * 可用物理内存
     */
    private Long memAvailReal;

    //以下三个保留字段
    private Long memShared;

    private Long memBuffer;

    private Long memCached;

    /***
     * 冗余字段:插入数据时候的时间戳,方便根据时间排序用
     */
    private long stamp;


    public SnmpMemoryUsage(Long nodeId, Long memTotalSwap, Long memTotalReal, Long memAvailSwap, Long memAvailReal) {
        super(nodeId, memTotalSwap, memTotalReal);
        this.memAvailSwap = memAvailSwap;
        this.memAvailReal = memAvailReal;
    }

    private SnmpMemoryUsage(Builder builder) {
        setNodeId(builder.nodeId);
        setMemTotalSwap(builder.memTotalSwap);
        setMemTotalReal(builder.memTotalReal);
        setCreateTime(builder.createTime);
        setLastUpdateTime(builder.lastUpdateTime);
        setMemId(builder.memId);
        setMemAvailSwap(builder.memAvailSwap);
        setMemAvailReal(builder.memAvailReal);
        setMemShared(builder.memShared);
        setMemBuffer(builder.memBuffer);
        setMemCached(builder.memCached);
    }


    public static final class Builder {
        private Long nodeId;
        private Long memTotalSwap;
        private Long memTotalReal;
        private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private LocalDateTime lastUpdateTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private Long memId;
        private Long memAvailSwap;
        private Long memAvailReal;
        private Long memShared;
        private Long memBuffer;
        private Long memCached;

        public Builder() {
        }

        public Builder(SnmpMemoryUsage copy) {
            this.nodeId = copy.getNodeId();
            this.memTotalSwap = copy.getMemTotalSwap();
            this.memTotalReal = copy.getMemTotalReal();
            this.createTime = copy.getCreateTime();
            this.lastUpdateTime = copy.getLastUpdateTime();
            this.memId = copy.getMemId();
            this.memAvailSwap = copy.getMemAvailSwap();
            this.memAvailReal = copy.getMemAvailReal();
            this.memShared = copy.getMemShared();
            this.memBuffer = copy.getMemBuffer();
            this.memCached = copy.getMemCached();
        }

        public Builder withNodeId(Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder withMemTotalSwap(Long memTotalSwap) {
            this.memTotalSwap = memTotalSwap;
            return this;
        }

        public Builder withMemTotalReal(Long memTotalReal) {
            this.memTotalReal = memTotalReal;
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

        public Builder withMemId(Long memId) {
            this.memId = memId;
            return this;
        }

        public Builder withMemAvailSwap(Long memAvailSwap) {
            this.memAvailSwap = memAvailSwap;
            return this;
        }

        public Builder withMemAvailReal(Long memAvailReal) {
            this.memAvailReal = memAvailReal;
            return this;
        }

        public Builder withMemShared(Long memShared) {
            this.memShared = memShared;
            return this;
        }

        public Builder withMemBuffer(Long memBuffer) {
            this.memBuffer = memBuffer;
            return this;
        }

        public Builder withMemCached(Long memCached) {
            this.memCached = memCached;
            return this;
        }

        public SnmpMemoryUsage build() {
            return new SnmpMemoryUsage(this);
        }
    }
}
