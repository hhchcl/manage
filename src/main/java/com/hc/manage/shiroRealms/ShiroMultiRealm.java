package com.hc.manage.shiroRealms;

import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.util.ByteSource;

/**
 * @project: hc-manage
 * @package: com.hc.manage.shiroRealms
 * @author: hc
 * @date: 2020/5/2 23:03
 */
public class ShiroMultiRealm extends AuthenticatingRealm {
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("----[MultiRealm]-doGetAuthenticationInfo----"+authenticationToken.hashCode());
        // 1、把 AuthenticationToken 转换为 UsernamePasswordToken
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        setCachingEnabled(true);
        setAuthenticationCachingEnabled(true);
        // 2、从 UsernamePasswordToken 中获取 username
        String username = token.getUsername();

        // 3、调用数据库方法，查询username记录
        System.out.println("从数据库获取username：" + username);

        // 4、用户不存在，则可以抛出 UnknownAccountException 异常
        if("unknow".equals(username)) {
            throw new UnknownAccountException("用户不存在");
        }

        // 5、根据用户信息，决定是否抛出其他 AuthenticationException 异常
        if("monster".equals(username)) {
            throw new LockedAccountException("用户被锁定");
        }

        // 6、根据用户情况，来构建 AuthenticationInfo 并返回
        /**
         * principal：认证的实体信息，可以是username，也可以是数据表对应的实体类对象
         * credentials：数据库中的密码
         * realmName：当前realm对象的name，调用父类的getName()方法即可
         * credentialsSalt：盐值加密：如果不使用盐值加密的话，密码一样，加密后出来的字符串也一样
         */
        System.out.println("密码----->"+token.getCredentials());
        Object principal = username;
        String credentials = "123456";
        String realmName = getName();
        ByteSource byteSource = ByteSource.Util.bytes(username); // 以用户名作为盐值，因为用户名唯一的，要保证盐值是唯一的就行

        //SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(principal, DigestUtils.sha1Hex(credentials), byteSource, realmName);
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(principal, credentials, byteSource, realmName);

        return simpleAuthenticationInfo;
    }

    public static void main(String[] args) {// 加密过程实际就是调用这个 HashedCredentialsMatcher 类里面的SimpleHash
        String hashAlgorithmName = "SHA-1"; // MD5、SHA-1、SHA-256、SHA-384、SHA-512
        Object credentials = "123456";
        Object salt = ByteSource.Util.bytes("admin");
        int hashIterations = 1024;
        SimpleHash simpleHash = new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
        System.out.println(simpleHash);
    }
}
