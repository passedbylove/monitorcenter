package com.netmarch.monitorcenter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.service.*;
import com.netmarch.monitorcenter.util.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: lining
 * @Description: ServerDeployController
 */
@Controller
@RequestMapping("server")
public class ServerDeployController {
    @Autowired
    private ServerDeployService serverDeployService;
    @Autowired
    private ServerInfoService serverInfoService;
    @Autowired
    private ServerGzService serverGzService;
    @Autowired
    private ServerScriptService serverScriptService;
    @Autowired
    private ServerManageService serverManageService;

    @GetMapping("tolist")
    public ModelAndView toList(ModelAndView modelAndView,@RequestParam Map<String,Object> params){
        modelAndView.setViewName("serverdeploy/list");
        PageParam pageParam = new PageParam(params);
        List<ServerDeploy> serverDeploys = serverDeployService.queryAll();
        List<ServerInfo> serverInfos = serverInfoService.queryAllServerInfo();
        List<Map<String, Object>> treeMap = toTreeMap(serverDeploys,serverInfos);
        modelAndView.addObject("treeJson",JSON.toJSONString(treeMap));
        modelAndView.addObject("param",pageParam);
        return  modelAndView;
    }
    private List<Map<String, Object>> toTreeMap(List<ServerDeploy> serverDeploys, List<ServerInfo> serverInfos){
            List<Map<String,Object>> serverInfoMaps = serverInfos.stream().map(item -> {
                Map<String,Object> data = new HashMap();
                data.put("id",item.getId());
                data.put("version",item.getSysName());
                data.put("serverName",item.getIpAddress());
                data.put("pid",-1);
                return  data;
            }).collect(Collectors.toList());
            Map<String,Integer> ipToIdMap = serverInfos.stream().collect(Collectors.toMap(
                    ServerInfo::getIpAddress,ServerInfo::getId
            ));
            List<JSONObject> serverDeployMaps =serverDeploys.stream().map(item -> {
                JSONObject serverDeployMap = JSONUtils.parseJSONObject(item);
                //设置父id
                serverDeployMap.put("pid",ipToIdMap.get(item.getServerIp()));
                return serverDeployMap;
            }).collect(Collectors.toList());
            serverInfoMaps.addAll(serverDeployMaps);
            return serverInfoMaps;
    }
    /**
     * @Description: ServerDeployController
     * 添加服务部署，添加后即自动部署到服务器
     * @Author: fengxiang
     * @Date: 2018/12/14 13:49
     */
    @GetMapping("toadd")
    public ModelAndView toAdd(ModelAndView modelAndView){
        List<ServerInfo> serverInfos = serverInfoService.queryAllServerInfo();
        modelAndView.addObject("serverInfos",serverInfos);
        List<ServerScript> serverScripts = serverScriptService.getAll();
        modelAndView.addObject("serverScripts",serverScripts);
        modelAndView.setViewName("serverdeploy/add");
        return modelAndView;
    }

    /**
     * 服务部署树结构数据
     * @return
     */
    @GetMapping("gzstree")
    @ResponseBody
    public String getServerGzTreeData(){
        List<ServerGz> serverGzs = serverGzService.queryAllWithParent();
        return toTreeMap(serverGzs);
    }
    private String toTreeMap(List<ServerGz> serverGzs){
        List serverGzsTreeData = new ArrayList();
        if (!CollectionUtils.isEmpty(serverGzs)){
            serverGzsTreeData = serverGzs.stream().peek(serverGz -> {
                if (!StringUtils.isEmpty(serverGz.getGroupCode())) {
                    if (StaticObj.CONTECT_GZ.equals(serverGz.getGroupCode())) {
                        serverGz.setGroupCode("环境安装包");
                    } else if (StaticObj.SERVER_GZ.equals(serverGz.getGroupCode())) {
                        serverGz.setGroupCode("服务安装包");
                    }
                }
            }).collect(
                    Collectors.groupingBy(ServerGz::getGroupCode,Collectors.toList())
            ).entrySet().stream().map(item -> {
                String rootKey = "0";
                Map<String,List<ServerGz>> serverGzMapGroupByPid = item.getValue().stream().collect(Collectors.groupingBy(ServerGz::getParentId, Collectors.toList()));
                List<ServerGz> serverGzRoot = serverGzMapGroupByPid.get(rootKey);
                List<Map> serverGzRootMaps = null;
                if(!CollectionUtils.isEmpty(serverGzRoot)){
                    serverGzMapGroupByPid.remove(rootKey);
                    //jdk,node,python分类
                    serverGzRootMaps = serverGzRoot.stream().map(serverGz -> {
                        HashMap<String,Object> serverGzMap = new HashMap<>();
                        serverGzMap.put("id",serverGz.getId());
                        serverGzMap.put("name",serverGz.getName());
                        serverGzMap.put("open",true);
                        serverGzMap.put("chkDisabled",true);
                        List<ServerGz> serverGzChildren = serverGzMapGroupByPid.get(serverGz.getId().toString());
                        if (!CollectionUtils.isEmpty(serverGzChildren)){
                            serverGzMap.put("children",serverGzChildren.stream().map(serverGzChild -> {
                                HashMap<String,Object> serverGzChildMap = new HashMap<>();
                                serverGzChildMap.put("id",serverGzChild.getId());
                                serverGzChildMap.put("name",serverGzChild.getName()+" version:"+serverGzChild.getVersion());
                                serverGzChildMap.put("groupCode",serverGzChild.getGroupCode());
                                serverGzChildMap.put("status",serverGzChild.getStatus());
                                serverGzChildMap.put("open",false);
                                return serverGzChildMap;
                            }).collect(Collectors.toList()));
                        }
                        return serverGzMap;
                    }).collect(Collectors.toList());
                }

                HashMap<String,Object> map = new HashMap<>();
                map.put("children",serverGzRootMaps);
                map.put("open",true);
                map.put("name",item.getKey());
                map.put("chkDisabled",true);
                return map;
            }).collect(Collectors.toList());
        }
        return JSON.toJSONString(serverGzsTreeData);
    }

