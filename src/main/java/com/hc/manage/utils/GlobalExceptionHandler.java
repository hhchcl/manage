package com.hc.manage.utils;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @project: hc-manage
 * @package: com.hc.manage.utils
 * @describe：@ControllerAdvice注解的作用：是一个Controller增强器，可对controller中被@RequestMapping注解的方法加一些逻辑处理，
 * 最常用的就是异常处理；【三种使用场景】全局异常处理。全局数据绑定，全局数据预处理
 * @author: hc
 * @date: 2020/6/3 13:43
 */

@ControllerAdvice(basePackages = {"com.hc.manage.controller"})
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationException.class)
    public String authorizationExceptionHandler() {
        return "/error/403";
    }

    @ExceptionHandler(AuthenticationException.class)
    public String authenticationExceptionHandler() {
        return "/error/403";
    }

    @ExceptionHandler(Exception.class)
    public String defaultExceptionHandler() {
        return "/error/403";
    }
}
