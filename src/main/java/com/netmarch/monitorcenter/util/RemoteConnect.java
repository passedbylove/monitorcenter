package com.netmarch.monitorcenter.util;

import ch.ethz.ssh2.*;
import net.schmizz.sshj.common.IOUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteConnect {
    public static final String MEMOREY_FILED_TOTAL = "MemTotal";
    public static final String CPU_FILED_NAME = "model name";
    public static final String CPU_FILED_MHZ = "cpu MHz";
    public static final String CPU_FILED_TOTAL_CORES = "cpuTotalCores";

    //  ######################   Path  ###################
    public static final String MEMORY_PATH = "/proc/meminfo";
    public static final String CPU_PATH = "/proc/cpuinfo";
    public static final String NETWORK_PATH = "/proc/net/dev";
    public static final String SYSTEM_PATH = "/etc/redhat-release";


    public static void main(String[] args) {
        // 示例
//        String hostName = "192.168.16.134";
//        int port = 22;
//        String username = "root";
//        String password = "123456";
//        Connection connect = null;
//        try {
//            connect = getConnect(hostName, username, password, port);
//        } catch (IOException e) {
//            throw e;
//        }
//

    }


    /**
     * 远程执行Linux命令，返回一个流对象，获取执行相应内容
     */
    public static String executeCommand(Connection connect, String command) throws IOException {
        BufferedReader bufferedReader = null;
        String result = "";
        if (connect != null) {
            Session ss = null;
            try {
                ss = connect.openSession();
                ss.execCommand(command);
                InputStream inputStream = new StreamGobbler(ss.getStdout());
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(bufferedReader);
                if (!BeanUtil.isEmpty(ss)) {
                    ss.close();
                }
            }
        } else {
            throw new NullPointerException("Connection 为空！");
        }
        return result;
    }

    public static String readSystemVersionInfo(Connection conn) throws IOException {
        String result = null;
        if (conn != null) {
            Session ss = null;
            try {
                ss = conn.openSession();
                ss.execCommand("tail -100 ".concat(RemoteConnect.SYSTEM_PATH));
                InputStream is = new StreamGobbler(ss.getStdout());
                BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                result = brs.readLine();
                brs.close();
            } catch (Exception e) {
                throw e;
            } finally {
                if (ss != null) {
                    ss.close();
                }
            }
        }
        return result;
    }


    public static String readMemoryLog(Connection conn) throws IOException {
        String result = null;
        if (conn != null) {
            Session ss = null;
            BufferedReader brs = null;
            InputStream is = null;
            try {
                ss = conn.openSession();
                ss.execCommand("tail -100 ".concat(RemoteConnect.MEMORY_PATH));
                is = new StreamGobbler(ss.getStdout());
                brs = new BufferedReader(new InputStreamReader(is));
                while (true) {
                    String line = brs.readLine();
                    if (line == null) {
                        break;
                    } else {
                        if (line.startsWith(MEMOREY_FILED_TOTAL)) {
                            String value = line.split(":")[1].trim();
                            if (value.length() < 6) {
                                result = value;
                            }
                            String temp = value.substring(0, value.length() - 6);
                            if (temp.length() > 3) {
                                result = RemoteConnect.getMeorySize(temp);
                            } else {
                                result = temp.concat(" MB");
                            }
                            break;
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(brs);
                if (ss != null) {
                    ss.close();
                }
            }
        }
        return result;
    }

    private static String getMeorySize(String temp) {
        return (temp.substring(0, temp.length() - 3).concat(".") + temp.charAt(temp.length() - 3)).concat(" GB");
    }

    public static List<String> getNetworkNames(Connection conn) throws IOException {
        List<String> networkList = new ArrayList<>();
        int index = 0;
        if (conn != null) {
            InputStream is = null;
            BufferedReader brs = null;
            try {
                Session ss = conn.openSession();
                // 拉出一共有多少网卡
                ss.execCommand("tail -100 ".concat(NETWORK_PATH));
                is = new StreamGobbler(ss.getStdout());
                brs = new BufferedReader(new InputStreamReader(is));
                while (true) {
                    String line = brs.readLine();
                    if (line == null) {
                        break;
                    } else {
                        index++;
                        if (index >= 3) {
                            networkList.add(line.split(":")[0].trim());
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(is, brs);
            }
        }
        return networkList;
    }

    /**
     * @param networkNames 网卡名称
     * @param conn 连接对象
     * @return 返回网卡名称——对应网卡带宽的集合
     * @throws IOException
     */
    public static List<Map<String, String>> getNetworkInfo(List<String> networkNames, Connection conn) throws IOException {
        List<Map<String, String>> resultMap = new ArrayList<>();
        Session ss = null;
        InputStream bandwidthIS = null;
        BufferedReader bandwidthBrs = null;
        try {
            for (String name : networkNames) {
                ss = conn.openSession();
                HashMap<String, String> map = new HashMap<>();
                String command = "ethtool ".concat(name).concat(" | grep Speed");
                ss.execCommand(command);
                bandwidthIS = new StreamGobbler(ss.getStdout());
                bandwidthBrs = new BufferedReader(new InputStreamReader(bandwidthIS));
                String line = bandwidthBrs.readLine();
                if (!StringUtils.isEmpty(line)) {
                    String[] kv = line.split(":");
                    map.put(name, kv[1].trim());
                }
                if (map.size() != 0) {
                    resultMap.add(map);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (ss != null) {
                ss.close();
            }
            IOUtils.closeQuietly(bandwidthBrs, bandwidthIS);
        }
        return resultMap;
    }

    /**
     * @param conn 连接对象
     * @return 返回cpu信息Map
     * @throws IOException
     */
    public static Map<String, Object> readCPULog(Connection conn) throws IOException {
        Map<String, Object> cpuMap = new HashMap<>(16);
        if (conn != null) {
            Session ss = null;
            try {
                ss = conn.openSession();
                ss.execCommand("tail -100 ".concat(CPU_PATH));
                InputStream returnInputStream = new StreamGobbler(ss.getStdout());
                BufferedReader returnBufferedReader = new BufferedReader(new InputStreamReader(returnInputStream));
                while (true) {
                    String line = returnBufferedReader.readLine();
                    if (line == null) {
                        break;
                    } else {
                        // 有没有CPU名称
                        if (cpuMap.get(CPU_FILED_NAME) == null && line.startsWith(CPU_FILED_NAME)) {
                            cpuMap.put(CPU_FILED_NAME, line.split(":")[1].trim());
                            // 有没有cpu最高Hz
                        } else if (cpuMap.get(CPU_FILED_MHZ) == null && line.startsWith(CPU_FILED_MHZ)) {
                            cpuMap.put(CPU_FILED_MHZ, line.split(":")[1].trim());
                        }
                    }
                }
                ss = conn.openSession();
                ss.execCommand("lscpu | grep \"CPU(s):\"");
                InputStream coresInputStream = new StreamGobbler(ss.getStdout());
                BufferedReader coresBufferedReader = new BufferedReader(new InputStreamReader(coresInputStream));
                String cpus = coresBufferedReader.readLine();
                cpuMap.put(CPU_FILED_TOTAL_CORES, cpus.split(":")[1].trim());
                returnBufferedReader.close();
                coresInputStream.close();
                coresBufferedReader.close();
            } catch (Exception e) {
                throw e;
            } finally {
                if (ss != null) {
                    ss.close();
                }
            }
        }
        return cpuMap;
    }

    public static Connection getConnect(String hostName, String username, String password, int port) throws IllegalArgumentException, IOException {
        Connection conn = new Connection(hostName, port);
        try {
            // 连接到主机
            conn.connect();
            // 使用用户名和密码校验
            boolean isconn = conn.authenticateWithPassword(username, password);
            if (!isconn) {
                throw new IllegalArgumentException("连接账号或密码不正确");
            } else {
                return conn;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static boolean fileExist(String path, Connection conn) throws IOException {
        if (conn != null) {
            Session ss = null;
            try {
                ss = conn.openSession();
                ss.execCommand("ls -l ".concat(path));
                InputStream is = new StreamGobbler(ss.getStdout());
                BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                String line = "";
                while (true) {
                    String lineInfo = brs.readLine();
                    if (lineInfo != null) {
                        line = line + lineInfo;
                    } else {
                        break;
                    }
                }
                brs.close();
                if (line != null && line.length() > 0) {
                    return true;
                }
            } catch (Exception e) {
                throw e;
            } finally {
                // 连接的Session和Connection对象都需要关闭
                if (ss != null) {
                    ss.close();
                }
            }
        }
        return false;
    }

    /**
     * 执行脚本
     *
     * @param connection
     * @param cmd
     * @throws Exception
     */
    public static void executeShell(Connection connection, String cmd) throws IOException {
        if (!BeanUtil.isEmpty(connection)) {
            Session session = connection.openSession();
            try {
                session.requestPTY("bash");
                session.startShell();
                PrintWriter pw = new PrintWriter(session.getStdin());
                pw.println(cmd);
                pw.println("exit");
                pw.close();
                session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EXIT_STATUS | ChannelCondition.EOF, 10000);
            } finally {
                session.close();
            }
        }
    }

    /**
     * 上传文件
     *
     * @param connection
     * @param sourceFile
     * @param destDirectory
     * @throws Exception
     */
    public static void uploadFile(Connection connection, String sourceFile, String destDirectory) throws Exception {
        if (!BeanUtil.isEmpty(connection)) {
            SCPClient scpClient = connection.createSCPClient();
            scpClient.put(sourceFile, destDirectory);
        }
    }
}
