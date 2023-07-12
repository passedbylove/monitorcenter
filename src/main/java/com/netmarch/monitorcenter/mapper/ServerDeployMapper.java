package com.netmarch.monitorcenter.mapper;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.ServerDeploy;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author: lining
 * @Description: ServerDeployMapper
 */
@Repository
public interface ServerDeployMapper extends Mapper<ServerDeploy> {
    /**
     * comment: 根据页码、服务名称、ip获得服务分页列表
     * @param: [pageNo, serverName, ip]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.ServerDeploy>
     * @date: 2018/12/14 9:57
     */
    Page<ServerDeploy> listServerDeploysByNameAndIp(@Param("serverName") String serverName, @Param("serverIp") String ip);

    /**
     * 带出服务器详细信息和安装包详细信息
     * @param example
     * @return
     */
    List<ServerDeploy> selectRelationByExample(Example example);

    @Update("update server_deploy set status = 1 where id = #{id}")
    int updateDeployStatus(Integer id);

    @Update("update server_deploy set status = 0 where id = #{id}")
    int recoverDeployStatus(Integer id);

    @Select("select status from server_deploy where id = #{id}")
    ServerDeploy queryServerDeployById(Integer id);

    @Select("select count(1) from server_deploy where serverIp = #{server_ip} and serverPort = #{server_port}")
    int selectRepeat(ServerDeploy serverDeploy);
}
