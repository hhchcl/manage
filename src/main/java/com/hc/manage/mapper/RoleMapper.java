package com.hc.manage.mapper;

import com.hc.manage.entitys.Role;
import com.hc.manage.entitys.UserRole;
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
public interface RoleMapper {

    public List<Role> getRoleByUserId(int uid);

    public List<Role> getRoleByUser(Integer userId);
}
