package com.netmarch.monitorcenter.controller;

import ch.ethz.ssh2.Connection;
import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.service.GroupService;
import com.netmarch.monitorcenter.service.ServerGzService;
import com.netmarch.monitorcenter.service.ServerInfoService;
import com.netmarch.monitorcenter.service.ServerScriptService;
import com.netmarch.monitorcenter.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
* @Description:controller
* @Author: dinggan
* @Date: 2018/12/14 0014
*/
@Controller
@Login
@Slf4j
@RequestMapping("serverScript")
public class ServerScriptController {

    @Autowired
    private ServerScriptService serverScriptService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ServerInfoService serverInfoService;

    @Autowired
    private ServerGzService serverGzService;

    /**
     * 列表信息展示
     * @param pageNo
     * @param serverScript
     * @param model
     * @return
     */
    @RequestMapping("list")
    public String list(Integer pageNo, ServerScript serverScript, Model model){
        try {
            Page<ServerScript> list = serverScriptService.selectByCondition(serverScript,pageNo);
            model.addAttribute("list",list);
            List<Group> groups =  groupService.listAllGroupsByType(StaticObj.SCRIPT_TYPE);
            model.addAttribute("groups",groups);
        } catch (Exception e) {
            log.info(e.toString());
        }
        return "serverScript/list";
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    @ResponseBody
    public RequestData delete(@PathVariable Integer id){
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            ServerScript serverScript = serverScriptService.getById(id);
            File scriptFile = new File(serverScript.getScriptUrl());
            if(scriptFile.isFile()){
                scriptFile.delete();
                File parentDir = scriptFile.getParentFile();
                if(parentDir.isDirectory()){
                    parentDir.delete();
                }
            }
            int count = serverScriptService.deleteById(id);
            if(count > 0){
                return requestData;
            }else{
                return RequestDataUtils.ERROR_RED("删除");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,log);
        }
    }

    /**
     * 跳转编辑页面
     * @param id
     * @param model
     * @return
     */
    @GetMapping("{id}")
    public String edit(@PathVariable Integer id,Model model){
        try {
            ServerScript bean = serverScriptService.getById(id);
            model.addAttribute("bean",bean);
            List<Group> groups =  groupService.listAllGroupsByType(StaticObj.SCRIPT_TYPE);
            model.addAttribute("groups",groups);
        } catch (Exception e) {
            log.info(e.toString());
        }
        return "serverScript/edit";
    }

    /**
     * 跳转添加页面
     * @param model
     * @return
     */
    @GetMapping("add")
    public String add(Model model){
        try {
            List<Group> groups =  groupService.listAllGroupsByType(StaticObj.SCRIPT_TYPE);
            model.addAttribute("groups",groups);
        } catch (Exception e) {
            log.info(e.toString());
        }
        return "serverScript/add";
    }

