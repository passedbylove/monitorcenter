package com.netmarch.monitorcenter.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 连接工具类
 *
 * @Author:xieqiang
 * @Date:2018/12/12
 */
public class ConnectionUtil {
    /**
     * 用于测试数据库连接是否正常
     *
     * @return 返回true则连接正常
     */
    public Boolean dataBaseConnectionTest(String url, String driverClassName, String username, String password) throws SQLException, ClassNotFoundException {
        Class.forName(driverClassName);
        Connection connection = DriverManager.getConnection(url, username, password);
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 用于测试与服务器连接是否正常
     *
     * @return 返回true则连接正常
     */
    public static Boolean serverConnectionTest(String ip, Integer port, String username, String password) throws IOException {
        ch.ethz.ssh2.Connection conn = new ch.ethz.ssh2.Connection(ip, port);
        try {
            // 连接到主机
            conn.connect();
            // 使用用户名和密码校验
            boolean isconn = conn.authenticateWithPassword(username, password);
            if (!isconn) {
                System.out.println("用户名称或密码不正确，连接失败");
            } else {
                System.out.println("服务器连接成功.");
                return true;
            }
        } catch (Exception e) {
            throw e;
        }finally {
            conn.close();
        }
        return false;
    }

    public static void main(String[] args) {
        String path = "/proc/meminfo";
        String hostName = "192.168.16.133";
        int port = 22;
        String username = "root";
        String password = "itcast";
        try {
            Boolean result = serverConnectionTest(hostName, port, username, password);
            System.out.println("连接结果：" + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
