package com.hc.manage.entitys;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @project: hc-manage
 * @package: com.hc.manage.entitys
 * @describe：
 * @author: hc
 * @date: 2020/5/30 10:02
 */
@Setter
@Getter
@ToString
public class UserSearchResult implements Serializable{

    private Integer page;
    private Integer limit;
    private String uname;
    private String umobile;
    private String insertTime;

}
