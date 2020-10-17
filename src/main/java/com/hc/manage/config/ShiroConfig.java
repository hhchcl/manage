package com.hc.manage.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.hc.manage.filter.KickoutSessionFilter;
import com.hc.manage.shiroRealms.RetryLimitHashedCredentialsMatcher;
import com.hc.manage.shiroRealms.ShiroMultiRealm;
import com.hc.manage.shiroRealms.ShiroRealm;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.Filter;
import java.io.IOException;
import java.util.*;

/**
 * @project: hc-manage
 * @package: com.hc.manage.config
 * @author: hc
 * @date: 2020/5/2 14:34
 */

@Configuration
@EnableTransactionManagement // 启动事务管理器，等同于xml配置方式的 <tx:annotation-driven />
public class ShiroConfig {

    // slf4j日志框架
    private static final Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

    @Autowired
    private CustomConfigProperties customConfigProperties;
    //private List list = null;
    /**
     * ShiroFilterFactoryBean 处理拦截资源文件过滤器
     *	1、配置shiro安全管理器接口securityManage;
     *	2、shiro 连接约束配置filterChainDefinitions;
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            org.apache.shiro.mgt.SecurityManager securityManager) {
        //shiroFilterFactoryBean对象
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        logger.info("-----------------Shiro拦截器工厂类注入开始");
        // 配置shiro安全管理器 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //添加kickout认证
        HashMap<String,Filter> hashMap=new HashMap<String,Filter>();

        //限制同一帐号同时在线的个数。
        hashMap.put("kickout",kickoutSessionFilter());
        shiroFilterFactoryBean.setFilters(hashMap);

        // 指定要求登录时的链接
        shiroFilterFactoryBean.setLoginUrl("/login");
        //shiroFilterFactoryBean.setLoginUrl("/index");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/home");
        // 未授权时跳转的界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/login");

        // filterChainDefinitions拦截器=map必须用：LinkedHashMap，因为它必须保证有序
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置退出过滤器,具体的退出代码Shiro已经实现
        filterChainDefinitionMap.put("/logout", "logout");
        //配置记住我或认证通过可以访问的地址
        filterChainDefinitionMap.put("/user/userList", "user");
        //filterChainDefinitionMap.put("/", "user");

//		// 配置不会被拦截的链接 从上向下顺序判断
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/css/*", "anon");
        filterChainDefinitionMap.put("/js/*", "anon");
        filterChainDefinitionMap.put("/js/*/*", "anon");
        filterChainDefinitionMap.put("/js/*/*/*", "anon");
        filterChainDefinitionMap.put("/images/*/**", "anon");
        filterChainDefinitionMap.put("/layui/*", "anon");
        filterChainDefinitionMap.put("/layui/*/**", "anon");
        filterChainDefinitionMap.put("/treegrid/*", "anon");
        filterChainDefinitionMap.put("/treegrid/*/*", "anon");
        filterChainDefinitionMap.put("/fragments/*", "anon");
        filterChainDefinitionMap.put("/layout", "anon");

        //filterChainDefinitionMap.put("/hello", "anon");
        //filterChainDefinitionMap.put("/admin", "roles[admin]");

        filterChainDefinitionMap.put("/user/sendMsg", "anon");
        filterChainDefinitionMap.put("/user/login", "anon");
        filterChainDefinitionMap.put("/home", "authc");
//		/*filterChainDefinitionMap.put("/page", "anon");
//		filterChainDefinitionMap.put("/channel/record", "anon");*/
        filterChainDefinitionMap.put("/user/delUser", "authc,perms[usermanage]");
//		//add操作，该用户必须有【addOperation】权限
////		filterChainDefinitionMap.put("/add", "perms[addOperation]");
//
//		// <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问【放行】-->
        filterChainDefinitionMap.put("/auth/kickout", "anon");
        filterChainDefinitionMap.put("/**", "kickout,authc");
        filterChainDefinitionMap.put("/*/*", "authc");
        filterChainDefinitionMap.put("/*/*/*", "authc");
        filterChainDefinitionMap.put("/*/*/*/**", "authc");
        //filterChainDefinitionMap.put("/**", "user");


