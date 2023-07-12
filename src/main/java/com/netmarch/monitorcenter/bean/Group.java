package com.netmarch.monitorcenter.bean;

import lombok.Data;

/**
 * @Author: lining
 * @Description: 分组
 */
@Data
public class Group {
    //主键
    private Integer id;
    //组名
    private String name;
    //组编码
    private String code;
    //组类型
    private String type;
}
