package com.netmarch.monitorcenter.util;

import com.netmarch.monitorcenter.bean.RequestData;
import com.netmarch.monitorcenter.bean.StaticObj;
import org.slf4j.Logger;

/**
 * 返回实体工具类
 */
public class RequestDataUtils {

    /**
     * 成功返回对象
     * @return
     */
    public static RequestData SUCESSES_RED(){
        RequestData requestData = new RequestData();
        requestData.setCode(StaticObj.R_STATUS_SUCCESS);
        requestData.setMsg(StaticObj.UPDATE_MSG_SUCCESS);
        return requestData;
    }

    /**
     * 成功返回对象
     * @return
     */
    public static RequestData SUCESSES_RED_DATA(Object ObjData){
        RequestData requestData = new RequestData();
        requestData.setCode(StaticObj.R_STATUS_SUCCESS);
        requestData.setMsg(StaticObj.UPDATE_MSG_SUCCESS);
        requestData.setObjData(ObjData);
        return requestData;
    }

    /**
     * 异常返回对象
     * @param e
     * @return
     */
    public static RequestData EXP_RED(Exception e,Logger logger){
        RequestData requestData = new RequestData();
        requestData.setCode(StaticObj.R_STATUS_ERROR);
        requestData.setMsg(StaticObj.R_SQL_ERROR+e.getMessage());
        logger.info(requestData.loginfo());
        return requestData;
    }

    /**
     * 错误返回对象
     * @param error
     * @return
     */
    public static RequestData ERROR_RED(String error){
        RequestData requestData = new RequestData();
        requestData.setCode(StaticObj.R_STATUS_ERROR);
        requestData.setMsg(StaticObj.UPDATE_MSG_ERROR+error);
        return requestData;
    }

    /**
     * 错误返回对象
     * @param error
     * @return
     */
    public static RequestData ERROR_RED(String code,String msg){
        RequestData requestData = new RequestData();
        requestData.setCode(code);
        requestData.setMsg(msg);
        return requestData;
    }

}
