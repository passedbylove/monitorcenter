package com.netmarch.monitorcenter.service;

import com.netmarch.monitorcenter.bean.Users;

public interface UsersService {
    /**
     * 登录
     * @param code
     * @param password
     * @return
     */
    Users getUsersByCodeAndPassword(String code, String password);

    /**
     * 修改密码
     * @param id
     * @param pwd
     */
    void updatePwd(Long id, String pwd);
}
