package com.hc.manage.service.auth;

import com.hc.manage.entitys.Permission;
import com.hc.manage.entitys.Role;
import com.hc.manage.mapper.PermissionMapper;
import com.hc.manage.mapper.RoleMapper;
import com.hc.manage.mapper.RolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.service.auth
 * @describe：
 * @author: hc
 * @date: 2020/5/23 17:59
 */

@Service
public class AuthService {

    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 根据用户id获取权限数据
     * @param id
     * @return
     */
    public List<Permission> getUserPerms(Integer id) {
        return this.permissionMapper.getUserPerms(id);
    }

    /**
     * 根据用户获取角色列表
     * @param userId
     * @return
     */
    public List<Role> getRoleByUser(Integer userId) {
        return roleMapper.getRoleByUser(userId);
    }

    /**
     * 根据角色id获取权限数据
     * @param id
     * @return
     */
    public List<Permission> findPermsByRoleId(Integer id) {
        return permissionMapper.findPermsByRole(id);
    }

}
