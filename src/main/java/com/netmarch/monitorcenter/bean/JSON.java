package com.netmarch.monitorcenter.bean;

import java.io.Serializable;

public abstract class JSON implements Serializable {
    public String toJSONString(){
        return com.alibaba.fastjson.JSON.toJSONString(this);
    }
}
