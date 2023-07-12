package com.netmarch.monitorcenter.query;


import com.netmarch.monitorcenter.bean.PagesStatic;

/**
 * @ClassName ServerGzQuery
 * @Description TODO
 * @Author 王顶奎
 * @Date 2018/12/1415:49
 * @Version 1.0
 **/
public class ServerGzQuery extends PagesStatic {

    //通过Id
    private Long id;

    //通过code 查询
    private String code;

    //通过Type查询
    private String type;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
