package com.hc.manage.service.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hc.manage.entitys.Role;
import com.hc.manage.entitys.User;
import com.hc.manage.entitys.UserRole;
import com.hc.manage.entitys.UserSearchResult;
import com.hc.manage.mapper.RoleMapper;
import com.hc.manage.mapper.UserMapper;
import com.hc.manage.utils.DateUtil;
import com.hc.manage.utils.PageDataResult;
import com.hc.manage.utils.SendMessage;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.service.user
 * @describe：User类的Service
 * @author: hc
 * @date: 2020/5/17 9:44
 */

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;

    public User findUserByMobile(String mobile) {
        return userMapper.findUserByMobile(mobile);
    }

    public PageDataResult getUsers(UserSearchResult userSearchResult, int page, int limit) {
        if (null != userSearchResult) {
            userSearchResult.setInsertTime(DateUtil.format(new Date()));
        }
        PageDataResult pageDataResult = new PageDataResult();
        PageHelper.startPage(page, limit);
        List<UserRole> list = userMapper.getUsers(userSearchResult);
        // 获取分页查询后的数据
        PageInfo<UserRole> pageInfo = new PageInfo<>(list);
        // 设置总记录数
        pageDataResult.setTotals(Long.valueOf(pageInfo.getTotal()).intValue());

        if (null != list && list.size() > 0) {
            for (UserRole userRole: list) {
                List<Role> roles = roleMapper.getRoleByUserId(userRole.getId());
                if (null != roles && roles.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < roles.size(); i++) {
                        Role role = roles.get(i);
                        builder.append(role.getRoleName());
                        if (i != roles.size() - 1) {
                            builder.append("，");
                        }
                    }
                    userRole.setRoleNames(builder.toString());
                }
            }

        }
        pageDataResult.setList(list);
        return pageDataResult;
    }

    public int update(User user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    public int updatePwd(int uId, String pwd) {
        return userMapper.updatePwd(uId, pwd);
    }


    public String sendMsg(User user1) {
        SimpleHash simpleHash = new SimpleHash("MD5", user1.getPassword(), ByteSource.Util.bytes(user1.getMobile()), 1024);
        User user = userMapper.findUser(user1.getUsername());//, simpleHash.toString()
        if (null != user && user.getMobile().equals(user1.getMobile())) {
            String mobileCode = "";
            if (null != user.getSendTime()) {
                long beginTime = user.getSendTime().getTime();
                long endTime = System.currentTimeMillis();
                if ((endTime - beginTime) <= 1000) { // 一分钟有效
                    logger.info("发送短信验证码【有效期】用户信息=user:" + user);
                    mobileCode = user.getMcode();
                }
            }
            String randomCode = SendMessage.getRandomCode(6);
            /* 短信验证码保留到数据库，如果有需要，可以将注释打开 */
            //Integer send = SendMessage.send("hhc0727", "d41d8cd98f00b204e980", "13********5", randomCode);
            Integer send = 1;
            if (send > 0) {
                user.setMcode(randomCode);
                logger.info("短信验证码:" + randomCode);
                user.setMcode(randomCode);
                user.setSendTime(new Date());
                // 更新验证码
                int count = userMapper.updateByPrimaryKeySelective(user);
                if (count > 0) {
                    return "ok";
                } else {
                    return "数据库插入验证码失败";
                }
            } else {
                return "验证码发送失败，请稍后再试！";
            }

        } else {
            return "用户不存在或被锁定";
        }
    }
}
