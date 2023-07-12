package com.netmarch.monitorcenter.enums;

import com.netmarch.monitorcenter.util.IEnum;

public enum GroupType implements IEnum {

    AZBLX("AZBLX","安装包类型"),
    ;

    private String value;

    private String description;

    GroupType(String value, String description) {
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
