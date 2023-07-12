package com.netmarch.monitorcenter.service;

import com.alibaba.fastjson.JSONObject;
import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.RequestData;
import com.netmarch.monitorcenter.bean.ServerGz;
import com.netmarch.monitorcenter.query.ServerGzQuery;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

/**
 * @ClassName ServiceGzService
 * @Descriptio 安装包服务管理
 * @Author 王顶奎
 * @Date 2018/12/1216:47
 * @Version 1.0
 **/
public interface ServerGzService {
    /**
    * @Description: ServerGzService 查出所有安装包供选择
    * @Author: fengxiang
    * @Date: 2018/12/18 9:21
    */
    List<ServerGz> queryAll();

    List<ServerGz> queryAllWithParent();

    /**
     * 通过group code 获取所有安装包 获取tree结构
     * @return
     */
    public JSONObject queryList(ServerGzQuery query);

    /**
     *  保存安装包信息
     * @return
     */
    public void save(ServerGz serverGz) throws Exception;


    /**
     *  更新安装包信息
     * @return
     */
    public ServerGz queryById(Long id);


    /**
     *  保存安装包信息
     * @return
     */
    public RequestData delete(Long id) throws Exception;

    /**
     *  更新安装包信息
     * @return
     */
    public RequestData update(ServerGz serverGz);

    /**
     *  更新name
     * @return
     */
    public void editUpdate(String name,Long id) throws Exception;

    /**
     *  查询安装包code
     * @return
     */
    public List<Group> getCodeByType(ServerGzQuery query);

    /**
     *  通过父类ID获取下面子菜单数
     * @return
     */
    public Integer getCountChildenById(Long id);

    /**
     *  通过父类ID获取下面子菜单数
     * @return
     */
    public RequestData uploadServer(ServerGz serverGz, MultipartHttpServletRequest request);

    /**
     * 通过id查询
     * @param gzId
     * @return
     */
    ServerGz selectGzById(Integer gzId);
    /**
     * 获取更新的安装包
     */
    List<ServerGz> queryNew(Integer parentId,String version);
    /**
     * 获取旧的安装包
     */
    List<ServerGz> queryOld(Integer parentId,String version);

    /**
     * 更新安装包状态
     */
    public void updateStatus(Long id ,Integer status);

}
