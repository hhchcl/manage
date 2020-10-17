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
 * @date: 2020/5/23 18:20
 */

@Getter
@Setter
@ToString
public class Permission implements Serializable {

    private String id;

    private String name;

    private String pId;

    private String istype;

    private String code;

    private String page;

    private String icon;

    private String zindex;

    private boolean checked;

    private boolean open;

}
