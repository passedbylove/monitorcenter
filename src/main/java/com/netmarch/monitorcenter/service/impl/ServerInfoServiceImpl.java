package com.netmarch.monitorcenter.service.impl;

import ch.ethz.ssh2.Connection;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.PagesStatic;
import com.netmarch.monitorcenter.bean.ServerInfo;
import com.netmarch.monitorcenter.exception.RepeatException;
import com.netmarch.monitorcenter.mapper.ServerInfoMapper;
import com.netmarch.monitorcenter.service.ServerInfoService;
import com.netmarch.monitorcenter.util.ConnectionUtil;
import com.netmarch.monitorcenter.util.RemoteConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author:xieqiang
 * @Date:2018/12/12
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ServerInfoServiceImpl implements ServerInfoService {
    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Override
    public List<ServerInfo> queryServerInfoByConditions(ServerInfo serverInfo, Integer page) {
        PageHelper.startPage(page, PagesStatic.PAGES_SIZE);
        return serverInfoMapper.queryServerInfoByConditions(serverInfo);
    }

    @Override
    public boolean insertServerInfo(ServerInfo serverInfo) throws RepeatException, IOException {
        boolean result = serverInfoMapper.checkRepeat(serverInfo.getIpAddress()) > 0;
        if (result) {
            throw new RepeatException("该服务器IP地址已存在");
        }
        ServerInfo info = getServerInfo(serverInfo);
        return serverInfoMapper.insertSelective(info) > 0;
    }

    @Override
    public boolean updateServerInfo(ServerInfo serverInfo) {
        return serverInfoMapper.updateServerInfo(serverInfo) > 0;
    }

    @Override
    public boolean deleteServerInfo(Integer id) {
        return serverInfoMapper.deleteServerInfoById(id) > 0;
    }

    @Override
    public ServerInfo queryServerInfoById(Integer id) {
        return serverInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean checkServiceStatus(Integer id) {
        ServerInfo serverInfo = queryServerInfoById(id);
        try {
            Boolean testResult = ConnectionUtil.serverConnectionTest(serverInfo.getIpAddress(), serverInfo.getLinkPort(), serverInfo.getUserName(), serverInfo.getPwd());
            // 测试连接正常，如果之前是异常状态，则恢复成正常
            if (testResult) {
                // 测试连接正常，重置服务器信息数据
                ServerInfo newInfo = getServerInfo(serverInfo);
                serverInfoMapper.updateByPrimaryKeySelective(newInfo);
                if (serverInfo.getStatus() != 0) {
                    serverInfoMapper.recoverServerInfoStatus(id);
                }
                return true;
            } else {
                // 测试连接异常，如果之前是正常状态，则改为异常
                if (serverInfo.getStatus() != 1) {
                    serverInfoMapper.updateServerInfoStatus(serverInfo.getId());
                }
                return false;
            }
        } catch (IOException e) {
            serverInfoMapper.updateServerInfoStatus(serverInfo.getId());
            log.error("测试连接失败！" + serverInfo.toString());
            return false;
        }
    }

    @Override
    public List<ServerInfo> queryAllServerInfo() {
        return serverInfoMapper.selectAll();
    }

    /**
     * 用于获取完整的ServerInfo对象
     */
    public static ServerInfo getServerInfo(ServerInfo serverInfo) throws IOException {
        Map<String, Object> cpuMap = null;
        String memoryLog = null;
        String systemVersion = null;
        List<Map<String, String>> netWorkInfo = null;
        List<String> networkNames = null;

        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverInfo.getIpAddress(), serverInfo.getUserName(), serverInfo.getPwd(), serverInfo.getLinkPort());
            if (RemoteConnect.fileExist(RemoteConnect.CPU_PATH, connect)) {
                cpuMap = RemoteConnect.readCPULog(connect);
            } else {
                throw new FileNotFoundException("找不到文件：" + RemoteConnect.CPU_PATH);
            }

            if (RemoteConnect.fileExist(RemoteConnect.MEMORY_PATH, connect)) {
                memoryLog = RemoteConnect.readMemoryLog(connect);
            } else {
                throw new FileNotFoundException("找不到文件：" + RemoteConnect.MEMORY_PATH);
            }

            if (RemoteConnect.fileExist(RemoteConnect.NETWORK_PATH, connect)) {
                networkNames = RemoteConnect.getNetworkNames(connect);
                netWorkInfo = RemoteConnect.getNetworkInfo(networkNames, connect);
            } else {
                throw new FileNotFoundException("找不到文件：" + RemoteConnect.NETWORK_PATH);
            }

            if (RemoteConnect.fileExist(RemoteConnect.SYSTEM_PATH, connect)) {
                systemVersion = RemoteConnect.readSystemVersionInfo(connect);
            } else {
                throw new FileNotFoundException("找不到文件：" + RemoteConnect.SYSTEM_PATH);
            }
        } finally {
            if (connect != null) {
                connect.close();
            }
        }

        Object cpuMHz = cpuMap.get(RemoteConnect.CPU_FILED_MHZ);
        Object cpuTotalCores = cpuMap.get(RemoteConnect.CPU_FILED_TOTAL_CORES);
        Object cpuName = cpuMap.get(RemoteConnect.CPU_FILED_NAME);

        // 赋值
        serverInfo.setMemory(memoryLog);
        serverInfo.setSysName(systemVersion);
        if (cpuMHz != null) {
            serverInfo.setCpuMaxHz(cpuMHz.toString());
        }
        if (cpuTotalCores != null) {
            serverInfo.setCpuTotalCares(cpuTotalCores.toString());
        }
        if (cpuName != null) {
            serverInfo.setCpuName(cpuName.toString());
        }
        serverInfo.setNetworkCardNum(networkNames.size());
        serverInfo.setNetworkInfo(JSON.toJSONString(netWorkInfo));
        return serverInfo;
    }
}
