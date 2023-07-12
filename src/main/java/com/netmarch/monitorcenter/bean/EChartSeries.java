package com.netmarch.monitorcenter.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

/***
 * //    name:'邮件营销',
 * //    type:'line',
 * //    stack: '内存使用率',
 * //    data:[120, 132, 101, 134, 90, 230, 210]
 */
@Data
@NoArgsConstructor
public class EChartSeries {
    private String name;
    private String type;
    private String stack;
    private LinkedList<String> data;

    private EChartSeries(Builder builder) {
        setName(builder.name);
        setType(builder.type);
        setStack(builder.stack);
        setData(builder.data);
    }


    public static final class Builder {
        private String name;
        private String type;
        private String stack;
        private LinkedList<String> data;

        public Builder() {
        }

        public Builder(EChartSeries copy) {
            this.name = copy.getName();
            this.type = copy.getType();
            this.stack = copy.getStack();
            this.data = copy.getData();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withStack(String stack) {
            this.stack = stack;
            return this;
        }

        public Builder withData(LinkedList<String> data) {
            this.data = data;
            return this;
        }

        public EChartSeries build() {
            return new EChartSeries(this);
        }
    }
}
