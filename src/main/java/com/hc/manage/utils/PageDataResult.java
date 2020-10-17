package com.hc.manage.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.utils
 * @describe：
 * @author: hc
 * @date: 2020/5/27 18:33
 */

@Setter
@Getter
@ToString
public class PageDataResult {

    //总记录数量
    private Integer totals;
    //当前页数据列表
    private List<?> list;
    private Integer code=200;

    public PageDataResult() {
    }

    public PageDataResult( Integer totals, List<?> list) {
        this.totals = totals;
        this.list = list;
    }
}
