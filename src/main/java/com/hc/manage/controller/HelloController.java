package com.hc.manage.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @project: hc-manage
 * @package: com.hc.manage.controller
 * @author: hc
 * @date: 2020/5/2 11:08
 */

@Controller
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "login";
    }

    @RequestMapping("/loginaa")
    //@RequiresRoles("超级管理")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        System.out.println("加密前密码："+password);
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        System.out.println("加密后用户名=====>"+token.getUsername());
        System.out.println("加密后密码=====>"+token.getPassword());
        token.setRememberMe(true);
        Subject subject = SecurityUtils.getSubject();
        try {
            System.out.println("token--1-->："+token.hashCode());
            subject.login(token);
            System.out.println("登录成功");
            return "login";
        } catch (AuthenticationException e) {
            System.out.println("登录失败："+e.getMessage());
            subject.logout();
            return "hello";
        }
    }

    @RequestMapping("/logout1")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "hello";
    }

}
