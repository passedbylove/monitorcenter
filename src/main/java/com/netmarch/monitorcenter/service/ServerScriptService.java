package com.netmarch.monitorcenter.service;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.ServerGz;
import com.netmarch.monitorcenter.bean.ServerInfo;
import com.netmarch.monitorcenter.bean.ServerScript;

import java.util.List;

public interface ServerScriptService {

    /**
     * 分页条件查询
     * @param serverScript
     * @param pageNo
     * @return
     */
    Page<ServerScript> selectByCondition(ServerScript serverScript, Integer pageNo);

    /**
     * 删除通过id
     * @param id
     * @return
     */
    int deleteById(Integer id);

    /**
     * 通过id获取
     * @param id
     * @return
     */
    ServerScript getById(Integer id);

    /**
     * 插入脚本
     * @param serverScript
     * @return
     */
    int insert(ServerScript serverScript);

    /**
     * 编辑
     * @param serverScript
     * @return
     */
    int update(ServerScript serverScript);

    /**
     * 获取脚本
     * @return
     */
    List<ServerScript> getAll();

    /**
     * 获取安装包
     * @return
     */
    List<ServerGz> getAllGz();

    /**
     * 获取所有服务器
     * @return
     */
    List<ServerInfo> getAllServer();
}
