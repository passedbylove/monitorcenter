package com.netmarch.monitorcenter.service.impl;

import ch.ethz.ssh2.Connection;
import com.netmarch.monitorcenter.bean.ServerManage;
import com.netmarch.monitorcenter.mapper.ServerManageMapper;
import com.netmarch.monitorcenter.service.ServerManageService;
import com.netmarch.monitorcenter.service.deploy.DeployExecutor;
import com.netmarch.monitorcenter.util.RemoteConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;

/**
 * @Author:xieqiang
 * @Date:2018/12/29
 */
@Service
@Slf4j
public class ServerManageServiceImpl implements ServerManageService {
    public static final int START_TYPE = 1;
    public static final int STOP_TYPE = 2;
    @Autowired
    private ServerManageMapper serverManageMapper;

    @Override
    public boolean startServer(Integer id) throws IOException {
        ServerManage serverManage = serverManageMapper.selectDeployAndGzAndInfoByDeployId(id);
        String gzName = getGzName(serverManage.getGzUrl());
        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverManage.getIpAddress(), serverManage.getUsername(), serverManage.getPassword(), serverManage.getLinkPort());
            String command = "cd " + serverManage.getDeployUrl() + " && ./" + DeployExecutor.SYS_DEFAULT_START + ".sh " + gzName + " " + serverManage.getDeployPort();
            RemoteConnect.executeShell(connect, command);
            return checkServerRunStatus(connect, gzName, START_TYPE, serverManage.getDeployUrl()) == START_TYPE
                    && serverManageMapper.changeRunStatus(id, START_TYPE) > 0;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    @Override
    public boolean stopServer(Integer id) throws IOException {
        ServerManage serverManage = serverManageMapper.selectDeployAndGzAndInfoByDeployId(id);
        String gzName = getGzName(serverManage.getGzUrl());
        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverManage.getIpAddress(), serverManage.getUsername(), serverManage.getPassword(), serverManage.getLinkPort());
            String command = "cd " + serverManage.getDeployUrl() + " && ./" + DeployExecutor.SYS_DEFAULT_STOP + ".sh " + gzName;
            RemoteConnect.executeShell(connect, command);
            return checkServerRunStatus(connect, gzName, STOP_TYPE, serverManage.getDeployUrl()) == STOP_TYPE
                    && serverManageMapper.changeRunStatus(id, STOP_TYPE) > 0;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    @Override
    public boolean restart(Integer id) throws IOException {
        ServerManage serverManage = serverManageMapper.selectDeployAndGzAndInfoByDeployId(id);
        String gzName = getGzName(serverManage.getGzUrl());
        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverManage.getIpAddress(), serverManage.getUsername(), serverManage.getPassword(), serverManage.getLinkPort());
            String stop = "cd " + serverManage.getDeployUrl() + " && ./" + DeployExecutor.SYS_DEFAULT_STOP + ".sh " + gzName;
            String start = "cd " + serverManage.getDeployUrl() + " && ./" + DeployExecutor.SYS_DEFAULT_START + ".sh " + gzName + " " + serverManage.getDeployPort();
            RemoteConnect.executeShell(connect, stop);
            RemoteConnect.executeShell(connect, start);
            return checkServerRunStatus(connect, gzName, START_TYPE, serverManage.getDeployUrl()) == START_TYPE
                    && serverManageMapper.changeRunStatus(id, START_TYPE) > 0;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    @Override
    public boolean checkStatus(Integer id) throws IOException {
        ServerManage serverManage = serverManageMapper.selectDeployAndGzAndInfoByDeployId(id);
        String gzName = getGzName(serverManage.getGzUrl());
        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverManage.getIpAddress(), serverManage.getUsername(), serverManage.getPassword(), serverManage.getLinkPort());
            int result = checkServerRunStatus(connect, gzName, START_TYPE, serverManage.getDeployUrl());
            if (serverManage.getRunStatus() == result) {
                // 确认服务状态，再确认数据库状态是否一致，一致直接返回；不一致，更改状态
                return true;
            } else {
                serverManageMapper.changeRunStatus(id, result);
            }
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
        return false;
    }

    @Override
    public void openFireWall(Integer id) throws IOException {
        ServerManage serverManage = serverManageMapper.selectDeployAndGzAndInfoByDeployId(id);
        Connection connect = null;
        try {
            connect = RemoteConnect.getConnect(serverManage.getIpAddress(), serverManage.getUsername(), serverManage.getPassword(), serverManage.getLinkPort());
            RemoteConnect.executeShell(connect, "firewall-cmd --zone=public --add-port=" + serverManage.getDeployPort() + "/tcp --permanent");
            RemoteConnect.executeShell(connect, "/sbin/iptables -I INPUT -p tcp --dport " + serverManage.getDeployPort() + " -j ACCEPT");
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    /**
     * 用户获取安装包缩写名称
     *
     * @param gzUrl 安装包路径
     * @return
     */
    private String getGzName(String gzUrl) {
        if (!StringUtils.isEmpty(gzUrl)) {
            return gzUrl.substring(gzUrl.lastIndexOf("/") + 1, gzUrl.length() - 4);
        } else {
            throw new NullPointerException("安装包路径不能为空");
        }
    }

    private int checkServerRunStatus(Connection connection, String gzName, Integer checkType, String deployUrl) throws IOException {
        String pidCommand = "cat " + deployUrl + "/" + gzName + ".pid";
        String pid = RemoteConnect.executeCommand(connection, pidCommand);
        String check = "ps aux  | awk '{print $2}' | grep " + pid;
        String exist = RemoteConnect.executeCommand(connection, check);
        boolean result = false;
        switch (checkType) {
            case START_TYPE:
                result = !StringUtils.isEmpty(exist) && exist.equals(pid);
                break;
            case STOP_TYPE:
                result = !StringUtils.isEmpty(exist);
                break;
            default:
        }
        return result ? START_TYPE : STOP_TYPE;
    }

}
