package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.ServerInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author:Administrator
 * @Date:2018/12/12
 */
@Repository
public interface ServerInfoMapper extends Mapper<ServerInfo> {

    List<ServerInfo> queryServerInfoByConditions(ServerInfo serverInfo);

    @Update("update server_info set status = 1 where id = #{id}")
    int updateServerInfoStatus(Integer id);

    @Update("update server_info set status = 0 where id = #{id}")
    int recoverServerInfoStatus(Integer id);

    @Select("select count(1) from server_info where ip_address = #{ipAddress}")
    int checkRepeat(String ipAddress);

    @Delete("delete from server_info where id = #{id}")
    int deleteServerInfoById(Integer id);

    int updateServerInfo(ServerInfo serverInfo);

    @Select("select ip_address from server_info where id = #{id}")
    String selectIpAddressById(String id);
}
