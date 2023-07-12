package com.netmarch.monitorcenter.mapper;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.Group;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: lining
 * @Description: GroupMapper
 */
public interface GroupMapper {
    /**
     * comment: 根据组类型获得组列表
     * @param: [type]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.Group>
     * @date: 2018/12/12 15:58
     */
    Page<Group> listGroupsByType(@Param("type") String type);

    /**
     * comment: 根据组类型获得组列表
     * @param: [type]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.Group>
     * @date: 2018/12/12 15:58
     */
    List<Group> getCodeByType(@Param("type") String type);

    /**
     * comment: 获得类型列表（未去重）
     * @param: []
     * @return: java.util.List<java.lang.String>
     * @date: 2018/12/12 17:11
     */
    List<String> listAllType();

    /**
     * comment: 新增Group
     * @param: [group]
     * @return: void
     * @date: 2018/12/13 10:22
     */
    void saveGroup(Group group);

    /**
     * comment: 根据code查找Group
     * @param: [code]
     * @return: com.netmarch.monitorcenter.bean.Group
     * @date: 2018/12/13 10:23
     */
    Group getGroupByCode(String code);

    /**
     * comment: 根据id获得Group
     * @param: [id]
     * @return: com.netmarch.monitorcenter.bean.Group
     * @date: 2018/12/13 11:03
     */
    Group getGroupById(int id);

    /**
     * comment: 修改Group
     * @param: [group]
     * @return: void
     * @date: 2018/12/13 11:16
     */
    void updateGroup(Group group);

    /**
     * comment: 根据code在server_info表中获得个数
     * @param: [code]
     * @return: java.util.List<com.netmarch.monitorcenter.bean.ServerInfo>
     * @date: 2018/12/13 13:18
     */
    int countServerInfoByCode(String code);

    /**
     * comment: 根据code在server_script表中获得个数
     * @param: [code]
     * @return: int
     * @date: 2018/12/13 13:23
     */
    int countServerScriptByCode(String code);

    /**
     * comment: 根据id 删除Group
     * @param: [id]
     * @return: void
     * @date: 2018/12/14 10:31
     */
    void deleteGroup(int id);

    @Select("select * from groups")
    List<Group> getAllGroups();
}