    /**
     * 更新后发布
     * @param serverDeploy
     * @return
     */
    @PostMapping("add")
    @ResponseBody
    public RequestData add(ServerDeploy serverDeploy){
        Integer id = serverDeployService.saveOrUpdateWithDeploy(serverDeploy);
        return new RequestData(id);
    }
    /**
     * 更新
     * @param serverDeploy
     * @return
     */
    @PostMapping("update")
    public String update(ServerDeploy serverDeploy){
        serverDeployService.saveOrUpdateWithDeploy(serverDeploy);
        return "redirect:tolist";
    }
    @GetMapping("to-exec-msg")
    public ModelAndView toExecMessage(String id,ModelAndView modelAndView){
        modelAndView.addObject("serverDeployId",id);
        modelAndView.setViewName("serverdeploy/exec-message");
        return  modelAndView;
    }
    @GetMapping("get-exec-msg")
    @ResponseBody
    public RequestData execMessage(String id){
        String msg = serverDeployService.getExecMessage(Integer.parseInt(id));
        return new RequestData(msg);
    }
    /**
     * 重新或者回滚（当选择更低版本的时候）
     * @param serverDeploy
     * @return
     */
    @PostMapping("redeploy")
    @ResponseBody
    public RequestData reDeploy(ServerDeploy serverDeploy){
        serverDeployService.reDeployWithUpdate(serverDeploy.getId());
        return new RequestData();
    }

    /**
     * 卸载
     * @param id
     * @return
     */
    @GetMapping("uninstall/{id}")
    @ResponseBody
    public RequestData uninstall(@PathVariable("id") Integer id) {
        RequestData requestData = new RequestData();
        try {
            serverManageService.stopServer(id);
        } catch (Exception e) {
            requestData.setCode(RequestData.FAIL_CODE);
            requestData.setMsg("停止服务失败："+e.getMessage());
        }
        serverDeployService.unInstall(id);
        return requestData;
    }
    /**
     * 获取更新安装包信息
     * @return
     */
    @GetMapping("new-gzs")
    @ResponseBody
    public RequestData getNewGzs(Integer parentId,String version){
        List<ServerGz> serverGzs = serverGzService.queryNew(parentId,version);
        return new RequestData(serverGzs);
    }
    @GetMapping("check-deployport")
    @ResponseBody
    public RequestData checkServerDeployPort(String serverId,Integer deployPort){
        Boolean isUnique = false ;
        if (StringUtils.isEmpty(serverId) || deployPort == null) {
            isUnique = false;
        }else {
            ServerDeploy serverDeploy = new ServerDeploy();
            serverDeploy.setServerId(serverId);
            serverDeploy.setServerPort(deployPort);
            List<ServerDeploy> list = serverDeployService.query(serverDeploy);
            isUnique = list!=null?list.size()==0:true;
        }
        return new RequestData(isUnique);
    }
    /**
     * 获取更新安装包信息
     * @return
     */
    @GetMapping("old-gzs")
    @ResponseBody
    public RequestData getOldGzs(Integer parentId,String version){
        List<ServerGz> serverGzs = serverGzService.queryOld(parentId,version);
        return new RequestData(serverGzs);
    }
    /**
     * 删除服务部署
     * @param id
     * @return
     */
    @GetMapping("delete/{id}")
    @ResponseBody
    public RequestData delete(@PathVariable("id") Integer id){
        RequestData requestData = new RequestData();
        try {
            Integer res = serverDeployService.delete(id);
            requestData.setObjData(res);
        } catch (Exception e) {
           throw e;
        }
        return requestData;
    }

    /**
     * 编辑操作
     * @param id
     * @param serverDeploy
     * @return
     */
    @PostMapping("edit/{id}")
    public RequestData edit(@PathVariable("id") Long id,ServerDeploy serverDeploy){
        return null;
    }

    /**
     * 列表，包含查询条件
     * @param params
     * @return
     */
    @PostMapping("list")
    @ResponseBody
    public PageBean list(@RequestBody Map<String,Object> params){
        PageBean pageBean = serverDeployService.query(new PageParam(params));
        return pageBean;
    }
}
