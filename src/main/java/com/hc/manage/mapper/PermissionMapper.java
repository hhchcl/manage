package com.hc.manage.mapper;

import com.hc.manage.entitys.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.mapper
 * @describe：
 * @author: hc
 * @date: 2020/5/23 18:38
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据用户id获取权限数据
     * @param userId
     * @return
     */
    public List<Permission> getUserPerms(Integer userId);

    /**
     * 根据角色id获取权限数据
     * @param roleId
     * @return
     */
    public List<Permission> findPermsByRole(Integer roleId);

}
