package com.hc.manage.shiroRealms;

import com.hc.manage.entitys.User;
import com.hc.manage.mapper.UserMapper;
import com.hc.manage.service.user.UserService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @project: hc-manage
 * @package: com.hc.manage.shiroRealms
 * @describe：shiro之密码输入次数限制6次，并锁定2分钟
 * @author: hc
 * @date: 2020/5/17 9:44
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {// 密码匹配

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShiroRealm.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    //集群中可能会导致出现验证多过5次的现象，因为AtomicInteger只能保证单节点并发
    //解决方案，利用ehcache、redis（记录错误次数）和mysql数据库（锁定）的方式处理：密码输错次数限制； 或两者结合使用
    private Cache<String, AtomicInteger> passwordRetryCache;  
  
    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
        //读取ehcache中配置的登录限制锁定时间
        passwordRetryCache = cacheManager.getCache("passwordRetryCache");  
    }

    /**
     * 在回调方法doCredentialsMatch(AuthenticationToken token,AuthenticationInfo info)中进行身份认证的密码匹配，
     * </br>这里我们引入了Ehcahe用于保存用户登录次数，如果登录失败retryCount变量则会一直累加，如果登录成功，那么这个count就会从缓存中移除，
     * </br>从而实现了如果登录次数超出指定的值就锁定。
     * @param token
     * @param info
     * @return
     */
    @Override  
    public boolean doCredentialsMatch(AuthenticationToken token,  
            AuthenticationInfo info) {
        //获取登录用户名
        String mobile = (String) token.getPrincipal();
        User user = userService.findUserByMobile(mobile);
        //从ehcache中获取密码输错次数
        // retryCount
        AtomicInteger retryCount = passwordRetryCache.get(mobile);
        if (retryCount == null) {
            //第一次
            retryCount = new AtomicInteger(0);  
            passwordRetryCache.put(mobile, retryCount);
        }
        //retryCount.incrementAndGet()自增：count + 1
        if (retryCount.incrementAndGet() > 5) {
            // if retry count > 5 throw  超过5次 锁定
            /* 可优化*/
            if (user != null && "0".equals(user.getState())){
                //数据库字段 默认为 0  就是正常状态 所以 要改为1
                //修改数据库的状态字段为锁定
                user.setState("1");
                userService.update(user);
            }
            logger.info("锁定用户" + user.getUsername());
            //抛出用户锁定异常 throw new LockedAccountException();
            throw new ExcessiveAttemptsException("mobile:"+mobile+" tried to login more than 5 times in period");
        }  
        //否则走判断密码逻辑
        boolean matches = super.doCredentialsMatch(token, info);  
        if (matches) {  
            // clear retry count  清楚ehcache中的count次数缓存
            passwordRetryCache.remove(mobile);
        }  
        return matches;  
    }

    /**
     * 根据用户名 解锁用户
     * @param username
     * @return 可优化
     */
    /*public void unlockAccount(String username){
        User user = userMapper.findByUserName(username);
        if (user != null){
            //修改数据库的状态字段为锁定
            //user.setState("0");
            userMapper.update(user);
            passwordRetryCache.remove(username);
             redisManager.del(getRedisKickoutKey(username));
        }
    }*/
} 