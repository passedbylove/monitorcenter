package com.netmarch.monitorcenter.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ServerGz
 * @Description TODO
 * @Author 王顶奎
 * @Date 2018/12/1414:21
 * @Version 1.0
 **/
@Data
public class ServerGz implements Serializable {
    public static final String SERVER_GROUP_CODE = "server";
    @Id
    private Long id;
    private String name;      //安装包名称
    private String abbreviate;  //缩写
    private String alias;  //别名
    private String version;     //版本号
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;    //创建时间
    private String gzUrl;       //安装包路径
    private String remark;      //备注
    private Integer status;     //状态
    private String groupCode;    //编号
    private String parentId;  //父类Id

}
