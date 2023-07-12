package com.netmarch.monitorcenter.service.impl;

import com.netmarch.monitorcenter.bean.Users;
import com.netmarch.monitorcenter.config.db.DatabaseContextHolder;
import com.netmarch.monitorcenter.config.db.DatabaseType;
import com.netmarch.monitorcenter.mapper.UsersMapper;
import com.netmarch.monitorcenter.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper usersMapper;


    public Users getUsersByCodeAndPassword(String code, String password){
        DatabaseContextHolder.close();
        DatabaseContextHolder.setDatabaseType(DatabaseType.ONEDB);
        Users bean = new Users();
        bean.setCode(code);
        bean.setPassword(password);
        return usersMapper.getUsersByCodeAndPassword(bean);
    }

    public void  updatePwd(Long id,String pwd){

        DatabaseContextHolder.close();
        DatabaseContextHolder.setDatabaseType(DatabaseType.ONEDB);
        usersMapper.updatePwd(id,pwd);

    }
}
