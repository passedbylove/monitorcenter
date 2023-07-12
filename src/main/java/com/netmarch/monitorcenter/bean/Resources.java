package com.netmarch.monitorcenter.bean;

import lombok.Data;

/**
 * 资源实体
 */
@Data
public class Resources {
    private long id;
    private String code;
    private String murl;
    private String furl;
    private long type;
    private long pid;
    private long status;
    private String name;
    private long sort;
    private String css;
    private String display;


}
