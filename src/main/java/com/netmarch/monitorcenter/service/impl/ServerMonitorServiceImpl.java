package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.bean.ServerDeploy;
import com.netmarch.monitorcenter.mapper.ServerDeployMapper;
import com.netmarch.monitorcenter.service.ServerInfoService;
import com.netmarch.monitorcenter.service.ServerMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author:xieqiang
 * @Date:2018/12/15
 */
@Service
@Slf4j
public class ServerMonitorServiceImpl implements ServerMonitorService {
    @Autowired
    private ServerDeployMapper serverDeployMapper;

    @Autowired
    private ServerInfoService serverInfoService;

    @Override
    public String checkServerStatus(Integer id) {
        // 判断是否能正常连接
        boolean result = serverInfoService.checkServiceStatus(id);
        ServerDeploy serverDeploy = serverDeployMapper.queryServerDeployById(id);
        if (result) {
            if (serverDeploy.getStatus() == 2) {
                return serverDeployMapper.recoverDeployStatus(id) > 0 ? "服务器状态恢复正常" : "服务器错误，请查看日志！";
            }else {
                return "服务器连接状态正常";
            }
        } else {
            if (serverDeploy.getStatus() == 1) {
                return serverDeployMapper.updateDeployStatus(id) > 0 ? "服务器状态异常！" : "服务器错误，请查看日志！";
            }else {
                return "服务器连接状态异常";
            }
        }
    }
}
