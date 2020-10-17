package com.hc.manage.utils;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

/**
 * @project: hc-manage
 * @package: com.hc.manage.utils
 * @describe：密码加密
 * @author: hc
 * @date: 2020/7/12 9:10
 */

public class PwdEncryptionUtil {

    public SimpleHash pwdEnc(String mobile, String pwd) {
        String hashAlgorithmName = "MD5";
        Object credentials = pwd;
        ByteSource salt = ByteSource.Util.bytes(mobile);
        int hashIterations = 1024;
        SimpleHash simpleHash = new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
        return simpleHash;
    }
}
