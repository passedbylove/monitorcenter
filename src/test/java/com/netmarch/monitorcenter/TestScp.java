package com.netmarch.monitorcenter;

import ch.ethz.ssh2.*;
import com.netmarch.monitorcenter.bean.StaticObj;
import com.netmarch.monitorcenter.util.BeanUtil;
import com.netmarch.monitorcenter.util.RemoteConnect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * @program: monitorcenter
 * @description: testScp
 * @author: dinggan
 * @create: 2018-12-17 16:14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestScp {

    @Test
    public void test() throws IOException {
        Connection connection = RemoteConnect.getConnect("172.16.3.116", "root", "123456", 22);
        try {
            if(!BeanUtil.isEmpty(connection)){
                Session session = connection.openSession();
                session.requestPTY("bash");
                session.startShell();
                PrintWriter pw = new PrintWriter(session.getStdin());
                pw.println("if [ ! -d \"/script/\" ];then\n" +
                        "mkdir /script fi");
                pw.println("exit");
                pw.close();
                session.waitForCondition(ChannelCondition.CLOSED|ChannelCondition.EXIT_STATUS|ChannelCondition.EOF,30000);
                BufferedReader brs = new BufferedReader(new InputStreamReader(session.getStdout()));
                String line = "";
                while (true) {
                    String lineInfo = brs.readLine();
                    if (lineInfo != null) {
                        line = line + lineInfo;
                    } else {
                        break;
                    }
                }

                SCPClient scpClient = connection.createSCPClient();
                scpClient.put("D:\\Desktop\\jdk\\install-jdk.sh", StaticObj.SCRIPT_PATH);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
