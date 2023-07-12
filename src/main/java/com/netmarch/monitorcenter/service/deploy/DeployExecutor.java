package com.netmarch.monitorcenter.service.deploy;

import com.netmarch.monitorcenter.bean.ServerDeploy;
import com.netmarch.monitorcenter.bean.ServerGz;
import com.netmarch.monitorcenter.bean.ServerInfo;
import com.netmarch.monitorcenter.bean.ServerScript;
import com.netmarch.monitorcenter.mapper.ServerDeployMapper;
import com.netmarch.monitorcenter.mapper.ServerGzMapper;
import com.netmarch.monitorcenter.mapper.ServerInfoMapper;
import com.netmarch.monitorcenter.mapper.ServerScriptMapper;
import com.netmarch.monitorcenter.service.common.MessageQueueMarker;
import com.netmarch.monitorcenter.util.SSHManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Queue;

@Component
@Log4j2
@EnableAsync
public class DeployExecutor {
    public static final String SYS_DEFAULT_START = "sys-default-start";
    public static final String SYS_DEFAULT_STOP = "sys-default-stop";
    private static final Integer DEPLOY_UNINSTALL_SUCCESS = 3;
    private static final Integer DEPLOY_UNINSTALL_FAIL = 2;
    private static final Integer DEPLOY_SUCCESS = 1;
    private static final String DEPLOY_SUCCESS_MSG = "exit_ok";
    private static final Integer DEPLOY_FAIL = 0;
    private static final String DEPLOY_FAIL_MSG = "exit_no";
    public static final String MESSAGE_QUEUE_PREFIX = "deploy_message_";
    @Autowired
    private ServerScriptMapper serverScriptMapper;
    @Autowired
    private ServerGzMapper serverGzMapper;
    @Autowired
    private ServerDeployMapper serverDeployMapper;