        shiroFilterFactoryBean
                .setFilterChainDefinitionMap(filterChainDefinitionMap);
        logger.info("-----------------Shiro拦截器工厂类注入成功");
        return shiroFilterFactoryBean;
    }

    /**
     * shiro安全管理器设置realm认证和ehcache缓存管理
     * @return
     */
    @Bean
    public org.apache.shiro.mgt.SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        /*list = new ArrayList();
        list.add(shiroRealm());
        list.add(shiroMultiRealm());
        securityManager.setRealms(list);*/ // 配置多个reaml验证，针对有些mysql存密码，有些oracle存密码
        // //注入ehcache缓存管理器;
        securityManager.setCacheManager(ehCacheManager());
        //securityManager.setCacheManager(redisCacheManager());
        // //注入session管理器;
        securityManager.setSessionManager(sessionManager());
        //注入Cookie记住我管理器
        securityManager.setRememberMeManager(rememberMeManager());

        // 设置realm.
        securityManager.setRealm(shiroRealm());
        return securityManager;
    }

    /**
     * 身份认证realm; (账号密码校验；权限等)
     *
     * @return
     */
    @Bean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        //使用自定义的CredentialsMatcher进行密码校验和输错次数限制
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * 身份认证realm; (账号密码校验；权限等)
     * 多个realm验证
     * @return

    @Bean
    public ShiroMultiRealm shiroMultiRealm() {
        ShiroMultiRealm shiroMultiRealm = new ShiroMultiRealm();
        //使用自定义的CredentialsMatcher进行密码校验和输错次数限制
        shiroMultiRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroMultiRealm;
    }*/

    /**
     * ehcache缓存管理器；shiro整合ehcache：
     * 通过安全管理器：securityManager
     * 单例的cache防止热部署重启失败
     * @return EhCacheManager
     */
    @Bean
    public EhCacheManager ehCacheManager() {
        logger.info(
                "=====shiro整合ehcache缓存：ShiroConfiguration.getEhCacheManager()");
        EhCacheManager ehcache = new EhCacheManager();
        CacheManager cacheManager = CacheManager.getCacheManager("shiro");
        if(cacheManager == null){
            try {

                cacheManager = CacheManager.create(ResourceUtils.getInputStreamForPath("classpath:config/ehcache.xml"));

            } catch (CacheException | IOException e) {
                e.printStackTrace();
            }
        }
        ehcache.setCacheManager(cacheManager);
        return ehcache;
    }

    /**
     * 设置记住我cookie过期时间
     * @return
     */
    @Bean
    public SimpleCookie remeberMeCookie(){
        logger.info("记住我，设置cookie过期时间！");
        //cookie名称;对应前端的checkbox的name = rememberMe
        SimpleCookie scookie=new SimpleCookie("rememberMe");
        //记住我cookie生效时间30天 ,单位秒  [10天]
        scookie.setMaxAge(864000);
        return scookie;
    }

    /**
     * 配置cookie记住我管理器
     * @return
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(){
        logger.info("配置cookie记住我管理器！");
        CookieRememberMeManager cookieRememberMeManager=new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(remeberMeCookie());
        return cookieRememberMeManager;
    }

    /**
     * 凭证匹配器 （由于我们的密码校验交给Shiro的SimpleAuthenticationInfo进行处理了
     * 所以我们需要修改下doGetAuthenticationInfo中的代码,更改密码生成规则和校验的逻辑一致即可; ）
     * 盐值加密，采用MD5
     * @return
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new RetryLimitHashedCredentialsMatcher(ehCacheManager());
        //new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");// 散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true); // 这一行决定hex还是base64,true=Hex,false=base64
        hashedCredentialsMatcher.setHashIterations(1024);// 默认值是1，可以不写，散列的次数，比如散列两次，相当于 // md5(md5(""));
        return hashedCredentialsMatcher;
    }

    /**
     * @描述：ShiroDialect，为了在thymeleaf里使用shiro的标签的bean
     * @创建人：hc
     * @创建时间：2017年12月21日 下午1:52:59
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect(){
        return new ShiroDialect();
    }

    /**
     * EnterpriseCacheSessionDAO shiro sessionDao层的实现；
     * 提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     */
    @Bean
    public EnterpriseCacheSessionDAO enterCacheSessionDAO() {
        EnterpriseCacheSessionDAO enterCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //添加缓存管理器
        //enterCacheSessionDAO.setCacheManager(ehCacheManager());
        //添加ehcache活跃缓存名称（必须和ehcache缓存名称一致）
        enterCacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
        return enterCacheSessionDAO;
    }

    /**
     *
     * @描述：sessionManager添加session缓存操作DAO
     * @创建人：hc
     * @创建时间：2018年4月24日 下午8:13:52
     * @return
     */
    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //sessionManager.setCacheManager(ehCacheManager());
        sessionManager.setSessionDAO(enterCacheSessionDAO());
        // redis
        //sessionManager.setSessionDAO(redisSessionDAO());
        sessionManager.setSessionIdCookie(sessionIdCookie());
        // 取消url 后面的 JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    /**
     * redisCacheManager 缓存 redis实现
     * 使用的是shiro-redis开源插件
     *
     * @return
     */
    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        return redisCacheManager;
    }

    /**
     * 配置shiro redisManager
     * 使用的是shiro-redis开源插件
     *
     * @return
     */
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(customConfigProperties.getHost());
        redisManager.setPort(customConfigProperties.getPort());
        redisManager.setTimeout(customConfigProperties.getTimeout());
        redisManager.setPassword(customConfigProperties.getPassword());
        return redisManager;
    }

    /**
     * RedisSessionDAO shiro sessionDao层的实现 通过redis
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        return redisSessionDAO;
    }

    /*
    * Session ID 生成器
    */
    @Bean
    public JavaUuidSessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    /**
     *
     * @描述：自定义cookie中session名称等配置
     * @创建人：hc
     * @创建时间：2018年5月8日 下午1:26:23
     * @return
     */
    @Bean
    public SimpleCookie sessionIdCookie() {
        //DefaultSecurityManager
        SimpleCookie simpleCookie = new SimpleCookie();
        //sessionManager.setCacheManager(ehCacheManager());
        //如果在Cookie中设置了"HttpOnly"属性，那么通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。
        simpleCookie.setHttpOnly(true);
        simpleCookie.setName("SHRIOSESSIONID");
        //单位秒
        simpleCookie.setMaxAge(900000);
        return simpleCookie;
    }

    /**
     *
     * @描述：kickoutSessionFilter同一个用户多设备登录限制
     * @创建人：hc
     * @创建时间：2018年4月24日 下午8:14:28
     * @return
     */
    public KickoutSessionFilter kickoutSessionFilter(){
        KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
        //使用cacheManager获取相应的cache来缓存用户登录的会话；用于保存用户—会话之间的关系的；
        //这里我们还是用之前shiro使用的ehcache实现的cacheManager()缓存管理
        //也可以重新另写一个，重新配置缓存时间之类的自定义缓存属性
        kickoutSessionFilter.setCacheManager(ehCacheManager());
       // kickoutSessionFilter.setCacheManager(redisCacheManager());
        //用于根据会话ID，获取会话进行踢出操作的；
        kickoutSessionFilter.setSessionManager(sessionManager());
        //是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序。
        kickoutSessionFilter.setKickoutAfter(false);
        //同一个用户最大的会话数，默认1；比如2的意思是同一个用户允许最多同时两个人登录；
        kickoutSessionFilter.setMaxSession(1);
        //被踢出后重定向到的地址；
        kickoutSessionFilter.setKickoutUrl("/toLogin?kickout=1");
        //kickoutSessionFilter.setKickoutUrl("/auth/kickout");
        return kickoutSessionFilter;
    }


    /**
     *
     * @描述：开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     * </br>Enable Shiro Annotations for Spring-configured beans. Only run after the lifecycleBeanProcessor(保证实现了Shiro内部lifecycle函数的bean执行) has run
     * </br>不使用注解的话，可以注释掉这两个配置
     * @创建人：hc
     * @创建时间：2018年5月21日 下午6:07:56
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

    public static void main(String[] args) {
        List list = new ArrayList();
        list.add(new ShiroRealm());
        list.add(new ShiroMultiRealm());
        for (int i = 0; i < list.size(); i++) {
            System.out.println("集合元素"+"["+i+"]"+"："+list.get(i).getClass().toString());
            if(list.get(i).getClass().toString().equals("class com.hc.manage.shiroRealms.ShiroRealm")) {
                System.out.println("包含 ShiroRealm");
            }
        }

    }
}
