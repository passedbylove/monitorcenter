package com.netmarch.monitorcenter.bean;

import lombok.Data;

import java.util.List;

@Data
public class RequestData {
    /**
     *  操作成功
     */
    public static final String SUCCESS_CODE = "1";
    public static final String SUCCESS_MSG = "操作成功";
    public static final String FAIL_CODE = "0";
    public static final String FAIL_MSG = "操作失败";
    public RequestData(){
        this.msg = RequestData.SUCCESS_MSG;
        this.code = RequestData.SUCCESS_CODE;
    }
    public RequestData(Object data){
       this();
       if (data instanceof List){
           this.listData = (List<?>) data;
       }else{
           this.objData = data;
       }
    }
    private String msg;
    private String code;
    private Object objData;
    private List<?> listData;
    private String key;
    public String loginfo(){
        return this.code+":"+this.msg;
    }

}
