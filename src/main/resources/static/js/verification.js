//JS验证控件非空或者格式     
//---------------------------------调用过程---------------------------
//1.引用本JS文件 配合layer使用
//2.定义验证控件对象
//id 控件的ID 
//"input NotNull English" 验证内容空格隔开,第一个为控件属性 
//单选或者复选框 name要起名和ID一样
//var data = { //验证定义对象
//		id : "input NotNull English",
//		accp : "select NotNull",
//		sex : "radio NotNull"
//	}
//3.调用方法
//var flag = V_CHECK(data);
//flag 返回 false 验证不通过 
//flag 返回 true 验证通过 
//-----------------------控件定义可自定义新增编辑----------------------------
var type = {
		input : "文本",
		select:"下拉菜单选项",
		check:"复选按钮",
		radio:"单选框按钮",
		fileinput:"附件",
	}
//------------------------验证属性定义可自定义新增编辑------------------------
var vtype = {
	NotNull : "NotNull-不能为空！",//非空
	Number:"Number-请输入数字类型内容！",//数字
	Integer:"Integer-请输入整数！",//整数
	Double:"Double-请输入小数！",//小数
	Float2:"Float2-请输入不超过两位的小数！",//两位小数
	English:"English-请输入英文字母！",//英文字符
	Chinese:"Chinese-请输入中文！",//中文字符
	Float4:"Float4-请输入不超过四位的小数！",//四位小数
	Tel:"Tel-请输入正确的手机号码！",//手机号码
	Email:"Email-请输入电子邮箱！"//电子邮箱
}
//------------------------验证实现方式---------------------------------
function V_CHECK(obj){
	layer.closeAll('tips'); 
	var fun = true;
	$.each(obj,function(key,value){
		if(fun){
			fun = decompose(key,value);
		}else{
			decompose(key,value);
		}
	})
	return fun;
}
//分解
function decompose(key,value){
	var fun = true;
	var val  = value.split(" ");
	var one = val[0];
	$.each(val,function(value){
		if(value!=0){
			if(fun){
				fun = verification(key,one,val[value]);
			}else{
				verification(key,one,val[value]);
			}
		}
	})
	return fun;
}
function verification(id,stype,value){
	var data =  eval("vtype."+value).split("-");
	var fun = eval(data[0] + "('"+stype+"','"+id+"')");
	if(!fun){
		var a = eval("type."+stype);
        if(stype != 'input') {
            var o = layer.tips(a+data[1], $("#"+id).parent(), {
                tipsMore: true,
                tips: [1, '#EE0000'],
                time: 0
            });
            $("#"+id).parent().click(function () {
                layer.close(o);
            })
        } else {
            var o = layer.tips(a+data[1], "#"+id, {
                tipsMore: true,
                tips: [1, '#EE0000'],
                time: 0
            });
        }
		$("#"+id).click(function(){
			layer.close(o);
			if(stype == "select"){
				($("#" + id)).focus();
			}
		});
	}
    return fun;
}
//-----------------------------单独验证方法可自定义新增编辑--------------------
//非空验证
function NotNull(ctype,id){
	var value ="";
	if(ctype == "input" || ctype == "fileinput" ){
		value = $("#"+id).val();
		if(value==""){
			Focus(id);
			return false;
		}
	}else if(ctype == "select"){
		value = $("#"+id).val();
		if(value==""){
			return false;
		}
	}else if(ctype == "radio"  || ctype == "check"){
		value =  $('input:radio[name="'+id+'"]:checked').val();
		if(value==null){
			return false;
		}
	}
	return true;
}
//数字验证
function Number(ctype,id){
	var value = $("#"+id).val();
	if(isNaN(value)){
		Focus(id);
        return false;
    }
	return true;
}
//整数验证
function Integer(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^[0-9]*[1-9][0-9]*$/; 
	var regEnd = /^-[0-9]*[1-9][0-9]*$/;
    if(!regPos.test(value) && !regEnd.test(value)){
    	Focus(id);
        return false;
    }
	return true;
}
//小数验证
function Double(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^[-\+]?\d+(\.\d+)?$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}

function Float2(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^(0|[1-9]\d{0,3})(\.\d{0,2})?$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}

function Float4(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^(0|[1-9]\d{0,3})(\.\d{0,4})?$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}
function Tel(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^15|18|17|13|14\d{9}$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
	
}
//邮箱验证
function Email(ctype,id){
	var value = $("#"+id).val();
	var regPos = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}

function English(ctype,id){
	var value = $("#"+id).val();
	var regPos =  /^[A-Za-z]+$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}

function Chinese(ctype,id){
	var value = $("#"+id).val();
	var regPos =  /^[\u0391-\uFFE5]+$/; 
	if(!regPos.test(value)){
		Focus(id);
		return false;
	}
	return true;
}

//-----工具类-----
function Focus(id){
	$("#"+id).focus();
}