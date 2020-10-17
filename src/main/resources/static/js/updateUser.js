/**
 * 修改用户密码
 * */
//获取路径uri
var picCode;
$(function(){
    picCode = drawPic();
    //监控（发送验证码）
    layui.use(['form' ,'layer'], function() {
        var form = layui.form;
        var layer = layui.layer;
        //监控提交
        form.on("submit(getMsg)",function (data) {
            //sendMsg();
            var flag=checkData();
            if(flag!=false){
                sendMessage(this,true);
            }
            return false;
        });
        //确认手机号
        form.on("submit(updatePwd)",function () {
            updatePwd();
            return false;
        });
        //确认修改密码
        form.on("submit(setPwd)",function () {
            setPwd();
            return false;
        });
    })
})
function checkData(){
//  校验
    var mobile=$("#telephone").val();
    var code=$("#picCode").val();
    if("ok"!=ValidateUtils.checkMobile(mobile)){
        //tips层-右
        layer.tips(ValidateUtils.checkMobile(mobile), '#telephone', {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    if("ok"!=ValidateUtils.checkPicCode(code)){
        //tips层-右
        layer.tips(ValidateUtils.checkPicCode(code), '#canvas', {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    if(picCode.toLowerCase()!=code.toLowerCase()){
        //tips层-右
        layer.tips("请您输入正确的验证码", '#canvas', {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
}
var wait=60;
function sendMessage(o,flag){
    if (!flag) {
        return false;
    }
    //第一次秒数
    if (wait == 60) {
        o.setAttribute("disabled", true);
        //自定义验证规则
        $.post("/user/sendMessage", {"mobile":$("#telephone").val(),"picCode":$("#picCode").val()}, function (data) {
            console.log("data:" + data)
            if (data.code == "1000") {
                layer.msg("发送短信成功");
            } else {
                //picCode = drawPic();
                $("#picCode").val("");
                layer.alert(data.message);
                //禁用发送短信验证码按钮
                o.removeAttribute("disabled");
                //o.value = "获取验证码";
                wait = 60;
                flag = false;
                layer.alert(data.message);
            }
            return false;
        });
    }
    if (wait == 0) {
        o.removeAttribute("disabled");
        $("#getMsgBtn").html("获取验证码");
        wait = 60;
    } else {
        o.setAttribute("disabled", true);
        if (wait <60) {
            $("#getMsgBtn").html("<span style='margin-left: -12px;'>"+wait + "s后可重新发送</span>");
        }
        wait--;
        setTimeout(function () {
            if (wait == 0) {
                flag = true
            };
            send(o, flag)
        }, 1000)
    }
}
/*
function updatePwd(){
    var flag=checkData();
    if(flag!=false){
        var mobileCode=$("#mobileCode").val();
        if("ok"!=ValidateUtils.checkCode(mobileCode)){
            //tips层-右
            layer.tips("请您输入正确的验证码", '#getMsgBtn', {
                tips: [2, '#78BA32'], //还可配置颜色
                tipsMore: true
            });
            return false;
        }
        $.post("/user/updatePwd",{"mobile":$("#telephone").val(),"picCode":$("#picCode").val(),"mobileCode":mobileCode},function(data){
            console.log("data:"+data)
            if(data.code=="1000"){
                layer.closeAll();
                layer.open({
                    type:1,
                    title: "设置新密码",
                    fixed:false,
                    resize :false,
                    shadeClose: true,
                    area: ['450px'],
                    content:$('#pwdDiv')
                });
            }else{
                picCode=drawPic();
                $("#picCode").val("");
                $("#mobileCode").val("");
                layer.alert(data.message);
            }
        });

    }
}*/

function setPwd(){
    var oldPwd=$("#oldPwd").val();
    var pwd=$("#pwd").val();
    var isPwd=$("#isPwd").val();
    if(pwd != isPwd){
        //tips层-右
        $("#pwd").val("");
        $("#isPwd").val("");
        layer.tips("两次输入的密码不一致", '#isPwd', {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    if("ok" != ValidateUtils.checkSimplePassword(oldPwd) ) {
        $("#oldPwd").val("");
        layer.tips("密码格式有误，请您重新输入", "#oldPwd", {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    if("ok" != ValidateUtils.checkSimplePassword(pwd) ) {
        $("#pwd").val("");
        layer.tips("密码格式有误，请您重新输入", "#pwd", {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    if("ok" != ValidateUtils.checkSimplePassword(isPwd)){
        //tips层-右
        $("#isPwd").val("");
        layer.tips("密码格式有误，请您重新输入", "#isPwd", {
            tips: [2, '#78BA32'], //还可配置颜色
            tipsMore: true
        });
        return false;
    }
    $.post("/user/setPwd",{"oldPwd":oldPwd, "pwd":pwd, "isPwd":isPwd},function(data){
        console.log("data:"+data);
        if(data.code=="1000"){
            layer.msg(data.message, function () {
                layer.closeAll();
                window.location.href="/logout";
            });
            /*layer.alert("操作成功",function () {
                layer.closeAll();
                window.location.href="/logout";
            });*/
        }else{
            layer.msg(data.message);
            //layer.msg(data.message)//在更改密码之后，弹出修改成功1.5秒后，关闭修改密码的弹窗
            /*layer.alert(data.message,time{1.5},function () {
                layer.closeAll();
                //window.location.href="/index";
            });*/
        }
    });
}
