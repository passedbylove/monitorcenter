package com.netmarch.monitorcenter.mapper;

import com.netmarch.monitorcenter.bean.Users;
import org.apache.ibatis.annotations.Param;

public interface UsersMapper {

    public Users getUsersByCodeAndPassword(Users bean);
    /*通过ID修改密码*/
    public void updatePwd(@Param("id") Long id, @Param("password") String password);
}
