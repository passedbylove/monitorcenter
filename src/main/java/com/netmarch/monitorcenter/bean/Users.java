package com.netmarch.monitorcenter.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户表
 */
@Data
public class Users implements Serializable {
    private Long id;
    private String name;
    private String code;
    private String password;
}
