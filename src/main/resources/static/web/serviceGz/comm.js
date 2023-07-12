layui.use(['form', 'layedit', 'laydate','layer'], function () {
    var form = layui.form
        , layer = layui.layer
        , layedit = layui.layedit
        , laydate = layui.laydate;
});

var customer = {
    index:null,
    returnURL:function(url){
        return /*[[@{*/url/*}]]*/;
    },
    targer_url: function(url){
        location.href = customer.returnURL(url);
    },
    fh: function () {
        location.href = customer.returnURL("/serviceGz/index");
    },
    edit:function(id){
        location.href = customer.returnURL("/serviceGz/edit/" +id);
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
                        if(data.code == "SUCCESS"){
                            customer.fh();
                        }
                        layer.closeAll();

                    });
                }
            })
        });

    },
    editsubmit: function () {
            $.ajax({
                type: "post",
                url: '/serviceGz/editUpdate',
                data: {id:$("#id").val(),name:$("#name").val()},
                dataType: 'json',
                success: function (data) {
                    customer.fh();
                }
            })
     },
    addsubmit: function () {
        var data = {
            name: "input NotNull",
            remark: "input NotNull",
            abbreviate: "input NotNull English",
            code: "select NotNull"
        };
     /*   // var code = $("#code").val();
        // if(code ==''){
        //     layer.alert("请选择安装包类型");
        //     return;
        // }*/

        var flag = V_CHECK(data);
        if (flag) {
            var index = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });


            $.ajax({
                type: "post",
                url: '/serviceGz/addAction',
                data: $("#form").serialize(),
                dataType: 'json',
                success: function (data) {
                    layer.close(index);
                    customer.fh();
                }
            })
        }
    },
    uploadSubmit: function () {
        var data = {
            name: "input NotNull",
            fileName: "input NotNull",
            remark: "input NotNull",
            abbreviate: "input NotNull English"
        };
        var flag = V_CHECK(data);
        if (flag) {
            var index = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });
            var formData = new FormData( $("#form")[0]);

            $.ajax({
                type:"POST",
                url:"/serviceGz?_method=PUT",
                dataType:"json",
                data:formData,
                cache:false,
                processData:false,
                success:function (result) {
                    layer.close(index);
                    layer.alert(result.msg,function () {
                        if(result.code == "SUCCESS"){
                            customer.fh();
                        }
                        layer.closeAll();
                    });
                },
                contentType:false
            });

        }

    },
    chooseFile:function () {
        $("#gzFile").click();
    },
    fileChange:function (obj) {
        $("#fileName").val(obj.files[0].name);
    }

}

