//功能按钮角色验证的初始化函数
var a;
var init = function(iframId,r_v_data){
	a = r_v_data;
	get_Ifram(iframId);
	
}
var  nodes = [];

var get_Ifram = function(iframId){
	var iframe = document.getElementById(iframId);
	if (iframe.attachEvent) {
	    iframe.attachEvent("onload", function () {
	    	get_R_V(iframe.contentWindow.document);
	    });
	} else {
	    iframe.onload = function () {
	    	get_R_V(iframe.contentWindow.document);
	    };
	}
}

var get_R_V = function(obj){
	dfs(obj.body);
}

function dfs(node) {
	if(node!=null){
	    if(node.nodeType === 1) {
	        var tagName = node.tagName;
	        if(node.getAttribute('R_V') != null){
	        	 if(a.indexOf(node.getAttribute('R_V'))==-1){
	        		 if(isIE()||isIE11()){
	        			 node.removeNode(true); 
	        		 }else{
	        			 node.remove();
	        		 }
	             }
	        }
	        
	        var children = node.childNodes;
	        for(var i = 0, len = children.length; i < len; i++) {
	            dfs(children[i]);
	        }
	    }
	}
}

function isIE(){
	if(!!window.ActiveXObject || "ActiveXObject" in window){
		return true;
	}else{
		return false;
	}
} 
function isIE11(){
	if((/Trident\/7\./).test(navigator.userAgent)){
	   return true;
	}else{
		return false;
	}
}
