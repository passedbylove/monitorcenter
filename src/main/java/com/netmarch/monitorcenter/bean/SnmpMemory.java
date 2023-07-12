package com.netmarch.monitorcenter.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/***
 * 内存条固件信息(内存总量),预装机初始设置信息(交换空间)
 */
@Data
@NoArgsConstructor
public class SnmpMemory {

    private Long id;
    /***
     * 节点id
     */
    private Long nodeId;
    /***
     * 虚拟内存总量
     */
    private Long memTotalSwap;
    /***
     * 物理内存总量
     */
    private Long memTotalReal;

    /***
     * 采集时间(北京时间)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));


    /***
     * 网卡信息最后一次状态采集时间(北京时间)
     */
    @JSONField(serialize = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime/* =LocalDateTime.now(ZoneId.of("UTC+8"))*/;

    public SnmpMemory(Long nodeId, Long memTotalSwap, Long memTotalReal) {
        this.nodeId = nodeId;
        this.memTotalSwap = memTotalSwap;
        this.memTotalReal = memTotalReal;
    }

    private SnmpMemory(Builder builder) {
        setId(builder.id);
        setNodeId(builder.nodeId);
        setMemTotalSwap(builder.memTotalSwap);
        setMemTotalReal(builder.memTotalReal);
        setCreateTime(builder.createTime);
        setLastUpdateTime(builder.lastUpdateTime);
    }


    public static final class Builder {
        private Long id;
        private Long nodeId;
        private Long memTotalSwap;
        private Long memTotalReal;
        private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private LocalDateTime lastUpdateTime/* = LocalDateTime.now(ZoneId.of("UTC+8"))*/;

        public Builder() {
        }

        public Builder(SnmpMemory copy) {
            this.id = copy.getId();
            this.nodeId = copy.getNodeId();
            this.memTotalSwap = copy.getMemTotalSwap();
            this.memTotalReal = copy.getMemTotalReal();
            this.createTime = copy.getCreateTime();
            this.lastUpdateTime = copy.getLastUpdateTime();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
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

        public SnmpMemory build() {
            return new SnmpMemory(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpMemory memory = (SnmpMemory) o;
        return memTotalSwap.equals(memory.memTotalSwap) &&
                memTotalReal.equals(memory.memTotalReal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memTotalSwap, memTotalReal);
    }
}
