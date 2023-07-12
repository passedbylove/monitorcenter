var i=GetRandomSum(1,100);
var setting = {
		data: {
			simpleData: {
				enable: true
			}
		},
        view: {
            addDiyDom:addDiyDom
        }
	};

    
$(function(){
   loadTree(); 
});

//随机数
function GetRandomNum(Min,Max){
	var Range = Max - Min;   
	var Rand = Math.random();   
	return(Min + Math.round(Rand * Range));   
}
//反悔时间戳+随机数来保证唯一性
function GetRandomSum(Min,Max){
   	var i=GetRandomNum(Min,Max);
   	var a=new Date().getTime();
   	return a+""+i;
}

function checked(obj,treeId, treeNode) {
	if (treeNode.isParent) {
	    var childrenNodes = treeNode.children;
	    if (childrenNodes) {
	        for (var i = 0; i < childrenNodes.length; i++) {
	            $("#selAll" + childrenNodes[i].tId).prop("checked",obj.checked);
	            checked(obj,treeId, childrenNodes[i]);
	        }
	    }
	}

}

function addDiyDom(treeId,treeNode) {
	var  resobj = $("#resids").val().split(",");
	var  strCheck = "";
	for (var int = 0; int < resobj.length; int++) {
		if(treeNode.id == (resobj[int]*1)) strCheck = "checked";
	}
    if (treeNode.parentNode && treeNode.parentNode.id!=0) return;
    var aObj = $("#" + treeNode.tId + "_a");
    var editStr = "";
    if(treeNode.type == "1"){
        editStr += "<input id='selAll"+treeNode.tId+"' "+strCheck+" lay-ignore name='rolress["+treeNode.id+"].resroucesid' value='"+treeNode.id+"' type='checkbox'/> 主菜单";
    }else if(treeNode.type == "2"){
    	editStr += "<input id='selAll"+treeNode.tId+"' "+strCheck+" lay-ignore name='rolress["+treeNode.id+"].resroucesid' value='"+treeNode.id+"' type='checkbox'/> 功能菜单";
    }else if(treeNode.type != ""){
    	editStr += "<input id='selAll"+treeNode.tId+"' "+strCheck+" lay-ignore name='rolress["+treeNode.id+"].resroucesid' value='"+treeNode.id+"' type='checkbox'/>功能";
    }
    aObj.after(editStr);
    $("#selAll" + treeNode.tId).click(function(){
    	checked(this,treeId,treeNode);     
    });
   
}

function loadTree(){
var index = layer.load(1, {shade: false});
$.ajax({
    url: "/roles/ztree?r="+i,
    type: "GET",
    dataType: 'json',
    success: function(result){
    	$.fn.zTree.init($("#treeDemo"), setting, result);
    	var treeObj = $.fn.zTree.getZTreeObj("treeDemo"); 
    	treeObj.expandAll(true); 
    	layer.close(index);
    }
});

}