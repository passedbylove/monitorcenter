package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.ServerManage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @Author:Administrator
 * @Date:2018/12/29
 */
@Repository
public interface ServerManageMapper {
    ServerManage selectDeployAndGzAndInfoByDeployId(Integer id);

    @Update("update server_deploy set run_status = #{status} where id = #{id}")
    int changeRunStatus(@Param("id") Integer id, @Param("status") int result);
}
