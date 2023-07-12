var setting = {
    view: {
        dblClickExpand: false
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    callback: {
        beforeClick: beforeClick,
        onClick: onClick
    }
};

var treeObj;

function beforeClick(treeId, treeNode) {
    var check = treeNode.pId == null;
    if (check) layer.alert("不能选择根节点！");
    return !check;
}

function onClick(e, treeId, treeNode) {
    var cityObj = $("#groupname");
    cityObj.attr("value", treeNode.getParentNode().name+"-"+treeNode.name);
    var groupid = $("#groupid");
    groupid.attr("value", treeNode.id);
}

function showMenu() {
    var cityObj = $("#groupname");
    var cityOffset = $("#groupname").offset();
    $("#menuContent").css({left:cityOffset.left + "px", top:cityOffset.top + cityObj.outerHeight() + "px"}).slideDown("fast");

    $("body").bind("mousedown", onBodyDown);
}
function hideMenu() {
    $("#menuContent").fadeOut("fast");
    $("body").unbind("mousedown", onBodyDown);
}
function onBodyDown(event) {
    if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length>0)) {
        hideMenu();
    }
}
function treeClear(){
    var cityObj = $("#groupname");
    cityObj.attr("value", "");
    var groupid = $("#groupid");
    groupid.attr("value", "");
}

$(document).ready(function(){
    $.ajax({
        url: "/groups/ztree",
        type: "GET",
        dataType: 'json',
        success: function(result){
            $.fn.zTree.init($("#treeDemo"), setting, result);
            treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.expandAll(true);
        }
    });
});