package com.hc.manage.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @project: hc-manage
 * @package: com.hc.manage.controller
 * @describe：针对首页请求处理
 * @author: hc
 * @date: 2020/5/17 10:17
 */

@Controller
@RequestMapping(value = "/")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping("/index")
    public String index() {
        logger.debug("--------------index page----------------");
        return "login";
    }

    @RequestMapping(value = "/login")
    public String login() {
        logger.debug("--------------login page---------------");
        return "login";
    }

    @RequestMapping("/home")
    public String home() {
        logger.debug("--------------home page----------------");
        return "home";
    }

    @RequestMapping("/logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }

    @RequestMapping("/toLogin")
    public String toLogin() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }

}
