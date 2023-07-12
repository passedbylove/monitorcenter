package com.netmarch.monitorcenter.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.netmarch.monitorcenter.converter.IgnoreEscapeCharacterSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.StringJoiner;

/***
 * snmp采集来的文件系统信息
 */
@Data
@NoArgsConstructor
public class SnmpFileSystem {

    private Long id;

    private Long nodeId;

    /*文件系统索引*/
    Integer hrStorageIndex;
    /*文件系统类型*/
    String hrStorageType;
    /*文件系统mounted名称*/
    @JSONField(serializeUsing = IgnoreEscapeCharacterSerializer.class)
    String hrStorageDescr;
    /*文件系统空间单位*/
    Integer hrStorageAllocationUnits;
    /*文件系统大小*/
    Long hrStorageSize;

    /***
     * 采集时间(北京时间)
     */
    private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));

    /***
     * 网卡信息最后一次状态采集时间(北京时间)
     */
    @JSONField(serialize = false)
    private LocalDateTime lastUpdateTime = LocalDateTime.now(ZoneId.of("UTC+8"));

    private SnmpFileSystem(Builder builder) {
        setNodeId(builder.nodeId);
        setHrStorageIndex(builder.hrStorageIndex);
        setHrStorageType(builder.hrStorageType);
        setHrStorageDescr(builder.hrStorageDescr);
        setHrStorageAllocationUnits(builder.hrStorageAllocationUnits);
        setHrStorageSize(builder.hrStorageSize);
        setCreateTime(builder.createTime);
        setLastUpdateTime(builder.lastUpdateTime);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpFileSystem that = (SnmpFileSystem) o;
        return hrStorageIndex.equals(that.hrStorageIndex) &&
                hrStorageSize.equals(that.hrStorageSize) &&
                Objects.equals(hrStorageType, that.hrStorageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hrStorageIndex, hrStorageType, hrStorageSize);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnmpFileSystem.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("节点nodeId=" + nodeId)
                .add("文件系统索引hrStorageIndex=" + hrStorageIndex)
                .add("存储类型hrStorageType='" + hrStorageType + "'")
                .add("挂载节点名称hrStorageDescr='" + hrStorageDescr + "'")
                .add("存储空间大小hrStorageSize=" + hrStorageSize)
                .add("创建时间createTime=" + createTime)
                .toString();
    }

    public static final class Builder {
        private Long nodeId;
        private Integer hrStorageIndex;
        private String hrStorageType;
        private String hrStorageDescr;
        private Integer hrStorageAllocationUnits;
        private Long hrStorageSize;
        private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private LocalDateTime lastUpdateTime;

        public Builder() {
        }

        public Builder(SnmpFileSystem copy) {
            this.nodeId = copy.getNodeId();
            this.hrStorageIndex = copy.getHrStorageIndex();
            this.hrStorageType = copy.getHrStorageType();
            this.hrStorageDescr = copy.getHrStorageDescr();
            this.hrStorageAllocationUnits = copy.getHrStorageAllocationUnits();
            this.hrStorageSize = copy.getHrStorageSize();
            this.createTime = copy.getCreateTime();
            this.lastUpdateTime = copy.getLastUpdateTime();
        }

        public Builder withNodeId(Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder withHrStorageIndex(Integer hrStorageIndex) {
            this.hrStorageIndex = hrStorageIndex;
            return this;
        }

        public Builder withHrStorageType(String hrStorageType) {
            this.hrStorageType = hrStorageType;
            return this;
        }

        public Builder withHrStorageDescr(String hrStorageDescr) {
            this.hrStorageDescr = hrStorageDescr;
            return this;
        }

        public Builder withHrStorageAllocationUnits(Integer hrStorageAllocationUnits) {
            this.hrStorageAllocationUnits = hrStorageAllocationUnits;
            return this;
        }

        public Builder withHrStorageSize(Long hrStorageSize) {
            this.hrStorageSize = hrStorageSize;
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

        public SnmpFileSystem build() {
            return new SnmpFileSystem(this);
        }
    }
}
