package com.netmarch.monitorcenter.util;

import lombok.extern.log4j.Log4j2;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matchers;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 *
 */
@Log4j2
public class SSHManager {
    private static final Long JOIN_SECONDS = 5L;
    private static final Long WAIT_SECONDS = 1L;
    private static final String SHELL_PREFIX_REG =".*\\[.*@.*\\][\\$|\\#]";
    //    private static final String SHELL_ONE_LINE_REG ="[(\\r\\n)|(\\r)|(\\n)]+.*";
    private static final String SHELL_ONE_LINE_REG ="\\r\\n.*";
    private static final Pattern SHELL_PREFIX_PATTERN = Pattern.compile(SHELL_PREFIX_REG);
    private static final String EXECUTE_FAIL = "-1";
    /**
     * ssh 连接客户端
     */
    public static class SshClient {
        private Optional<String> host;
        private Optional<String> userName;
        private Optional<String> password;
        private Optional<SSHClient> sshjClient;
        private Optional<Session> session;
        private Optional<Expect> expect;
        private Optional<Consumer<String>> consumer;
        private SshClient(){
            this.host = Optional.empty();
            this.userName = Optional.empty();
            this.password = Optional.empty();
            this.sshjClient = Optional.empty();
            this.session = Optional.empty();
            this.expect = Optional.empty();
        }
        private void createSSHJClient() throws Exception{
            try {
                String host = this.host.orElseThrow(()-> new NullPointerException("host can't  be null"));
                String userName = this.userName.orElseThrow(()-> new NullPointerException("host can't  be null"));
                String password = this.password.orElseThrow(()-> new NullPointerException("host can't  be null"));
                this.sshjClient = Optional.of(new SSHClient());
                //不使用免密登录
                this.sshjClient.get().addHostKeyVerifier(
                        (s,i,publicKey) ->{
                            return true;
                        }
                );
                // 例：127.0.0.0:8080 获取端口
                int indexOfPort = host.indexOf(":");
                String port = null;
                if (indexOfPort!=-1){
                    String[] hostAndPort = host.split(":");
                    host = hostAndPort[0];
                    port = hostAndPort[1];
                }
                if (!StringUtils.isEmpty(port)){
                    this.sshjClient.get().connect(host,Integer.parseInt(port));
                }
                else {
                    this.sshjClient.get().connect(host);
                }
                this.sshjClient.get().authPassword(userName,password);
            } catch (Exception e) {
                HandleException(e);
            }
        }

        /**
         * 处理异常
         * 自定义异常抛出，非自定义异常打印
         * @param e
         * @throws Exception
         */
        private void HandleException(Exception e) throws Exception {
            close();
            throw e;
        }
        private SSHClient getSSHClient() throws Exception {
            if (!sshjClient.isPresent()){
                try {
                    createSSHJClient();
                } catch (Exception e) {
                    HandleException(e);
                }
            }
            return this.sshjClient.orElseThrow(()->new NullPointerException("sshjClient is null"));
        }
        private Session getSession() throws Exception {
            if (!this.session.isPresent()){
                this.session = Optional.of(getSSHClient().startSession());
            }
            return this.session.get();
        }
        private Expect getExpect() throws Exception {
            if (!this.expect.isPresent()){
                try {
                    Session session = getSession();
                    session.allocateDefaultPTY();
                    Session.Shell shell = session.startShell();
                    Expect expect = new ExpectBuilder()
                            .withOutput(shell.getOutputStream())
                            .withInputs(shell.getInputStream(), shell.getErrorStream())
                            .withEchoInput(System.out)
                            .withEchoOutput(System.err)
                            .withInputFilters(removeColors(), removeNonPrintable())
                            .withExceptionOnFailure()
                            .withTimeout(JOIN_SECONDS,TimeUnit.SECONDS)
                            .build();
                    this.expect = Optional.of(expect);
                } catch (Exception e) {
                    HandleException(e);
                }
            }
            return this.expect.get();
        }
        public SshClient host(String host) throws Exception {
            try {
                this.host = Optional.of(host);
            } catch (Exception e) {
                HandleException(e);
            }
            return this;
        }
        public SshClient addConsumer(Consumer<String> consumer){
            this.consumer = Optional.of(consumer);
            return  this;
        }
        public SshClient auth(String userName, String password) throws Exception {
            try {
                this.userName = Optional.of(userName);
                this.password = Optional.of(password);
                Expect expect = getExpect();
                expect.expect(Matchers.regexp(SHELL_ONE_LINE_REG)).group();
            } catch (Exception e) {
                HandleException(e);
            }
            return this;
        }
        public SshClient exec(String command) throws Exception {
            if ( this.consumer.isPresent()){
                return  exec(command,this.consumer.get());
            }
            else {
                return  exec(command,null,null);
            }
        }

