package com.netmarch.monitorcenter.mapper;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.ServerScript;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
@Repository
public interface ServerScriptMapper extends Mapper<ServerScript> {


    /**
     * 条件查询
     * @param serverScript
     * @return
     */
    Page<ServerScript> selectByCondition(ServerScript serverScript);

}
