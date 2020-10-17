package com.hc.manage.utils;

/**
 * @project: hc-manage
 * @package: com.hc.manage.utils
 * @author: hc
 * @date: 2020/5/17 9:12
 */
public interface IStatusMessage {

    String getCode();
    String getMessage();

    public enum SystemStatus implements IStatusMessage {

        /*
        * 自定义响应码和错误信息
        */
        SUCCESS("1000","SUCCESS"), //请求成功
        ERROR("1001","ERROR"),	   //请求失败
        PARAM_ERROR("1002","PARAM_ERROR"), //请求参数有误
        SUCCESS_MATCH("1003","SUCCESS_MATCH"), //表示成功匹配
        Refused_ERROR("1104","Refused_ERROR"), //拒绝用户登录
        NO_LOGIN("1100","NO_LOGIN"), //未登录
        MANY_LOGINS("1101","MANY_LOGINS"), //多用户在线（踢出用户）
        UPDATE("1102","UPDATE"), //用户信息或权限已更新（退出重新登录）
        LOCK("1111","LOCK"); //用户已锁定

        private String code;
        private String message;
        private SystemStatus(String code,String message){
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode(){
            return this.code;
        }
        @Override
        public String getMessage(){
            return this.message;
        }
    }
}
