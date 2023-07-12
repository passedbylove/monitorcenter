package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.StringJoiner;

/***
 * 文件系统使用率
 */
@Data
@NoArgsConstructor
public class SnmpFileSystemUsage extends SnmpFileSystem implements ReadNodeIdable{


    /***
     * 文件系统id(数据库主键)
     */
    Long fsId;

    /*文件系统已用*/
    Long hrStorageUsed;

    /***
     * 冗余字段:插入数据时候的时间戳,方便根据时间排序用
     */
    private long stamp;

    private SnmpFileSystemUsage(Builder builder) {
        setNodeId(builder.nodeId);
        setHrStorageIndex(builder.hrStorageIndex);
        setHrStorageType(builder.hrStorageType);
        setHrStorageDescr(builder.hrStorageDescr);
        setHrStorageAllocationUnits(builder.hrStorageAllocationUnits);
        setHrStorageSize(builder.hrStorageSize);
        setCreateTime(builder.createTime);
        setFsId(builder.fsId);
        setHrStorageUsed(builder.hrStorageUsed);
    }

    public Long getFsId() {
        return fsId;
    }

    public void setFsId(Long fsId) {
        this.fsId = fsId;
    }

    public Long getHrStorageUsed() {
        return hrStorageUsed;
    }

    public void setHrStorageUsed(Long hrStorageUsed) {
        this.hrStorageUsed = hrStorageUsed;
    }


    public static final class Builder {
        private Long nodeId;
        private Integer hrStorageIndex;
        private String hrStorageType;
        private String hrStorageDescr;
        private Integer hrStorageAllocationUnits;
        private Long hrStorageSize;
        private LocalDateTime createTime = LocalDateTime.now(ZoneId.of("UTC+8"));
        private Long fsId;
        private Long hrStorageUsed;

        public Builder() {
        }

        public Builder(SnmpFileSystemUsage copy) {
            this.nodeId = copy.getNodeId();
            this.hrStorageIndex = copy.getHrStorageIndex();
            this.hrStorageType = copy.getHrStorageType();
            this.hrStorageDescr = copy.getHrStorageDescr();
            this.hrStorageAllocationUnits = copy.getHrStorageAllocationUnits();
            this.hrStorageSize = copy.getHrStorageSize();
            this.createTime = copy.getCreateTime();
            this.fsId = copy.getFsId();
            this.hrStorageUsed = copy.getHrStorageUsed();
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

        public Builder withFsId(Long fsId) {
            this.fsId = fsId;
            return this;
        }

        public Builder withHrStorageUsed(Long hrStorageUsed) {
            this.hrStorageUsed = hrStorageUsed;
            return this;
        }

        public SnmpFileSystemUsage build() {
            return new SnmpFileSystemUsage(this);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnmpFileSystemUsage.class.getSimpleName() + "[", "]")
                .add("fsId=" + fsId)
                .add("hrStorageUsed=" + hrStorageUsed)
                .add("hrStorageIndex=" + hrStorageIndex)
                .add("hrStorageType='" + hrStorageType + "'")
                .add("hrStorageDescr='" + hrStorageDescr + "'")
                .add("hrStorageSize=" + hrStorageSize)
                .toString();
    }
}
