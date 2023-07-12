package com.netmarch.monitorcenter.bean;

import com.netmarch.monitorcenter.util.BeanMap;
import lombok.Data;

import java.util.Map;

@Data
public class PageParam implements Page {

    private Integer pageNum;
    private Integer pageSize;
    private BeanMap params;
    public PageParam(Map<String,Object> map){
        this.pageNum = (Integer) map.get(Page.PAGE_NUM);
        this.pageNum = this.pageNum==null? DEFAULT_PAGE_NUM :this.pageNum;
        this.pageSize = (Integer) map.get(Page.PAGE_SIZE);
        this.pageSize = this.pageSize==null?DEFAULT_PAGE_SIZE:this.pageSize;
        map.remove(Page.PAGE_NUM);
        map.remove(Page.PAGE_SIZE);
        this.params = new BeanMap(map);
    }
    public PageParam(Integer pageNum,Integer pageSize){
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
