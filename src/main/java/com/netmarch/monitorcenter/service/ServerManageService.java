package com.netmarch.monitorcenter.service;

import java.io.IOException;

/**
 * @Author:Administrator
 * @Date:2018/12/29
 */
public interface ServerManageService {
    boolean startServer(Integer id) throws IOException;

    boolean stopServer(Integer id) throws IOException;

    boolean restart(Integer id) throws IOException;

    boolean checkStatus(Integer id) throws IOException;

    void openFireWall(Integer id) throws IOException;
}
