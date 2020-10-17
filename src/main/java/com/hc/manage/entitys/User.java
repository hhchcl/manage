package com.hc.manage.entitys;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @project: hc-manage
 * @package: com.hc.manage.entitys
 * @describe：User实体类
 * @author: hc
 * @date: 2020/5/17 10:30
 */

@Setter
@Getter
@ToString
public class User implements Serializable {

    // username   用户名
    // password   手机号
    // mobile     邮箱
    // email      密码
    // insertUid  添加该用户的用户id
    // insertTime 注册时间
    // updateTcime 修改时间
    // isDel      是否删除（0：正常；1：已删）
    // isJob      是否在职（0：正常；1，离职）
    // mcode      短信验证码
    // sendTime   短信发送时间
    // version    更新版本

    private Integer id;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private Integer insertUid;
    private Date insertTime;
    private Date updateTime;
    private Boolean haveDel;
    private Boolean haveJob;
    private String mcode;
    private Date sendTime;
    private Integer version;
    private String state;

}