        /**
         *
         * @param command
         * @param regexp 需要返回的消息 匹配的正则表达式
         * @return
         * @throws Exception
         */
        public SshClient exec(String command,String regexp) throws Exception {
            return exec(command,regexp,null);
        }
        private SshClient exec(@NotNull String command,@NotNull Consumer<String> consumer) throws Exception {
            Session session = null;
            try {
                Expect expect = getExpect();
                log.info("start execute command:["+command+"]");
                expect.sendLine(command);
                consumer.accept(command);
                boolean isContinue = false;
                do {
                    long waitTime = 100;
                    Thread.sleep(waitTime);
                    String res =  expect.withTimeout(WAIT_SECONDS,TimeUnit.SECONDS).expect(Matchers.regexp(SHELL_ONE_LINE_REG)).group();
                    Matcher matcher = SHELL_PREFIX_PATTERN.matcher(res);
                    isContinue = !matcher.find() || res.endsWith(command);
                    if (isContinue && !res.endsWith(command)){
                        log.info("res:"+res);
                        res = res.replaceAll("\\r\\n","");
                        consumer.accept(res);
                    }
                }while (isContinue);
                log.info(" execute command:["+command+"] is successful");
            } catch (Exception e) {
//                queue.offer(EXECUTE_FAIL);
                HandleException(e);
            }finally {
                if (session!=null){
                    session.close();
                }
            }
            return this;
        }
        /**
         *
         * @param command
         * @param consumer
         * @return
         * @throws Exception
         */
        public SshClient exec(String command,BiConsumer<String, SshClient> consumer) throws Exception {
            return exec(command,null,consumer);
        }
        /**
         *
         * @param command
         * @param regexp regexp 需要返回的消息 匹配的正则表达式
         * @param consumer 讲匹配的字符串返回
         * @return
         * @throws Exception
         */
        public SshClient exec(String command,String regexp,BiConsumer<String, SshClient> consumer) throws Exception {
            Session session = null;
            try {
                Expect expect = getExpect();
                log.info("start execute command:["+command+"]");
                expect.sendLine(command);
                //为空执行一个空命令防止当前命令未执行完成就关闭session
                String res = null;
                if (!StringUtils.isEmpty(regexp)){
                    Result result = expect.withTimeout(WAIT_SECONDS,TimeUnit.SECONDS).expect(regexp(regexp));
                    if (result.isSuccessful()){
                        res = result.group();
                    }
                }else {
                    expect.expect(regexp(command));
                    Result result = expect.withTimeout(WAIT_SECONDS,TimeUnit.SECONDS).expect(regexp(SHELL_PREFIX_REG));
                    if (result.isSuccessful()){
                        res = result.getBefore();
                    }
                }
                res.replaceAll("\\%","%%");
                log.info("response of exec :"+res);
                if (consumer!=null&&res!=null){
                    consumer.accept(res,this);
                }
                log.info(" execute command:["+command+"] is successful");
            } catch (Exception e) {
                HandleException(e);
            }finally {
                if (session!=null){
                    session.close();
                }
            }
            return this;
        }
        /**
         * 上传文件
         * @param src
         * @param dest
         * @throws Exception
         */
        public SshClient scpUpload(@NotNull String src, @NotNull  String dest) throws Exception {
            try {
                SSHClient ssh = getSSHClient();
                FileSystemFile fileSystemFile = new FileSystemFile(src);
                SCPFileTransfer scpFileTransfer= ssh.newSCPFileTransfer();
                log.info("scp -r "+src+" "+this.userName.get()+"@"+this.host.get()+dest);
                scpFileTransfer.upload(fileSystemFile, dest);
            } catch (Exception e) {
                HandleException(e);
            }
            return this;
        }

        /**
         * 关闭查询
         */
        public void close(){
            try {
                int millisOfSencond = 1000;//1000毫秒等于一秒
                //等一秒防止未执行成功
                Thread.sleep(WAIT_SECONDS*millisOfSencond);
                this.expect.ifPresent(expect1 -> {
                    try {
                        expect1.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                });
                this.expect = Optional.empty();
                this.session.ifPresent(session1 -> {
                    try {
                        session1.close();
                    } catch (TransportException e) {
                        log.error(e);
                    } catch (ConnectionException e) {
                        log.error(e);
                    }
                });
                this.session = Optional.empty();
                this.sshjClient.ifPresent(sshClient -> {
                    try {
                        sshClient.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                });
                this.sshjClient = Optional.empty();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
    public static SshClient build(){
        return new SshClient();
    }
    public static void main(String[] args) {
//        String hostName = "192.168.16.133";
//        String username = "root";
//        String password = "itcast";
//        String strl = connectSSH("df -h", false, hostName, username, password);
        try {
            SSHManager.build()
                    .host("192.168.99.104")
                    .auth("root", "root123")
                    .exec("cd /home")
                    .exec("ls",(res,cleint)->{
                        System.out.printf(res);
                    })
                    .close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
