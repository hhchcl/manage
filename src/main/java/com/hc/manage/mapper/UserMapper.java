package com.hc.manage.mapper;

import com.hc.manage.entitys.User;
import com.hc.manage.entitys.UserRole;
import com.hc.manage.entitys.UserSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.mapper
 * @describe：@Mapper或者@MapperScan将接口扫描装配到容器中
 * @author: hc
 * @date: 2020/5/17 9:40
 */

@Mapper
public interface UserMapper {

    /*
     * 根据手机号获取用户信息
     */
    public User findUserByMobile(String mobile);

    /**
     * 分页查询用户数据
     * @return
     */
    public List<UserRole> getUsers(@Param("userSearchResult") UserSearchResult userSearchResult);

    /**
     * 根据用户名和密码查找用户
     * @param username
     * @param password
     * @return , @Param("password") String password
     */
    public User findUser(@Param("username") String username);

    /**
     * 更新验证码
     * @return
     */
    public int updateByPrimaryKeySelective(User record);

    /**
     * 更新用户
     * @param user
     * @return
     */
    public int updateUser(User user);

    /*
    * 修改密码
    * @param uId, pwd
    * @return
    */
    public int updatePwd(@Param("uId") int id, @Param("pwd") String password);

}
