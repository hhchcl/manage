package com.hc.manage.utils;

import org.apache.shiro.util.SimpleByteSource;

import java.io.Serializable;

/**
 * @project: hc-manage
 * @package: com.hc.manage.utils
 * @describe：
 * @author: hc
 * @date: 2020/7/12 11:48
 */
public class SimpleByteSerializable extends SimpleByteSource implements Serializable {

    public SimpleByteSerializable(byte[] bytes) {
        super(bytes);
    }

    public SimpleByteSerializable(String bytes) {
        super(bytes);
    }
}