    @Autowired
    private ServerInfoMapper serverInfoMapper;
    private void unInstall(Integer id) throws Exception {
        ServerDeploy serverDeploy = serverDeployMapper.selectByPrimaryKey(id);
        serverDeploy.setDeployTime(new Date(System.currentTimeMillis()));
        SSHManager.SshClient sshClient = null;
        Queue<String> messageQueue = MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+serverDeploy.getId().toString());
        String message = "[" + serverDeploy.getServerName() + "] 在 [" + serverDeploy.getServerIp() + "]服务器 卸载工作开始";
        try {
            log.info(message);
            messageQueue.offer(message);
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setIpAddress(serverDeploy.getServerIp());
            serverInfo = serverInfoMapper.selectOne(serverInfo);
            sshClient = SSHManager.build()
                    .host(serverInfo.getIpAddress() + ":" + serverInfo.getLinkPort())
                    .auth(serverInfo.getUserName(), serverInfo.getPwd())
                    .addConsumer(res->messageQueue.offer(res));
            ServerScript uninstallScript = serverScriptMapper.selectByPrimaryKey(serverDeploy.getUninstallScriptId());
            if (uninstallScript!=null){
                String deployPath = serverDeploy.getDeployPath();
                String uninstallSh = getFileName(uninstallScript.getScriptUrl(),true);
                sshClient
                        .exec("cd "+deployPath)
                        .exec("./"+uninstallSh);
            }
            //删除文件夹
            String removeCommand = "rm -rf " + "/" + serverDeploy.getDeployPath();
            sshClient.exec(removeCommand)
                    .close();
            serverDeploy.setStatus(DEPLOY_UNINSTALL_SUCCESS);
            message = "[" + serverDeploy.getServerName() + "] 在 [" + serverDeploy.getServerIp() + "]服务器 卸载工作成功";
            log.info(message);
            messageQueue.offer(message);
//            messageQueue.offer(DEPLOY_SUCCESS_MSG);
        } catch (Exception e) {
            serverDeploy.setStatus(DEPLOY_UNINSTALL_FAIL);
            message = "[" + serverDeploy.getServerName() + "] 在 [" + serverDeploy.getServerIp() + "]服务器 卸载工作失败";
            log.info(message);
            messageQueue.offer(e.getMessage());
            messageQueue.offer(message);
            log.error(e);
            throw e;
        } finally {
            serverDeployMapper.updateByPrimaryKeySelective(serverDeploy);
            if (sshClient!=null){
                sshClient.close();
            }
        }
    }
    /**
     * 异步卸载部署服务
     * @param serverDeploy
     * @return
     */
    @Async
    @Deploy
    public void unInstallAndDeploy(ServerDeploy serverDeploy){
        Queue<String> messageQueue = MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+serverDeploy.getId().toString());
        try {
            unInstall(serverDeploy.getId());
            deploy(serverDeploy);
            messageQueue.offer(DEPLOY_SUCCESS_MSG);
        } catch (Exception e) {
            log.error(e);
            messageQueue.offer(DEPLOY_FAIL_MSG);

        }
    }
    /**
     * 异步卸载服务
     * @param id
     * @return
     */
    @Async
    @Deploy
    public void asyncUnInstall(Integer id) {
        Queue<String> messageQueue = MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+id.toString());
        try {
            unInstall(id);
            messageQueue.offer(DEPLOY_SUCCESS_MSG);
        } catch (Exception e) {
            messageQueue.offer(DEPLOY_FAIL_MSG);
            log.error(e);
        }
    }

    /**
     * 根据id获取脚本路径
     * @param id
     * @return
     */
    public String getScriptUrl(Integer id){
        if (id!=null) {
            return  serverScriptMapper.selectByPrimaryKey(id).getScriptUrl();
        }else {
            return  null;
        }
    }

    /**
     * 根据name获取脚本路径
     * @param name
     * @return
     */
    public String getScriptUrl(String name){
        ServerScript query = new ServerScript();
        query.setScriptName(name);
        ServerScript startScript = serverScriptMapper.selectOne(query);
        return startScript.getScriptUrl();
    }

    /**
     * 获取停止脚本url
     * @param serverDeploy
     * @return
     */
    public String getStopScriptUrl(ServerDeploy serverDeploy){
        if (serverDeploy.getStopScriptId() == null){
            return getScriptUrl(SYS_DEFAULT_STOP);
        }
        return getScriptUrl(serverDeploy.getStopScriptId());
    }

    /**
     * 获取启动脚本url
     * @param serverDeploy
     * @return
     */
    public String getStartScriptUrl(ServerDeploy serverDeploy){
        if (serverDeploy.getStartScriptId() == null){
            return getScriptUrl(SYS_DEFAULT_START);
        }
        return getScriptUrl(serverDeploy.getStartScriptId());
    }

    /**
     * 获取部署脚本url
     * @param serverDeploy
     * @return
     */
    public String getDeployScriptUrl(ServerDeploy serverDeploy){
        return getScriptUrl(serverDeploy.getDeployScriptId());
    }
    /**
     * 获取卸载脚本url
     * @param serverDeploy
     * @return
     */
    public String getUninstallScriptUrl(ServerDeploy serverDeploy){
        return getScriptUrl(serverDeploy.getUninstallScriptId());
    }

    /**
     * 部署服务
     * @param serverDeploy
     */
    private void deploy(ServerDeploy serverDeploy) throws Exception {
        Queue<String> messageQueue = MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+serverDeploy.getId().toString());
        String message = "[" + serverDeploy.getServerName() + "]部署到：[" + serverDeploy.getServerIp() + "]服务器 工作开始";
        log.info(message);
        messageQueue.offer(message);
        SSHManager.SshClient sshClient = null;
        try {
            serverDeploy.setDeployTime(new Date(System.currentTimeMillis()));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setIpAddress(serverDeploy.getServerIp());
            serverInfo = serverInfoMapper.selectOne(serverInfo);
            ServerGz serverGz = serverGzMapper.selectGzById(serverDeploy.getGzId());
            //默认上传路径
            String destPath = serverDeploy.getDeployPath();
            //获取jar名称
            String gzUrl = serverGz.getGzUrl();
            String jarName = getFileName(gzUrl, false);
            String jarNameWithSuffix = getFileName(gzUrl, true);
            if (StringUtils.isEmpty(jarName)) {
                throw new NullPointerException("jarName of serverGz is null");
            }
            //创建pid命令
            String mkDir = "mkdir -p %s";
            mkDir = String.format(mkDir, destPath);
            sshClient= SSHManager.build()
                    .host(serverInfo.getIpAddress() + ":" + serverInfo.getLinkPort())
                    .auth(serverInfo.getUserName(), serverInfo.getPwd())
                    .addConsumer(res->messageQueue.offer(res))
                    //创建文件夹
                    .exec(mkDir)
                    //上传jar包
                    .scpUpload(serverGz.getGzUrl(), destPath + "/" + jarNameWithSuffix)
                    //执行部署脚本之前，修改文档权限
                    .exec("chmod -R 777 " + destPath);
            //上传启动和停止脚本,服务类型安装包一定有
            if (ServerGz.SERVER_GROUP_CODE.equals(serverGz.getGroupCode())){
                String mkPidCommand = "touch %s/%s.pid";
                mkPidCommand = String.format(mkPidCommand, destPath, jarName);
                //上传启动脚本
                String startScriptUrl = getStartScriptUrl(serverDeploy);
                if (startScriptUrl == null) {
                    throw new NullPointerException("startScript is  null");
                }
                String startScriptName = getFileName(startScriptUrl, true);
                //获取停止脚本
                String stopScriptUrl = getStopScriptUrl(serverDeploy);
                if (stopScriptUrl == null) {
                    throw new NullPointerException("stopScript is  null");
                }
                String stopScriptName = getFileName(stopScriptUrl, true);
                sshClient
                        //创建pid文件
                        .exec(mkPidCommand)
                        //上传启动脚本
                        .scpUpload(startScriptUrl, destPath + "/" + startScriptName)
                        //修正脚本，防止"/bin/bash^M: 坏的解释器：没有那个文件或目录"错误
                        .exec("sed -i 's/\\r$//' " + destPath + "/" + startScriptName)
                        .exec("mv -f "+destPath + "/" + startScriptName+" "+ destPath +"/" + SYS_DEFAULT_START+".sh")
                        //停止脚本
                        .scpUpload(stopScriptUrl, destPath + "/" + stopScriptName)
                        //修正脚本，防止"/bin/bash^M: 坏的解释器：没有那个文件或目录"错误
                        .exec("sed -i 's/\\r$//' " + destPath + "/" + stopScriptName)
                        //改下名字兼容旧程序
                        .exec("mv -f "+destPath + "/" + stopScriptName+" "+ destPath +"/" + SYS_DEFAULT_STOP+".sh")
                        //执行部署脚本之前，修改文档权限
                        .exec("chmod -R 777 " + destPath);
            }
            //获取卸载脚本
            String uninstallScriptUrl = getUninstallScriptUrl(serverDeploy);
            if(!StringUtils.isEmpty(uninstallScriptUrl)){
                sshClient
                        //上传卸载脚本
                        .scpUpload(uninstallScriptUrl,destPath + "/" + getFileName(uninstallScriptUrl,true))
                        //执行部署脚本之前，修改文档权限
                        .exec("chmod -R 777 " + destPath)
                        .exec("sed -i 's/\\r$//' " +destPath + "/" + getFileName(uninstallScriptUrl,true));
            }
            //获取部署脚本并执行
            String deployScriptUrl = getDeployScriptUrl(serverDeploy);
            if(!StringUtils.isEmpty(deployScriptUrl)){
                sshClient
                        //上传部署脚本
                        .scpUpload(deployScriptUrl,destPath + "/" + getFileName(deployScriptUrl,true))
                        .exec("sed -i 's/\\r$//' " +destPath + "/" + getFileName(deployScriptUrl,true))
                        //执行部署脚本之前，修改文档权限
                        .exec("chmod -R 777 " + destPath)
                        //执行部署脚本
                        .exec("cd "+destPath)
                        .exec("./"+ getFileName(deployScriptUrl,true));
            }
            sshClient.exec("cd "+ destPath);
            sshClient.exec("ll");
            message = "[" + serverDeploy.getServerName() + "]部署到：[" + serverDeploy.getServerIp() + "]服务器 工作成功";
            log.info(message);
            messageQueue.offer(message);
            serverDeploy.setStatus(DEPLOY_SUCCESS);
        } catch (Exception e) {
            message = "[" + serverDeploy.getServerName() + "]部署到：[" + serverDeploy.getServerIp() + "]服务器 工作失败";
            log.error(message);
            messageQueue.offer(e.getMessage());
            messageQueue.offer(message);
            serverDeploy.setStatus(DEPLOY_FAIL);
            log.error(e);
            throw e;
        }finally {
            serverDeployMapper.updateByPrimaryKeySelective(serverDeploy);
            if (sshClient!=null){
                sshClient.close();
            }
        }
    }
    /**
     * 异步服务发布
     * @param serverDeploy
     */
    @Async
    @Deploy
    public void asyncDeploy(ServerDeploy serverDeploy) {
        Queue<String> messageQueue = MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+serverDeploy.getId().toString());
        try {
            unInstall(serverDeploy.getId());
            deploy(serverDeploy);
            messageQueue.offer(DEPLOY_SUCCESS_MSG);
        } catch (Exception e) {
            messageQueue.offer(DEPLOY_FAIL_MSG);
            log.error(e);
        }
    }
    /**
     * 获取文件名，不包含后缀
     * @param url
     * @return
     */
    private static String getFileName(@NotNull String url, boolean hasSuffix) {
        if (!StringUtils.isEmpty(url)) {
            int startIndex = url.lastIndexOf("/");
            if (startIndex != -1) {
                int endIndex = -1;
                if (!hasSuffix) {
                    endIndex = url.lastIndexOf(".");
                }
                return endIndex != -1 ? url.substring(startIndex + 1, endIndex) : url.substring(startIndex + 1);
            }
        }
        return "";
    }
    public String getDeployMessage(String id) {
        return MessageQueueMarker.obtain(MESSAGE_QUEUE_PREFIX+id.toString()).poll();
    }
}
