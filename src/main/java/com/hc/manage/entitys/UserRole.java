package com.hc.manage.entitys;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @project: hc-manage
 * @package: com.hc.manage.entitys
 * @describe：
 * @author: hc
 * @date: 2020/5/30 10:58
 */

@Setter
@Getter
@ToString
public class UserRole implements Serializable {

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
    private String roleNames;
    private Integer version;
}
