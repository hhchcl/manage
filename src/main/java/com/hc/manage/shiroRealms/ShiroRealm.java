package com.hc.manage.shiroRealms;

import com.hc.manage.entitys.Permission;
import com.hc.manage.entitys.Role;
import com.hc.manage.entitys.User;
import com.hc.manage.service.auth.AuthService;
import com.hc.manage.service.user.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.shiroRealms
 * @author: hc
 * @date: 2020/5/2 23:03
 */
@Service
public class ShiroRealm extends AuthenticatingRealm {

    private static final Logger logger = LoggerFactory.getLogger(ShiroRealm.class);

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //System.out.println("----[FirstRealm]-doGetAuthenticationInfo----"+authenticationToken.hashCode());
        // 1、把 AuthenticationToken 转换为 UsernamePasswordToken
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        setCachingEnabled(true);
        setAuthenticationCachingEnabled(true);
        // 2、从 UsernamePasswordToken 中获取 username
        String mobile = token.getUsername();

        // 3、调用数据库方法，查询username记录
        User user = userService.findUserByMobile(mobile);
        logger.info("用户登录认证！用户信息user：" + user);
        if(user == null) {
           return null;
        } else {

        /* 4、用户不存在，则可以抛出 UnknownAccountException 异常
        if("unknow".equals(username)) {
            throw new UnknownAccountException("用户不存在");
        }

         5、根据用户信息，决定是否抛出其他 AuthenticationException 异常
        if("monster".equals(username)) {
            throw new LockedAccountException("用户被锁定");
        }*/

        // 6、根据用户情况，来构建 AuthenticationInfo 并返回
        /**
         * principal：认证的实体信息，可以是username，也可以是数据表对应的实体类对象
         * credentials：数据库中的密码，通过查询数据返回的值进行比对
         * realmName：当前realm对象的name，调用父类的getName()方法即可
         * credentialsSalt：盐值加密：如果不使用盐值加密的话，密码一样，加密后出来的字符串也一样
         */
        User users = new User();
        users.setMobile(mobile);


       /* Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        for(Session session: sessions) {
            if(users.toString().equals(String.valueOf(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)))) {
                redisSessionDAO.delete(session);
            }
        }*/

        Object principal = mobile;
        logger.info("-------------------principal:"+mobile);
        Object credentials = user.getPassword();/*123456 038bdaf98f2037b31f1e75b5b4c9b26e*/
        logger.info("-------------------credentials:"+user.getPassword());
        String realmName = getName();
        ByteSource byteSource = ByteSource.Util.bytes(user.getMobile()); // 以用户名作为盐值，因为用户名唯一的，要保证盐值是唯一的就行

        //SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(principal, DigestUtils.md5Hex("123456"), byteSource, realmName);
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user, credentials, byteSource, realmName);
        return simpleAuthenticationInfo;
        }
    }

    /*
    * 交由shiro管理角色和权限
    * */
   protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) throws UnauthorizedException{
       logger.info("=======授权角色和权限=======");
       SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
       // 获取登录当前用户信息
       Subject subject = SecurityUtils.getSubject();
       subject.hasRole("superman");
       User user = (User) principalCollection.getPrimaryPrincipal();
       if (user.getMobile().equals("13247550575")) {
           // 超级管理员，添加所有角色、添加所有权限
           authorizationInfo.addRole("*");
           authorizationInfo.addStringPermission("*");
       } else {
           // 普通用户，查询用户的角色，根据角色查询权限
           List<Role> roles = authService.getRoleByUser(user.getId());
           if (null != roles && roles.size() > 0) {
               for (Role role: roles) {
                   authorizationInfo.addRole(role.getCode());
                   // 角色对应的权限
                   List<Permission> perms = authService.findPermsByRoleId(role.getId());
                   if (null != perms && perms.size() > 0) {
                       for(Permission permission: perms) {
                           // 由shiro管理权限
                           authorizationInfo.addStringPermission(permission.getCode());
                       }
                   }
               }

           }
       }
       return  authorizationInfo;
   }

    /**
     * 重写方法，清除当前用户的 认证缓存
     * @param principals
     */
    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }


    public static void main(String[] args) {// 加密过程实际就是调用这个 HashedCredentialsMatcher 类里面的SimpleHash
        String hashAlgorithmName = "MD5"; // MD5、SHA-1、SHA-256、SHA-384、SHA-512
        Object credentials = "123456";
        Object salt = ByteSource.Util.bytes("15278499117");
        int hashIterations = 1024;
        SimpleHash simpleHash = new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
        System.out.println(simpleHash);
    }
}
