package com.netmarch.monitorcenter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.netmarch.monitorcenter.bean.*;
import com.netmarch.monitorcenter.controller.ServiceGzController;
import com.netmarch.monitorcenter.enums.StatusType;
import com.netmarch.monitorcenter.mapper.ServerGzMapper;
import com.netmarch.monitorcenter.query.ServerGzQuery;
import com.netmarch.monitorcenter.service.ServerGzService;
import com.netmarch.monitorcenter.util.DateUtil;
import com.netmarch.monitorcenter.util.RequestDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ServiceGzServiceImpl
 * @Description TODO
 * @Author ZGD
 * @Date 2018/12/1415:26
 * @Version 1.0
 **/
@Service("serverGzService")
public class ServerGzServiceImpl implements ServerGzService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceGzController.class);

    @Autowired
    public ServerGzMapper serverGzMapper;

    @Override
    public List<ServerGz> queryAll() {
        return serverGzMapper.getAllGz();
    }
    @Override
    public List<ServerGz> queryAllWithParent() {
        return serverGzMapper.getAllGzWithParent();
    }
    @Value("${upload.baseUrl}")
    private String uploadUrl;

    @Override
    public JSONObject queryList(ServerGzQuery query) {

        PageHelper.startPage(query.getPageNo() == null ? 1 : query.getPageNo(), PagesStatic.PAGES_SIZE);

        List<Group> listCode = this.getCodeByType(query);

        List<ServerGz> serviceGz = serverGzMapper.queryList(query);

        JSONObject TreeJson = BeanFactory.getJSONObject();
        List<Object> jsonList =BeanFactory.getArrayList(Object.class);

        for(Group group : listCode){

            List<ServerGz> serverGzList = serverGzMapper.getParentList(group.getCode());

            for(ServerGz serverGz : serverGzList ){
                //构建父状图
                JSONObject parentJson = new JSONObject();
                parentJson.put("name",serverGz.getName());
                parentJson.put("abbreviate",serverGz.getAbbreviate());
                parentJson.put("version",serverGz.getVersion());
                parentJson.put("createTime", DateUtil.parseDateToStr(serverGz.getCreateTime(), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS,"--null--"));
                parentJson.put("gzUrl",serverGz.getGzUrl());
                parentJson.put("remark",serverGz.getRemark());
                parentJson.put("status",serverGz.getStatus());
                parentJson.put("groupCode",serverGz.getGroupCode());
                parentJson.put("id",serverGz.getId());
                parentJson.put("parentId",serverGz.getParentId());
                parentJson.put("isParent",true);

                ServerGzQuery serverGzQuery =  new ServerGzQuery();
                serverGzQuery.setCode(serverGz.getGroupCode());
                serverGzQuery.setId(serverGz.getId());

                List<ServerGz> childenList = serverGzMapper.getChildenList(serverGzQuery);

                //构建childen状图
                parentJson.put("children",childenList);
                jsonList.add(parentJson);
            }
        }

        TreeJson.put("nodes",jsonList);

        return TreeJson;
    }


    @Override
    public void save(ServerGz serverGz) throws Exception{

        serverGzMapper.save(serverGz);
    }

    @Override
    public ServerGz queryById(Long id) {
        return serverGzMapper.queryById(id);
    }

    @Override
    public RequestData delete(Long id) throws Exception{
        ServerGz serverGz = this.queryById(Long.valueOf(id));
        if(serverGz ==null){
            return RequestDataUtils.ERROR_RED("删除不存在");
        }
        if(serverGz.getStatus() !=null && serverGz.getStatus() == 0){
            return RequestDataUtils.ERROR_RED("已部署，不能删除");
        }

        /**
         * 删除数据
         */
        serverGzMapper.delete(id);
        serverGzMapper.deleteChilden(id);

        File file=new File(serverGz.getGzUrl());
        if(file.exists()&&file.isFile())
            file.delete();

        File delfile = new File(serverGz.getGzUrl());

        //删除文件夹
        deleteDir(delfile);

        return RequestDataUtils.SUCESSES_RED();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir
     *            将要删除的文件目录
     * @return
     */
    private static boolean deleteDir(File dir) {
        if (!dir.exists()) return false;
        if (dir.isDirectory()) {
            String[] childrens = dir.list();
            // 递归删除目录中的子目录下
            for (String child : childrens) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    @Override
    public RequestData update(ServerGz serverGz) {

        try {
            serverGzMapper.update(serverGz);
        } catch (Exception e) {
            e.printStackTrace();
            return RequestDataUtils.ERROR_RED("更新失败");

        }

        return RequestDataUtils.SUCESSES_RED();

    }

    @Override
    public void editUpdate(String name, Long id) throws Exception {
        serverGzMapper.editUpdate(name,id);
    }

    @Override
    public List<Group> getCodeByType(ServerGzQuery query) {
        return serverGzMapper.getCodeByType(query);
    }

    @Override
    public Integer getCountChildenById(Long id) {
        return serverGzMapper.getCountChildenById(id);
    }

    @Override
    public RequestData uploadServer(ServerGz serverGz, MultipartHttpServletRequest request) {

        MultipartFile gzFile = request.getFile("gzFile");

        try {

            //获取子菜单数
            Integer count = serverGzMapper.getCountChildenById(serverGz.getId());
            ServerGz childenServerGz = BeanFactory.getServerGz();

            //获取父类信息
            ServerGz parentServerGz = this.selectGzById(serverGz.getId().intValue());
            //组装子菜单
            childenServerGz.setName(serverGz.getName());
            childenServerGz.setParentId(String.valueOf(serverGz.getId()));
            childenServerGz.setCreateTime(new Date());
            childenServerGz.setAbbreviate(parentServerGz.getAbbreviate());
            childenServerGz.setGroupCode(serverGz.getGroupCode());
            childenServerGz.setRemark(serverGz.getRemark());
            childenServerGz.setStatus(Integer.valueOf(StatusType.FAIL.getValue()));
            childenServerGz.setVersion(String.valueOf(count+1));
            String  urlBase = uploadUrl+parentServerGz.getAbbreviate()+"/"+ (count+1) ; //文件夹路径
            String url = urlBase + "/"+gzFile.getOriginalFilename();//文件路径
            childenServerGz.setGzUrl(url);
            //创建路径文件夹
            File file=new File(urlBase);
            if(!file.exists())
                file.mkdirs();

            this.save(childenServerGz);

            FileCopyUtils.copy(gzFile.getInputStream(),new FileOutputStream(url));

            return RequestDataUtils.SUCESSES_RED();
        } catch (Exception e) {
            return RequestDataUtils.EXP_RED(e,logger);
        }


    }

    @Override
    public ServerGz selectGzById(Integer gzId) {

        return serverGzMapper.selectGzById(gzId);
    }

    @Override
    public List<ServerGz> queryNew(Integer parentId, String version) {
        return serverGzMapper.selectNew(parentId,version);
    }

    @Override
    public List<ServerGz> queryOld(Integer parentId, String version) {
        return serverGzMapper.selectOld(parentId,version);
    }

    @Override
    public void updateStatus(Long id,Integer status) {

        serverGzMapper.updateStatus(id, status);
    }
}
