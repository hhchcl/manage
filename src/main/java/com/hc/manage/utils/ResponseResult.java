package com.hc.manage.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @project: hc-manage
 * @package: com.hc.manage.ResponseResult
 * @describe：统一响应处理,code:编码,message:描述,obj对象
 * @author: hc
 * @date: 2020/5/16 19:13
 */

@Setter
@Getter
@ToString
public class ResponseResult implements Serializable {

    // 使用lombok简化java样板代码，如getter，setter等方法
    private String code;
    private String message;
    private Object object;

    public ResponseResult() {
        this.code = IStatusMessage.SystemStatus.SUCCESS.getCode();
        this.message = IStatusMessage.SystemStatus.SUCCESS.getMessage();
    }

    public ResponseResult(IStatusMessage iStatusMessage) {
        this.code = iStatusMessage.getCode();
        this.message = iStatusMessage.getMessage();
    }
}
