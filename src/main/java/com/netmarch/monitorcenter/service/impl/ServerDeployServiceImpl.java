package com.netmarch.monitorcenter.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.mapper.ServerDeployMapper;
import com.netmarch.monitorcenter.mapper.ServerInfoMapper;
import com.netmarch.monitorcenter.service.ServerDeployService;
import com.netmarch.monitorcenter.service.common.MessageQueueMarker;
import com.netmarch.monitorcenter.service.deploy.DeployExecutor;
import com.netmarch.monitorcenter.util.BeanMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * @Author: lining
 * @Description: ServerDeployServiceImpl
 */
@Service
@Log4j2
public class ServerDeployServiceImpl implements ServerDeployService {
    @Autowired
    private ServerDeployMapper serverDeployMapper;

    @Autowired
    private ServerInfoMapper serverInfoMapper;
    @Autowired
    DeployExecutor deployExecutor;

    @Override
    public Page<ServerDeploy> listServerDeploysByNameAndIp(Integer pageNo, String serverName, String serverIp) {
        PageHelper.startPage(pageNo == null ? 1 : pageNo, PagesStatic.PAGES_SIZE);
        return serverDeployMapper.listServerDeploysByNameAndIp(serverName, serverIp);
    }
    /**
     * 分页查询实现
     * @param pageParam
     * @return
     */
    @Override
    public PageBean query(PageParam pageParam) {
        List data = null;
        try {

            Example example = new Example(ServerDeploy.class);
            BeanMap params = pageParam.getParams();
            String searchValue = params.getString("searchValue");
            if (!StringUtils.isEmpty(searchValue)) {
                searchValue = "%" + searchValue + "%";
                example.or().orLike("serverIp", searchValue);
                example.or().orLike("serverName", searchValue);
            }
            example.orderBy("deployTime").desc();
            PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize());
            data = serverDeployMapper.selectRelationByExample(example);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageBean<>(data);
    }

    /**
     * 获取所有服务发布
     * @return
     */
    @Override
    public List<ServerDeploy> queryAll() {
        return serverDeployMapper.selectRelationByExample(null);
    }

    @Override
    public PageBean query(Integer pageNo, String serverName, String serverIp) {
        List data = null;
        try {
            Example example = new Example(ServerDeploy.class);
            if (!StringUtils.isEmpty(serverIp)) {
                example.or().orLike("serverIp", serverIp);
            }

            if (!StringUtils.isEmpty(serverIp)) {
                example.or().orLike("serverName", serverName);
            }
            example.orderBy("deployTime").desc();
            if (pageNo == null) {
                pageNo = 1;
            }
            PageHelper.startPage(pageNo, PagesStatic.PAGES_SIZE);
            data = serverDeployMapper.selectRelationByExample(example);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageBean<>(data);
    }

    @Override
    public void unInstall(Integer id) {
        deployExecutor.asyncUnInstall(id);
    }


    /**
     * 数据库存储和远程上传行为一起完成
     * @param serverDeploy
     * @return
     */
    @Override
    public Integer saveOrUpdateWithDeploy(@NotNull ServerDeploy serverDeploy) {
        if (serverDeploy.getId() != null) {
            int result = serverDeployMapper.updateByPrimaryKeySelective(serverDeploy);
            reDeployWithUpdate(serverDeploy.getId());
            return result;
        } else {
            serverDeploy.setRunStatus(StaticObj.IS_NOT_RUNNING);
            serverDeploy.setCreateTime(new Date(System.currentTimeMillis()));
            serverDeploy.setServerIp(serverInfoMapper.selectIpAddressById(serverDeploy.getServerId()));
            serverDeployMapper.insertSelective(serverDeploy);
            deployExecutor.asyncDeploy(serverDeploy);
            return serverDeploy.getId();
        }
    }


    @Override
    public Integer delete(Integer id) {
        return serverDeployMapper.deleteByPrimaryKey(id);
    }

    /**
     * 重新部署
     * @param id
     */
    @Override
    public void reDeployWithUpdate(Integer id) {
        ServerDeploy serverDeploy = serverDeployMapper.selectByPrimaryKey(id);
        deployExecutor.unInstallAndDeploy(serverDeploy);
//        //更新状态
//        serverDeployMapper.updateByPrimaryKeySelective(serverDeploy);
    }

    /**
     * 部署更新
     * @param deploy
     */
//    @Override
//    public void modifyDeploy(ServerDeploy deploy) {
//        serverDeployMapper.updateByPrimaryKeySelective(deploy);
//        reDeployWithUpdate(deploy.getId());
//    }

    /**
     * 查询ServerDeploy
     */
    @Override
    public List<ServerDeploy> query(ServerDeploy serverDeploy) {
        return serverDeployMapper.select(serverDeploy);
    }

    @Override
    public int updateDeployStatus(Integer id) {
        return serverDeployMapper.updateDeployStatus(id);
    }

    @Override
    public int recoverDeployStatus(Integer id) {
        return serverDeployMapper.recoverDeployStatus(id);
    }
    @Override
    public String getDeployMessage(String id) {
        return deployExecutor.getDeployMessage(id);
    }
    private static  final  String execQueueIsNull = "exit_null";
    @Override
    public String getExecMessage(Integer id){
        Queue<String> queue = MessageQueueMarker.obtain(DeployExecutor.MESSAGE_QUEUE_PREFIX+id.toString());

        if (queue!=null){
           StringBuilder msg = new StringBuilder();
           while (!queue.isEmpty()){
               msg.append("\n");
               msg.append(queue.poll());
           }
           return msg.toString();
        }
        return execQueueIsNull;
    }
}
