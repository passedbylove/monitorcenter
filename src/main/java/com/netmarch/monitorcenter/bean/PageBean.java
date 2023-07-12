package com.netmarch.monitorcenter.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页Bean
 * 
 * @param <T>
 */
@Data
public class PageBean<T> implements Page,Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<T> data;    //结果集
	private Long total;        //总记录数
    private Integer pageNum;    // 第几页
    private Integer pageSize;    // 每页记录数
    private Integer pages;        // 总页数
    private String orderByClause;//排序条件
    private Integer size;        // 当前页的数量 <= pageSize，该属性来自ArrayList的size属性
    private Integer status;
    private String msg = "";
    /**
     * 包装Page对象，因为直接返回Page对象，在JSON处理以及其他情况下会被当成List来处理，
     * 而出现一些问题。
     * @param list          page结果
     */
    public PageBean(List<T> list) {
    	super();
        if (list!=null){
            if (list instanceof com.github.pagehelper.Page) {
                com.github.pagehelper.Page<T> page = (com.github.pagehelper.Page<T>) list;
                this.pageNum = page.getPageNum();
                this.pageSize = page.getPageSize();
                this.total = page.getTotal();
                this.pages = page.getPages();
                this.data= new ArrayList<>(list);
                this.size = page.size();
            }else{
                this.data = list;
                this.pageNum = DEFAULT_PAGE_NUM;
                this.pageSize = DEFAULT_PAGE_SIZE;
                this.total = Integer.toUnsignedLong(list.size());
            }
        }else {
            this.data = new ArrayList<>();
            this.pageNum = DEFAULT_PAGE_NUM;
            this.pageSize = DEFAULT_PAGE_SIZE;
        }
        this.status = 200;
    }
    
    public PageBean() {
    	super();
    }
}

