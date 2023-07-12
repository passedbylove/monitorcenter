package com.netmarch.monitorcenter.bean;

import java.io.Serializable;

public class PagesStatic implements Serializable {
    /**
     * 设置每页显示数据条数
     */
    public static int PAGES_SIZE = 10;

    /**
     * 设置默认当前页数
     *
     */
    public Integer pageNo =0;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }
}
