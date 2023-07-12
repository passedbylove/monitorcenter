package com.netmarch.monitorcenter.bean;

/** 
* @Description: Page 分页相关
* @Author: fengxiang
* @Date: 2018/12/3 17:28
*/ 
public interface Page {
    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    /**
     * 第几页
      * @return
     */
  public Integer getPageNum();

    /**
     *  每页多少条
     * @return
     */
  public Integer getPageSize();
}
