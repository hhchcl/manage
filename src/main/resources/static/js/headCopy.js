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
    });
})
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
                        var dd=$("<dd><a style='margin-left: 10px' id='"+childArry[y].page+"' target='main_self_frame' href='"+childArry[y].page+"' onclick=addTab('" + childArry[y].name + "','" + childArry[y].page + "')>"+childArry[y].name+"</a></dd>");
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
/*function updateUsePwd(){
    layer.open({
        type:1,
        title: "修改密码",
        fixed:false,
        resize :false,
        shadeClose: true,
        area: ['450px'],
        content:$('#pwdDiv'),//content:$('#useDetail')
        skin:"layui-layer-molv demo-class"
    });
}*/

//添加选项卡
function addTab(name, url) {
    //触发事件
    var active = {
        //在这里给active绑定几项事件，后面可通过active调用这些事件
        tabChange: function(id,url) {
            //切换到指定Tab项
            layui.element.tabChange('layadmin-layout-tabs', url); //根据传入的id传入到指定的tab项
            layer.alert("切换"+url);
        },
        tabDelete: function (id,url) {
            layui.element.tabDelete("layadmin-layout-tabs", url);//删除
        }
        , tabDeleteAll: function (ids) {//删除所有
            $.each(ids, function (i,item) {
                layui.element.tabDelete("layadmin-layout-tabs", item); //ids是一个数组，里面存放了多个id，调用tabDelete方法分别删除
            })
        }
    };

    if(layui.jquery(".layui-tab-title li[lay-id='" + url + "']").length > 0) {
        //选项卡已经存在
        active.tabChange('layadmin-layout-tabs', url);
    } else {
        //动态控制iframe高度;
        //var tabheight = layui.jquery(window).height() - 95;
        var div=$("<div class='layadmin-tabsbody-item layui-show' id='LAY_app_body_item'></div>");
        var contentTxt = '<iframe name="main_self_frame" src=' + url + ' scrolling="no" style="width:100%;height:99%" scrolling="auto" frameborder="0" class="layadmin-iframe"></iframe>';
        div.append(contentTxt);
        var title = '<span>'+name+'</span>';

        //否则判断该tab项是否以及存在
        var isData = false; //初始化一个标志，为false说明未打开该tab项 为true则说明已有
        $.each($(".layui-tab-title li[lay-id='" + url + "']"), function () {

        //如果点击左侧菜单栏所传入的id 在右侧tab项中的lay-id属性可以找到，则说明该tab项已经打开
            if ($(this).attr("lay-id") == $(".layui-nav-child").attr("id")) {
                isData = true;
            }
        });
        if (isData == false) {
        //标志为false 新增一个tab项
            //新增一个Tab项
            layui.element.tabAdd('layadmin-layout-tabs', {
                title: title,
                content: div,
                id: url
            });
            //alert("新增");
        };

        $(".LAY_app_body").append(div);
    };

}

// 选项卡点击事件
function tabClick(name,url) {

}


