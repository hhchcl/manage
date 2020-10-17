/**
 * 菜单
 * */
//获取路径uri
var pathUri=window.location.href;

// console.log("pathUrl:"+pathUri);
$(function(){
    layui.use('element', function(){
        var element = layui.element;
        var $ = layui.jquery;

        // 左侧导航区域（可配合layui已有的垂直导航）
        $.get("/auth/getUserPerms",function(data){
            if(data!=null){
                getMenus(data);
                element.render('nav');
            }else{
                layer.alert("权限不足，请联系管理员",function () {
                    //退出
                    window.location.href="/logout";
                });
            }
        });

        // 左侧菜单dd标签点击事件，.huan-menu为每个dd标签的class
        $("body").on("click", ".huan-menu", function (e) {
            var url="";
            url = $(this).data("url");
            var id = $(this).data("id");
            var text = $(this).text();

            //如果不存在，就在右侧选项卡中增加一个选项
            if ($(".layui-tab-title").find("li[lay-id='" + id + "']").length <= 0) {
                var height = document.documentElement.clientHeight - 160 + "px";
                element.tabAdd('demo', {
                    title: text,
                    //iframe子页面onload新增监听src为/loginPage是父页面跳转/loginPage，解决“俄罗斯套娃”问题
                    content: "<iframe id='main_self_frame' name='main_self_frame' class='main_self_frame' width='100%' height='" + (height) + "' frameborder='0' scrolling='auto' src='" + url + "'></iframe>",
                    id: id
                });
            }
            // 如果存在，就切换到选项卡对应对应的选项
            element.tabChange('demo', id);
        });

        // 监听home.html选项卡lay-filter="demo"
        element.on('tab(demo)', function(data){
            var layId = $(this).attr('lay-id');
            // 将选中的选项卡的layId赋值给iframe的src
            document.getElementById("main_self_frame").src=layId;
            var menuDd = $(".layui-nav-tree .layui-nav-item .layui-nav-child dd");
            $.each($(".huan-menu"), function () {
                //如果点击左侧菜单栏所传入的id 在右侧tab项中的lay-id属性可以找到，则说明该tab项已经打开
                var id = $(this).data("id");
                if (layId == id) {
                    if(menuDd.find("a[id='"+layId+"']").length <= 0) {
                        menuDd.find("a[id !='"+layId+"']").parent().removeClass("layui-this");
                        menuDd.find("a[id !='"+layId+"']").parents("dl").removeClass("layui-nav-itemed");
                        menuDd.find("a[id !='"+layId+"']").parents("li").removeClass("layui-nav-itemed");
                    }
                    // 先移除原来菜单和选项卡选项的效果，不然会叠加选中，之后再具体选中具体东西
                    menuDd.removeClass("layui-this");
                    menuDd.find("a[id='"+layId+"']").parents("dl").removeClass("layui-nav-itemed");
                    menuDd.find("a[id='"+layId+"']").parents("li").removeClass("layui-nav-itemed");
                    menuDd.find("a[id='"+layId+"']").parents("dl").addClass("layui-nav-itemed");
                    menuDd.find("a[id='"+layId+"']").parents("li").addClass("layui-nav-itemed");
                    menuDd.find("a[id='"+layId+"']").parent().addClass("layui-this");
                    // 找到选项卡对应左侧的菜单就中断遍历
                    return false;
                } else {
                    menuDd.find("a[id !='"+layId+"']").parent().removeClass("layui-this");
                    menuDd.find("a[id !='"+layId+"']").parents("dl").removeClass("layui-nav-itemed");
                    menuDd.find("a[id !='"+layId+"']").parents("li").removeClass("layui-nav-itemed");
                    return true;
                }
            });
        });
    });
});

var getMenus=function(data){

    //回显选中
    var ul=$("<ul class='layui-nav layui-nav-tree' lay-shrink='all' lay-filter='test'></ul>");
    for(var i=0;i < data.length;i++){
        var node=data[i];
        if( node.istype =="0"){
            if(node.pid == "0"){
                var li=$("<li class='layui-nav-item' name='"+node.id+"'></li>");
                //父级无page
                var a=$("<a class='' href='javascript:;' lay-direction='2'><i class='layui-icon + "+node.icon+"' style='margin-right: 10px'></i><cite>"+node.name+"</cite></a>");
                li.append(a);
                //获取子节点
                var childArry = getParentArry(node.id, data);
                if(childArry.length>0){
                    a.append("<span class='layui-nav-more'></span>");
                    var dl=$("<dl class='layui-nav-child'></dl>");
                    for (var y in childArry) {
                        var dd=$("<dd class='"+childArry[y].page+"'><a style='margin-left: 10px' data-url='"+childArry[y].page+"' data-id='"+childArry[y].page+"' class='huan-menu'  target='main_self_frame' id='"+childArry[y].page+"' href='"+childArry[y].page+"'>"+childArry[y].name+"</a></dd>");
                        //判断选中状态
                        if(pathUri.indexOf(childArry[y].page)>0){
                            li.addClass("layui-nav-itemed");
                            //li.setAttribute("lay-attr",childArry[y].page);
                            dd.addClass("layui-this");
                        }
                        //TODO 由于layui菜单不是规范统一的，多级菜单需要手动更改样式实现；
                        dl.append(dd);
                    }
                    li.append(dl);
                }
                ul.append(li);
            }
        }
    }
    $(".layui-side-scroll").append(ul);
}