    /**
     * 新增
     * @param serverScript
     * @return
     */
    @PostMapping
    @ResponseBody
    public RequestData insert(ServerScript serverScript, MultipartHttpServletRequest request){
        MultipartFile scriptFile = request.getFile("scriptFile");
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            String originalFilename = scriptFile.getOriginalFilename();
            originalFilename = serverScript.getScriptName()+originalFilename.substring(originalFilename.lastIndexOf("."));
            File parentDir = new File(getParentPath(serverScript.getGroupId()));
            if(!parentDir.exists()){
                parentDir.mkdirs();
            }
            String scriptUrl = parentDir.getPath()+"/"+originalFilename;
            FileCopyUtils.copy(scriptFile.getInputStream(),new FileOutputStream(scriptUrl));
            serverScript.setScriptUrl(scriptUrl);
            serverScript.setCreateTime(new Date());
            int count = serverScriptService.insert(serverScript);
            if(count > 0){
                return requestData;
            }else{
                return RequestDataUtils.ERROR_RED("新增");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,log);
        }
    }



    /**
     * 编辑
     * @param serverScript
     * @return
     */
    @PutMapping
    @ResponseBody
    public RequestData update(ServerScript serverScript, MultipartHttpServletRequest request){
        MultipartFile scriptFile = request.getFile("scriptFile");
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try {
            if(!BeanUtil.isEmpty(scriptFile) && !scriptFile.isEmpty()){
                ServerScript oldServerScript = serverScriptService.getById(serverScript.getId());
                File scriptFil = new File(oldServerScript.getScriptUrl());
                if(scriptFil.isFile()){
                    scriptFil.delete();
                    File parentDir = scriptFil.getParentFile();
                    if(parentDir.isDirectory()){
                        parentDir.delete();
                    }
                }
                String originalFilename = scriptFile.getOriginalFilename();
                originalFilename = serverScript.getScriptName()+originalFilename.substring(originalFilename.lastIndexOf("."));
                File parentDir = new File(getParentPath(serverScript.getGroupId()));
                if(!parentDir.exists()){
                    parentDir.mkdirs();
                }
                String scriptUrl = parentDir.getPath()+"/"+originalFilename;
                FileCopyUtils.copy(scriptFile.getInputStream(),new FileOutputStream(scriptUrl));
                serverScript.setScriptUrl(scriptUrl);
            }

            int count = serverScriptService.update(serverScript);
            if(count > 0){
                return requestData;
            }else{
                return RequestDataUtils.ERROR_RED("编辑");
            }
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,log);
        }
    }

    /**
     * 下载
     * @param id
     * @param response
     */
    @GetMapping("download")
    public void downloadScript(Integer id, HttpServletResponse response){
        FileInputStream inStream = null;

        try {
            ServerScript bean = serverScriptService.getById(id);
            String scriptUrl = bean.getScriptUrl();
            File scriptFile = new File(scriptUrl);
            if(!scriptFile.exists()){
                return;
            }
            inStream = new FileInputStream(scriptFile);
            String returnName = scriptUrl.substring(scriptUrl.lastIndexOf("/")+1);
            response.setContentType("text/plain;charset=utf-8");
            returnName = URLEncoder.encode(returnName,"utf-8");			//保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition",   "attachment;filename=" + returnName);

            ServletOutputStream outputstream = response.getOutputStream();	//取得输出流
            byte[] b = new byte[1024];
            int len;
            while ((len = inStream.read(b)) > 0){
                outputstream.write(b, 0, len);
            }

        } catch (IOException e) {
            log.info(e.toString());
        }finally {
            if(inStream != null){
                try {
                    inStream.close();
                } catch (Exception e) {
                    log.info(e.toString());
                }
            }
        }
    }

    /**
     * 获取路径
     * @param groupId
     * @return
     */
    private String getParentPath(String groupId){
        return "/netmarch/script/"+groupId+"/"+
                DateUtil.parseDateToStr(new Date(),DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS)+"/";
    }

    /**
     * 执行脚本页面
     * @return
     */
    @GetMapping("execute")
    public String execute(Model model){
        List<ServerScript> serverScripts = serverScriptService.getAll();
        List<ServerGz> serverGzs = serverScriptService.getAllGz();
        List<ServerInfo> serverInfos = serverScriptService.getAllServer();
        model.addAttribute("serverInfos",serverInfos);
        model.addAttribute("serverGzs",serverGzs);
        model.addAttribute("serverScripts",serverScripts);
        return "serverScript/execute";
    }

    /**
     * 开始执行脚本
     * @param scriptId
     * @param gzId
     * @param serverId
     * @return
     */
    @PostMapping("start")
    @ResponseBody
    public RequestData startExecute(@RequestParam("scriptId") Integer scriptId,Integer gzId,@RequestParam("serverId") Integer serverId){
        RequestData requestData = RequestDataUtils.SUCESSES_RED();
        try{
            ServerInfo serverInfo = serverInfoService.queryServerInfoById(serverId);
            ServerScript serverScript = serverScriptService.getById(scriptId);
            Connection connection = RemoteConnect.getConnect(serverInfo.getIpAddress(), serverInfo.getUserName(), serverInfo.getPwd(), serverInfo.getLinkPort());
            if(!BeanUtil.isEmpty(connection)){
                try {
                    String cmd = "if [ ! -d \"/script/\" ];then\n" +
                            "mkdir /script\n" +
                            "else\n" +
                            "echo \"exist\"\n" +
                            "fi";
                    RemoteConnect.executeShell(connection,cmd);
                    RemoteConnect.uploadFile(connection,serverScript.getScriptUrl(),StaticObj.SCRIPT_PATH);
                    if (!BeanUtil.isEmpty(gzId)){
                        //todo 上传安装包
                        ServerGz serverGz = serverGzService.selectGzById(gzId);
                        RemoteConnect.uploadFile(connection,serverGz.getGzUrl(),StaticObj.SCRIPT_PATH);
                    }
                    String scriptUrl = serverScript.getScriptUrl();
                    String returnName = scriptUrl.substring(scriptUrl.lastIndexOf("/")+1);
                    RemoteConnect.executeCommand(connection,"chmod u+x /script/*.sh");
                    RemoteConnect.executeCommand(connection,StaticObj.SCRIPT_PATH+returnName);
                } finally {
                    connection.close();
                }

            }

        }catch (Exception e){
            return RequestDataUtils.EXP_RED(e,log);
        }

        //建立连接

        return requestData;
    }


}
