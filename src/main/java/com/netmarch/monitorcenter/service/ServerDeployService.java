package com.netmarch.monitorcenter.service;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.PageBean;
import com.netmarch.monitorcenter.bean.PageParam;
import com.netmarch.monitorcenter.bean.ServerDeploy;

import java.util.List;

/**
 * @Author: lining
 * @Description: ServerDeployService
 */
public interface ServerDeployService {
    /**
     * comment: 根据页码、服务名称、ip获得服务分页列表
     * @param: [pageNo, serverName, ip]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.ServerDeploy>
     * @date: 2018/12/14 9:57
     */
    Page<ServerDeploy> listServerDeploysByNameAndIp(Integer pageNo, String serverName, String serverIp);

    /**
     * 分页查询
     * @param pageParam
     * @return
     */
    PageBean query(PageParam pageParam);

    List<ServerDeploy> queryAll();

    PageBean query(Integer pageNo, String serverName, String serverIp);

    /**
     * 卸载服务
     * @param id
     * @return
     */
    void unInstall(Integer id);

    /**
     * 保存操作
     */
    Integer saveOrUpdateWithDeploy(ServerDeploy serverDeploy);

    /**
     * 删除操作
     */
    Integer delete(Integer id);
    /**
     * 重新发布
     * @param id
     */
    void reDeployWithUpdate(Integer id);

//    void modifyDeploy(ServerDeploy deploy);

    List<ServerDeploy> query(ServerDeploy serverDeploy);

    int updateDeployStatus(Integer id);

    int recoverDeployStatus(Integer id);

    String getDeployMessage(String id);

    String getExecMessage(Integer id);
}
