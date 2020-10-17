package com.hc.manage.controller;

import com.hc.manage.config.ShiroConfig;
import com.hc.manage.entitys.Permission;
import com.hc.manage.entitys.User;
import com.hc.manage.service.auth.AuthService;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.controller
 * @describe：
 * @author: hc
 * @date: 2020/5/23 12:03
 */

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

    @Autowired
    private AuthService authService;

    /**
     * 根据用户id查询权限树数据
     * @return PermTreeDTO
     */
    @RequestMapping(value = "/getUserPerms", method = RequestMethod.GET)
    @ResponseBody
    public List<Permission> getUserPerms(ModelMap map, HttpSession session) {
        logger.debug("根据用户id查询限树列表！");
        List<Permission> pvo = null;
        User existUser= (User) SecurityUtils.getSubject().getPrincipal();
        if(null==existUser){
            logger.info("根据用户id查询限树列表！用户未登录");
            return pvo;
        }
        try {
            pvo = authService.getUserPerms(existUser.getId());
            map.addAttribute("userPerms",pvo);
            session.setAttribute("userPerms",pvo);
            //生成页面需要的json格式
            logger.info("根据用户id查询限树列表查询=pvo:" + pvo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("根据用户id查询权限树列表查询异常！", e);
        }
        return pvo;
    }

    /**
     * 跳转到角色列表
     * @return
     */
    @RequestMapping("/roleManage")
    public ModelAndView toPage() {
        return new ModelAndView("/auth/roleManage");
    }

    /**
     * 权限列表
     * @return ok/fail
     */
    @RequestMapping(value = "/permList", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView permList() {
        logger.debug("权限列表！");
        ModelAndView mav = new ModelAndView("/auth/permList");
        try {
           /* List<Permission> permList = authService.permList();
            logger.debug("权限列表查询=permList:" + permList);
            mav.addObject("permList", permList);
            mav.addObject("msg", "ok");*/
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("权限查询异常！", e);
        }
        return mav;
    }

    @RequestMapping(value = "/kickout")
    public String kickout() {
        return "login";
    }
}
