layui.use(['form', 'layedit', 'laydate','tree', 'layer'], function () {
    var form = layui.form
        , layer = layui.layer
        , layedit = layui.layedit
        , laydate = layui.laydate;

    var layer = layui.layer
        ,$ = layui.jquery;

    form.render();

    var setting = {
        check: {
            enable: true
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        edit: {
            enable: false
        },
        callback: {
            onClick: customer.onClick
        }
    };


    var layer = layui.layer
        ,$ = layui.jquery;

    var code = $("#code").val();

    $.ajax({
        type: "post",
        url: '/serviceGz/getTree',
        data:{code:code},
        dataType: 'json',
        success: function (data) {
            $.fn.zTree.init($("#gz"), setting, data.nodes);
        }
    })

});

var customer = {
    index:null,
    returnURL:function(url){
        return /*[[@{*/url/*}]]*/;
    },
    targer_url: function(url){
        location.href = customer.returnURL(url);
    },
    edit:function(id){
        location.href = customer.returnURL("/serviceGz/edit/" +id);
    },
    uploadFile:function(id){
        location.href = customer.returnURL("/serviceGz/uploadFile/" +id);
    },
    del:function (id) {

        layer.alert("确认删除？",function () {
            $.ajax({
                type: "post",
                url: '/serviceGz/del',
                data:{id:id},
                dataType: 'json',
                success: function (data) {
                    layer.alert(data.msg,function () {
                        location.href = customer.returnURL("/serviceGz/index");
                        layer.closeAll();

                    });
                }
            })

        });


    },
    onClick: function(event, treeId, node, clickFlag) {

        $("#detail tbody").html("");

        if(node.parentId == 0){

            var tr ="<tr><td>"

            if(node.children.length==0){
                tr = tr + "<a><i class=\"layui-icon\">&#xe623;</i></a> "
            }else{
                tr = tr + "<a><i class=\"layui-icon\">&#xe625;</i></a> "
            }

            tr = tr + node.name;
            tr = tr+"</td><td>"+(node.abbreviate==null?'':node.abbreviate);
            tr = tr+"</td><td>"+(node.version==null?'':node.version);
            tr = tr+"</td><td>"+(node.createTime==null?'':node.createTime);
            tr = tr+"</td><td>"+(node.gzUrl==null?'':node.gzUrl);
            tr = tr+"</td><td>"+(node.remark==null?'':node.remark);
            tr = tr+"</td><td>";

            tr = tr+"</td><td align=\"right\"> <a class=\"layui-btn layui-btn-sm layui-bg-orange\" title=\文件包上传\"  onclick='javascript:customer.uploadFile(\""+ node.id + "\");'>上传</a>";
            tr = tr+" <a class=\"layui-btn layui-btn-sm layui-bg-blue\" title=\"修改\"  onclick='javascript:customer.edit(\""+ node.id + "\");'>修改</a>";
            tr = tr+" <a class=\"layui-btn layui-btn-sm layui-bg-red\" title=\"删除\"  onclick='javascript:customer.del(\""+node.id+"\");'>删除</a>";
            tr = tr+"</td><tr>";


            $("#detail").append(tr)

            for(var i=0;i <= node.children.length ;i++){

                var tr = "<tr><td>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" +node.children[i].alias;
                tr = tr+"</td><td>"+node.children[i].abbreviate;
                tr = tr+"</td><td>"+node.children[i].version;
                tr = tr+"</td><td>"+node.children[i].createTime;
                tr = tr+"</td><td>"+node.children[i].gzUrl;
                tr = tr+"</td><td>"+node.children[i].remark;
                if(node.children[i].status == 0) {
                    tr = tr+"</td><td ><span style=\"color: green\">已部署</span>";
                }else{
                    tr = tr+"</td><td><span style=\"color: red\">未部署</span>";
                }
                tr = tr+"</td><td align=\"right\">  <a class=\"layui-btn layui-btn-sm layui-bg-blue\" title=\"修改\"  onclick='javascript:customer.edit(\""+ node.children[i].id + "\");'>修改</a>";
                tr = tr+" <a class=\"layui-btn layui-btn-sm layui-bg-red\" title=\"删除\"  onclick='javascript:customer.del(\""+node.children[i].id+"\");'>删除</a>";
                tr = tr+"</td><tr>";
                $("#detail").append(tr)
            }

        }else{

            var tr ="<tr><td>"+node.alias;
            tr = tr+"</td><td>"+node.abbreviate;
            tr = tr+"</td><td>"+node.version;
            tr = tr+"</td><td>"+node.createTime;
            tr = tr+"</td><td>"+node.gzUrl;
            tr = tr+"</td><td>"+node.remark;
            if(node.status == 0) {
                tr = tr+"</td><td><span style=\"color: green\">已部署</span>";
            }else{
                tr = tr+"</td><td><span style=\"color: red\">未部署</span>";
            }
            tr = tr+"</td><td>  <a class=\"layui-btn layui-btn-sm layui-bg-blue\" title=\"修改\"  onclick='javascript:customer.edit(\""+ node.id + "\");'>  <i class=\"layui-icon\">&#xe642;</i></a>";
            tr = tr+" <a class=\"layui-btn layui-btn-sm layui-bg-red\" title=\"删除\"  onclick='javascript:customer.del(\""+node.id+"\");'><i class=\"layui-icon\">&#xe640;</i> </a>";
            tr = tr+"</td><tr>";
            $("#detail").append(tr)

        }
    }

}

