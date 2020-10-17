package com.hc.manage.controller;

import com.hc.manage.entitys.User;
import com.hc.manage.entitys.UserSearchResult;
import com.hc.manage.service.user.UserService;
import com.hc.manage.utils.IStatusMessage;
import com.hc.manage.utils.PageDataResult;
import com.hc.manage.utils.PwdEncryptionUtil;
import com.hc.manage.utils.ResponseResult;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @project: hc-manage
 * @package: com.hc.manage.controller
 * @describe：用来处理user请求
 * @author: hc
 * @date: 2020/5/16 13:11
 */

@Controller
@RequestMapping("/user")
public class UserConterller {

    // 使用slf4j日志框架
    private static final Logger logger = LoggerFactory.getLogger(UserConterller.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EhCacheManager ehCacheManager;

    @Autowired
    private SessionManager sessionManager;

    private Session session;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult login(User user, @RequestParam(value = "rememberMe",required = false) boolean rememberMe, @RequestParam("smsCode") String reqSmsCode) {
        logger.info("用户登录，请求参数=user:" + user);
        ResponseResult responseResult = new ResponseResult();
        Subject subject = SecurityUtils.getSubject();
        session = subject.getSession();
        session.setTimeout(900000); // 15分钟就退出到登录页面
        logger.info("===当前用户登录session：==" + session.getId());

        if (user == null) {
            responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR
                    .getCode());
            responseResult.setMessage("请求参数有误，请您稍后再试");
            logger.info("用户登录，结果=responseResult:" + responseResult);
            return responseResult;
        } if (!validatorRequestParam(user, responseResult)) {
            logger.info("用户登录，结果=responseResult:" + responseResult);
            return responseResult;
        }
        User existUser = userService.findUserByMobile(user.getMobile());
        if (existUser == null) {
            responseResult.setCode(IStatusMessage.SystemStatus.Refused_ERROR.getCode());
            responseResult.setMessage("该用户不存在，请您联系管理员");
            logger.info("用户登录，结果=responseResult:" + responseResult);
            return responseResult;
        } else {
            // 是否离职
            if (existUser.getHaveJob()) {
                responseResult.setCode(IStatusMessage.SystemStatus.Refused_ERROR.getCode());
                responseResult.setMessage("登录用户已离职，请您联系管理员");
                logger.info("用户登录，结果=responseResult:" + responseResult);
                return responseResult;
            }
            if ("1".equals(existUser.getState())) {
                responseResult.setCode(IStatusMessage.SystemStatus.Refused_ERROR.getCode());
                responseResult.setMessage("用户已被锁定，请您联系管理员");
                logger.info("用户登录，结果=responseResult:" + responseResult);
                return responseResult;
            }

            // 校验验证码
			if(!existUser.getMcode().equals(reqSmsCode)){
			 responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR.getCode());
			 responseResult.setMessage("短信验证码输入有误");
			 logger.info("用户登录，结果=responseResult:"+responseResult);
			 return responseResult;
			} //1分钟
			long beginTime =existUser.getSendTime().getTime();
			long endTime = System.currentTimeMillis();
			if(((endTime-beginTime)-600000>0)){
				 responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR.getCode());
				 responseResult.setMessage("短信验证码超时");
				 logger.info("用户登录，结果=responseResult:"+responseResult);
				 return responseResult;
			}
        }

        logger.info("是否记住我：" + rememberMe);
        AuthenticationToken token = new UsernamePasswordToken(user.getMobile(), user.getPassword(), rememberMe);
        logger.info("用户登录，用户验证开始！user=" + user.getMobile());
        //当前用户是否已经被认证（即是否已经登录）
        if (!subject.isAuthenticated() || subject.isRemembered()) {
            logger.info("用户没有认证====");
            try {
                subject.login(token);
                logger.info("登录授权02：" + subject.isAuthenticated() +"记住我02：" + subject.isRemembered());
            } catch (Exception e) {
                responseResult.setCode(IStatusMessage.SystemStatus.Refused_ERROR.getCode());
                Cache<Object, Object> cache = ehCacheManager.getCache("passwordRetryCache");
                logger.info("用户用户缓存！cache=" + user.getMobile());
                if (Integer.parseInt(cache.get(user.getMobile()).toString()) > 5) {
                    responseResult.setMessage("账号已被锁定，请联系管理员");
                } else {
                    responseResult.setMessage("您还有"+ (5-(Integer.parseInt(cache.get(user.getMobile()).toString()))) + "次机会输入密码");
                }
                //responseResult.setMessage("用户名或密码错误");
                return responseResult;
            }
            responseResult.setCode(IStatusMessage.SystemStatus.SUCCESS
                    .getCode());
            logger.info("用户登录，用户验证通过！user=" + user.getMobile());
        }
        return responseResult;
    }

    @RequestMapping("/userList")
    //@RequiresRoles({"guestmanage"})
    public String toUserList(){
        return "/auth/userList";
    }


    @RequestMapping(value = "/getUsers", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getUsers(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit, UserSearchResult searchResult) {

        logger.info("分页查询用户列表，搜索条件：UserSearchResult：" + searchResult + "，页数page：" + page + "，每页显示数量limit：" + limit);
        PageDataResult pageDataResult = new PageDataResult();
        if (null == page) {
            page = 1;
        }
        if (null == limit) {
            limit = 10;
        }
        // 获取用户和角色列表
        pageDataResult = userService.getUsers(searchResult, page, limit);

        logger.info("用户角色列表：" + pageDataResult);
        return pageDataResult;
    }



    @RequestMapping(value = "/sendMsg",method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult sendMsg(User user) {

        logger.info("发送短信验证码！user:" + user);
        ResponseResult responseResult = new ResponseResult();
        try {
            if (null == user) {
                responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR
                        .getCode());
                responseResult.setMessage("请求参数有误，请您稍后再试");
                logger.info("发送短信验证码，结果=responseResult:" + responseResult);
                return responseResult;
            }
            if (!validatorRequestParam(user, responseResult)) {
                logger.info("发送短信验证码，结果=responseResult:" + responseResult);
                return responseResult;
            }
            // 送短信验证码
            String msg=userService.sendMsg(user);
            //String msg = "ok";
            if (msg != "ok") {
                responseResult.setCode(IStatusMessage.SystemStatus.ERROR
                        .getCode());
                responseResult.setMessage(msg == "no" ? "发送验证码失败，请您稍后再试" : msg);
                return responseResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setCode(IStatusMessage.SystemStatus.ERROR.getCode());
            responseResult.setMessage("发送短信验证码失败，请您稍后再试");
            logger.error("发送短信验证码异常！", e);
        }
        logger.info("发送短信验证码，结果=responseResult:" + responseResult);
        return responseResult;
    }

    @RequestMapping(value = "/setPwd", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult setPwd(@RequestParam("oldPwd") String oldPwd, @RequestParam("pwd") String Pwd, @RequestParam("isPwd") String isPwd) {
        logger.info("====修改密码 1====");
        ResponseResult responseResult = new ResponseResult();
        try {

            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipals().getPrimaryPrincipal();
            if(null == user) {
                responseResult.setCode(IStatusMessage.SystemStatus.ERROR.getCode());
                responseResult.setMessage("您未登录或登录超时，请您重新登录后再试");
                return responseResult;
            }
            logger.info("principal==="+user.getMobile());
            PwdEncryptionUtil pwdEncryptionUtil = new PwdEncryptionUtil();
            SimpleHash oldSimpleHash = pwdEncryptionUtil.pwdEnc(user.getMobile(), oldPwd);
            SimpleHash isSimpleHash = pwdEncryptionUtil.pwdEnc(user.getMobile(), Pwd);
            if (!user.getPassword().equals(oldSimpleHash.toString())) {
                responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR.getCode());
                responseResult.setMessage("当前密码与登录密码不一致");
                return responseResult;
            }
            if (user.getPassword().equals(isSimpleHash.toString())) {
                responseResult.setCode(IStatusMessage.SystemStatus.PARAM_ERROR.getCode());
                responseResult.setMessage("新密码不能与最近密码一致");
                return responseResult;
            }
            logger.info("用户ID===：" + user.getId()+"=====密码====："+isSimpleHash.toString());
            int num = userService.updatePwd(user.getId(), isSimpleHash.toString());
            if (num != 1) {
                responseResult.setCode(IStatusMessage.SystemStatus.ERROR.getCode());
                responseResult.setMessage("操作失败，请您稍后再试");
                logger.info("修改密码失败，已经离职或该用户被删除！结果=responseResult:" + responseResult);
                return responseResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setCode(IStatusMessage.SystemStatus.ERROR.getCode());
            responseResult.setMessage("操作失败，请您稍后再试");
            logger.info("修改密码异常！", e);
            return responseResult;
        }
        responseResult.setCode(IStatusMessage.SystemStatus.SUCCESS.getCode());
        responseResult.setMessage("密码修改成功");
        return responseResult;
    }

    /**
     * @描述：校验请求参数
     * @param obj
     * @param response
     * @return
     */
    protected boolean validatorRequestParam(Object obj, ResponseResult response) {
        boolean flag = false;
        Validator validator = new Validator();
        List<ConstraintViolation> ret = validator.validate(obj);
        if (ret.size() > 0) {
            // 校验参数有误
            response.setCode(IStatusMessage.SystemStatus.PARAM_ERROR.getCode());
            response.setMessage(ret.get(0).getMessageTemplate());
        } else {
            flag = true;
        }
        return flag;
    }

    @RequestMapping(value = "/personalData",method = RequestMethod.GET)
    public String personalData() {// 基本资料
        return "page/personalData";
    }

    @RequestMapping(value = "/updateUsePwd",method = RequestMethod.GET)
    public String updateUsePwd() {// 修改密码
        return "page/updateUsePwd";
    }
}
