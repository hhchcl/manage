package com.hc.manage.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hc.manage.entitys.User;
import com.hc.manage.utils.IStatusMessage;
import com.hc.manage.utils.ResponseResult;
import com.hc.manage.utils.ShiroFilterUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @project: hc-manage
 * @package: com.hc.manage.service.user
 * @describe：自定义过滤器，进行用户访问控制
 * @author: hc
 * @date: 2020/5/17 9:48
 */
public class KickoutSessionFilter extends AccessControlFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(KickoutSessionFilter.class);

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private String kickoutUrl; // 踢出后到的地址
	private boolean kickoutAfter = false; // 踢出之前登录的/之后登录的用户 默认false踢出之前登录的用户
	private int maxSession = 1; // 同一个帐号最大会话数 默认1
	private SessionManager sessionManager;
	private Cache<String, Deque<Serializable>> cache;

	public void setKickoutUrl(String kickoutUrl) {
		this.kickoutUrl = kickoutUrl;
	}

	public void setKickoutAfter(boolean kickoutAfter) {
		this.kickoutAfter = kickoutAfter;
	}

	public void setMaxSession(int maxSession) {
		this.maxSession = maxSession;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	// 设置Cache的key的前缀
	public void setCacheManager(CacheManager cacheManager) {
		//必须和ehcache缓存配置中的缓存name一致
		this.cache = cacheManager.getCache("shiro-activeSessionCache");
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		// 没有登录授权 且没有记住我
		logger.info("没有登录授权：" + subject.isAuthenticated() +"没有记住我：" + subject.isRemembered());
		if (!subject.isAuthenticated() || subject.isRemembered()) {// || subject.isRemembered()
			// 如果没有登录，直接进行之后的流程
			//判断是不是Ajax请求，异步请求，直接响应返回未登录
			ResponseResult responseResult = new ResponseResult();
			if (ShiroFilterUtils.isAjax(request) ) {
				logger.info(getClass().getName()+ "当前用户已经在其他地方登录，并且是Ajax请求！");
				responseResult.setCode(IStatusMessage.SystemStatus.MANY_LOGINS.getCode());
				responseResult.setMessage("您已在别处登录，请您修改密码或重新登录");
				out(response, responseResult);
				return subject.isAuthenticated() || subject.isRemembered();
			}else{
				return true;
			}
		}
		// 获得用户请求的URI
		HttpServletRequest req=(HttpServletRequest) request;
		int maxInactiveInterval = req.getSession().getMaxInactiveInterval();
		logger.info("===session最大时间：==" + maxInactiveInterval);
		String path = req.getRequestURI();
		logger.info("===当前请求的uri：==" + path);
		//String contextPath = req.getContextPath();
		//logger.debug("===当前请求的域名或ip+端口：==" + contextPath);
		//放行登录
		if(path.equals("/login")){
		//if(path.equals("/auth/kickout")){
			return true;
		}
		Session session = subject.getSession();
		logger.info("==session时间设置：" + String.valueOf(session.getTimeout())
				+ "===========");
		try {
			// 当前用户
			User user = (User) subject.getPrincipal();
			String mobile = user.getMobile();
			logger.info("===当前用户mobile：==" + mobile);
			Serializable sessionId = session.getId();
			logger.info("===当前用户sessionId：==" + sessionId);
			// 读取缓存用户 没有就存入
			Deque<Serializable> deque = cache.get(mobile);
			logger.info("===当前deque：==" + deque);
			//logger.info("===kickout====："+session.getAttribute("kickout"));
			if (deque == null) {
				// 初始化队列
				logger.info("===初始化队列====");
				deque = new ArrayDeque<Serializable>();
			}
			// 如果队列里没有此sessionId，且用户没有被踢出；放入队列
			//if (!deque.contains(sessionId)  && session.getAttribute("kickout") == null) {
			if (!deque.contains(sessionId)) {
				// 将sessionId存入队列
				logger.info("===队列没有 sessionId===="+sessionId);
				deque.push(sessionId);
				// 将用户的sessionId队列缓存
				cache.put(mobile, deque);
			}
			// 如果队列里的sessionId数超出最大会话数，开始踢人
			while (deque.size() > maxSession) {
				logger.debug("===deque队列长度：==" + deque.size());
				Serializable kickoutSessionId = null;
				// 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；
				if (kickoutAfter) { // 如果踢出后者
					kickoutSessionId = deque.removeFirst();
				} else { // 否则踢出前者
					kickoutSessionId = deque.removeLast();
				}
				// 踢出后再更新下缓存队列
				cache.put(mobile, deque);
				try {
					// 获取被踢出的sessionId的session对象
					Session kickoutSession = sessionManager
							.getSession(new DefaultSessionKey(kickoutSessionId));
					logger.debug("===踢出session对象==="+kickoutSession);
					if (kickoutSession != null) {
						// 设置会话的kickout属性表示踢出了
						kickoutSession.setAttribute("kickout", true);
					}
				} catch (Exception e) {// ignore exception
				}
			}

			// 如果被踢出了，(前者或后者)直接退出，重定向到踢出后的地址
			if (session.getAttribute("kickout") != null && (Boolean)session.getAttribute("kickout") == true) {
				// 会话被踢出了
				try {
					// 退出登录
					logger.debug("===踢出登录===");
					subject.logout();
				} catch (Exception e) { // ignore
				}
				saveRequest(request);
				logger.info("==踢出后用户重定向的路径kickoutUrl:" + kickoutUrl);
				// ajax请求
				// 重定向
				WebUtils.issueRedirect(request, response, kickoutUrl);
				return isAjaxResponse(request,response);
			}
			return true;
		} catch (Exception e) { // ignore
			logger.info(
					"控制用户在线数量【lyd-admin-->KickoutSessionFilter.onAccessDenied】异常！",
					e);
			// 重启后，ajax请求，报错：java.lang.ClassCastException:
			// com.lyd.admin.pojo.AdminUser cannot be cast to
			// com.lyd.admin.pojo.AdminUser
			// 处理 ajax请求
			return isAjaxResponse(request,response);
		}
	}
	/**
	 *
	 * @描述：response输出json
	 * @创建人：hc
	 * @创建时间：2018年4月24日 下午5:14:22
	 * @param response
	 * @param result
	 */
	/*public static void out(ServletResponse response, ResponseResult result){
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("UTF-8");//设置编码
			response.setContentType("application/json");//设置返回类型
			out = response.getWriter();
			out.println(objectMapper.writeValueAsString(result));//输出
			logger.info("用户在线数量限制【hc-manager-->KickoutSessionFilter.out】响应json信息成功");
		} catch (Exception e) {
			logger.info("用户在线数量限制【hc-manager-->KickoutSessionFilter.out】响应json信息出错", e);
		}finally{
			if(null != out){
				out.flush();
				out.close();
			}
		}
	}*/

	public ResponseResult out(ServletResponse response, ResponseResult result) {
		logger.info("用户在线数量限制【hc-manager-->KickoutSessionFilter.out】响应json信息成功"+result);
		return result;
	}

	private boolean isAjaxResponse(ServletRequest request,
			ServletResponse response) throws IOException {
		// ajax请求
		/**
		 * 判断是否已经踢出
		 * 1.如果是Ajax 访问，那么给予json返回值提示。
		 * 2.如果是普通请求，直接跳转到登录页
		 */
		//判断是不是Ajax请求
		ResponseResult responseResult = new ResponseResult();
		if (ShiroFilterUtils.isAjax(request) ) {
			responseResult.setCode(IStatusMessage.SystemStatus.MANY_LOGINS.getCode());
			responseResult.setMessage("您已经在其他地方登录，请重新登录！");
			out(response, responseResult);
		}else{
			// 重定向
			WebUtils.issueRedirect(request, response, kickoutUrl);
		}
		return false;
	}

}
