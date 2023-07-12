package com.netmarch.monitorcenter.bean;


import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
public class ServerException implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    private String serverIp;

    private String deviceIp;

    private String exceptionDesc;

    private Date createTime;

    private Integer status;
}
