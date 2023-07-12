package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.ServerInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @Author:xieqiang
 * @Date:2018/12/12
 */
public interface ServerInfoService {
    List<ServerInfo> queryServerInfoByConditions(ServerInfo serverInfo, Integer page);

    boolean insertServerInfo(ServerInfo serverInfo) throws IOException;

    boolean updateServerInfo(ServerInfo serverInfo);

    boolean deleteServerInfo(Integer serverInfo);

    ServerInfo queryServerInfoById(Integer id);

    boolean checkServiceStatus(Integer id);

    List<ServerInfo> queryAllServerInfo();
}
