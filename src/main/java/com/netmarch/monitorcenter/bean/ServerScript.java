package com.netmarch.monitorcenter.bean;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @Author: lining
 * @Description: server_script
 */
@Data
public class ServerScript {
    //id
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    //脚本名称
    private String scriptName;
    //脚本路径
    private String scriptUrl;
    //脚本分组id 对应Group.code
    private String groupId;
    //创建时间
    private Date createTime;
    //备注
    private String remark;

    //脚本名称
    @Transient
    private String groupName;
}
