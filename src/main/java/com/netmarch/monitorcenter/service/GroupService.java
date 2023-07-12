package com.netmarch.monitorcenter.service;

import com.github.pagehelper.Page;
import com.netmarch.monitorcenter.bean.Group;

import java.util.List;

/**
 * @Author: lining
 * @Description: GroupService
 */
public interface GroupService {
    /**
     * comment: 根据组类型获得组列表
     * @param: [type]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.Group>
     * @date: 2018/12/12 15:58
     */
    Page<Group> listGroupsByType(String type, Integer pageNo);


    /**
     * comment: 根据组类型获得组列表
     * @param: [type]
     * @return: com.github.pagehelper.Page<com.netmarch.monitorcenter.bean.Group>
     * @date: 2018/12/12 15:58
     */
    List<Group> getCodeByType(String type);

    /**
     * comment: 获得group.type列表（去重）
     * @param: []
     * @return: java.util.List<java.lang.String>
     * @date: 2018/12/12 17:14
     */
    List<String> listAllType();

    /**
     * comment: 新增Group(判断code唯一)
     * @param: [group]
     * @return: void
     * @date: 2018/12/13 10:26
     */
    boolean saveGroup(Group group);

    /**
     * comment: 根据id获得Group
     * @param: [id]
     * @return: com.netmarch.monitorcenter.bean.Group
     * @date: 2018/12/13 11:03
     */
    Group getGroupById(int id);

    /**
     * comment: 修改Group
     * @param: [id]
     * @return: void
     * @date: 2018/12/13 11:19
     */
    boolean updateGroup(Group group);

    /**
     * comment: 删除Group,先进行"是否在用" 判断
     * @param: [id]
     * @return: void
     * @date: 2018/12/13 13:25
     */
    boolean deleteGroup(int id);

    List<Group> getAllGroups();

    List<Group> listAllGroupsByType(String type);
}
