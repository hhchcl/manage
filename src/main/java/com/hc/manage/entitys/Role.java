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
 * @date: 2020/5/30 11:28
 */

@Setter
@Getter
@ToString
public class Role implements Serializable {

    private Integer id;
    private String roleName;
    private String descpt;
    private String code;
    private Integer insertUid;
    private Date insertTime;
    private Date updateTime;

}