//根据菜单主键id获取下级菜单
//id：菜单主键id
//arry：菜单数组信息
function getParentArry(id, arry) {
    var newArry = new Array();
    for (var x in arry) {
        if (arry[x].pid == id)
            newArry.push(arry[x]);
    }
    return newArry;
}

function updateUsePwd(){
    layer.open({
        type:1,
        title: "修改密码",
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['450px'],
        content:$('#pwdDiv'),/*content:$('#useDetail')*/
        skin:"layui-layer-molv demo-class"
    });
}

// 删除当前tab
function closeThisTabs() {
    $.each($(".layui-tab-title li"), function () {
        //tab项中的lay-id属性
        var tabId = $(this).attr("lay-id");
        $.each($(".huan-menu"), function () {
            // 左侧菜单栏
            var menuId = $(this).data("id");
            if (tabId == menuId && $(".layui-nav-tree .layui-nav-item .layui-nav-child dd").find("a[id='"+tabId+"']").parent().hasClass("layui-this")) {
                layui.element.tabDelete("demo", tabId);
            }
        });
    });
}

// 删除其他tab
function closeOtherTabs() {
    $.each($(".layui-tab-title li"), function () {
        //tab项中的lay-id属性
        var tabId = $(this).attr("lay-id");
        $.each($(".huan-menu"), function () {
            // 左侧菜单栏
            var menuId = $(this).data("id");
            if ($(".layui-nav-tree .layui-nav-item .layui-nav-child dd").find("a[id ='"+tabId+"']").parent().hasClass("layui-this")) {
                return true;
            } else {
                if (tabId == "/home") {
                    return true;
                }
                layui.element.tabDelete("demo", tabId);
            }
        });
    });
}

// 删除全部tab
function closeAllTabs() {
    $.each($(".layui-tab-title li"), function () {
        // tab项中的lay-id属性
        var tabId = $(this).attr("lay-id");
        $.each($(".huan-menu"), function () {
            // 左侧菜单栏
            var menuId = $(this).data("id");
            if (tabId == menuId) {
                layui.element.tabDelete("demo", tabId);
            }
        });
    });
}

// 刷新当前页
function refresh() {
    $.each($(".huan-menu"), function () {
        // 左侧菜单栏
        var menuId = $(this).data("id");
        if ($(".layui-nav-tree .layui-nav-item .layui-nav-child dd").find("a[id ='"+menuId+"']").parent().hasClass("layui-this")) {
            location.reload();
        }
    });
}

// 基本资料
function personalData() {
    $.each($(".huan-menu"), function () {
        // 左侧菜单栏
        var menuId = $(this).data("id");
        var text = $(this).text();
        var menuDd = $(".layui-nav-tree .layui-nav-item .layui-nav-child dd");
        if (menuId == "/user/personalData") {
            menuDd.removeClass("layui-this");
            menuDd.find("a[id='"+menuId+"']").parents("dl").removeClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("li").removeClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("dl").addClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("li").addClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parent().addClass("layui-this");
            //如果不存在，就在右侧选项卡中增加一个选项
            if ($(".layui-tab-title").find("li[lay-id='" + menuId + "']").length <= 0) {
                var height = document.documentElement.clientHeight - 160 + "px";
                layui.element.tabAdd('demo', {
                    title: text,
                    //iframe子页面onload新增监听src为/loginPage是父页面跳转/loginPage，解决“俄罗斯套娃”问题
                    content: "<iframe id='main_self_frame' name='main_self_frame' class='main_self_frame' width='100%' height='" + (height) + "' frameborder='0' scrolling='auto' src='" + menuId + "'></iframe>",
                    id: menuId
                });
            }
            // 如果存在，就切换到选项卡对应对应的选项
            layui.element.tabChange('demo', menuId);
        }
    });
}

// 修改密码
function updateUsePwd() {
    $.each($(".huan-menu"), function () {
        // 左侧菜单栏
        var menuId = $(this).data("id");
        var text = $(this).text();
        var menuDd = $(".layui-nav-tree .layui-nav-item .layui-nav-child dd");
        if (menuId == "/user/updateUsePwd") {
            menuDd.removeClass("layui-this");
            menuDd.find("a[id='"+menuId+"']").parents("dl").removeClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("li").removeClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("dl").addClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parents("li").addClass("layui-nav-itemed");
            menuDd.find("a[id='"+menuId+"']").parent().addClass("layui-this");
            //如果不存在，就在右侧选项卡中增加一个选项
            if ($(".layui-tab-title").find("li[lay-id='" + menuId + "']").length <= 0) {
                var height = document.documentElement.clientHeight - 160 + "px";
                layui.element.tabAdd('demo', {
                    title: text,
                    //iframe子页面onload新增监听src为/loginPage是父页面跳转/loginPage，解决“俄罗斯套娃”问题
                    content: "<iframe id='main_self_frame' name='main_self_frame' class='main_self_frame' width='100%' height='" + (height) + "' frameborder='0' scrolling='auto' src='" + menuId + "'></iframe>",
                    id: menuId
                });
            }
            // 如果存在，就切换到选项卡对应对应的选项
            layui.element.tabChange('demo', menuId);
        }
    });
}