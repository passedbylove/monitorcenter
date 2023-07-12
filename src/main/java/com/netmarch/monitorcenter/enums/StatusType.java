package com.netmarch.monitorcenter.enums;

import com.netmarch.monitorcenter.util.IEnum;

public enum StatusType implements IEnum {

    SUCCESS("0","已部署"),
    FAIL("1","未部署"),
    ;

    private String value;

    private String description;

    StatusType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
