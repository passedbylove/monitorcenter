package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.Group;
import com.netmarch.monitorcenter.bean.ServerGz;
import com.netmarch.monitorcenter.query.ServerGzQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName ServiceGzMapper
 * @Description 安装包管理映射接口
 * @Author 王顶奎
 * @Date 2018/12/1216:51
 * @Version 1.0
 **/
@Repository
public interface ServerGzMapper {

    /**
     * 通过group code 获取所有安装包
     * @return
     */
    public List<ServerGz> queryList(ServerGzQuery query);


    /**
     *  保存安装包信息
     * @return
     */
    public void save(ServerGz serverGz);


    /**
     *  保存安装包信息
     * @return
     */
    public void delete(@Param("id") Long id);

    /**
     *  保存安装包子菜单信息
     * @return
     */
    public void deleteChilden(@Param("id") Long id);

    /**
     *  更新安装包信息
     * @return
     */
    public void update(ServerGz serverGz);
    /**
     *  只对name进行更新
     * @return
     */
    public void editUpdate(@Param("name") String name,@Param("id") Long id);

    /**
     *  获取安装包类型code
     * @return
     */
    public List<Group> getCodeByType(ServerGzQuery query );

    /**
     * 获取安装包(包括父节点)
     * @return
     */
    List<ServerGz> getAllGzWithParent();    /**
     * 获取安装包
     * @return
     */
    List<ServerGz> getAllGz();
    /**
     *  根据ID查询bean
     * @return
     */
    public ServerGz queryById(@Param("id") Long id );

    /**
     *  根据ID查询bean
     * @return
     */
    public List<ServerGz> getParentList(@Param("code") String code);

    /**
     *  根据ID,CODE ,查询子类信息
     * @return
     */
    public List<ServerGz> getChildenList(ServerGzQuery query);
    /**
     *  根据ID,CODE ,查询子类信息
     * @return
     */
    public Integer getCountChildenById(@Param("id") Long id);


    /**
     * 通过id查询
     * @param gzId
     * @return
     */
    ServerGz selectGzById(Integer gzId);

    /**
     * 获取新版安装包
     * @param parentId
     * @param version
     * @return
     */
    List<ServerGz> selectNew(@Param("parentId") Integer parentId,@Param("version") String version);

    /**
     * 获取旧的安装包
     * @param parentId
     * @param version
     * @return
     */
    List<ServerGz> selectOld(@Param("parentId") Integer parentId,@Param("version") String version);


    public void updateStatus(@Param("id") Long id,@Param("status") Integer status);
}
