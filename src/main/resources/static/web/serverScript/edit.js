layui.use(['form', 'layedit', 'laydate'], function () {
    var form = layui.form
        , layer = layui.layer
        , layedit = layui.layedit
        , laydate = layui.laydate;
    laydate.render({
        elem: '#fdate'
    });
    form.render();
});

var tbyoung = {
    index: null,
    returnURL: function (url) {
        return /*[[@{*/url/*}]]*/;
    },
    fh: function () {
        location.href = tbyoung.returnURL("/serverScript/list");
    },
    submit: function () {
        var data = {
            scriptName: "input NotNull",
            groupId: "select NotNull"
        };
        var flag = V_CHECK(data);
        if (flag) {
            var index = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });
            var formData = new FormData( $("#form")[0]);

            $.ajax({
                type:"POST",
                url:"/serverScript?_method=PUT",
                dataType:"json",
                data:formData,
                cache:false,
                processData:false,
                success:function (result) {
                    layer.close(index);
                    if(result.code == 'SUCCESS'){
                        layer.alert(result.msg,function () {
                            tbyoung.fh();
                        });

                    }else{
                        layer.alert(result.msg);
                    }
                },
                contentType:false
            });

        }
    },

    chooseFile:function () {
        $("#scriptFile").click();
    },
    fileChange:function (obj) {
        $("#fileName").val(obj.files[0].name);
    },
    download:function(id){
        location.href =tbyoung.returnURL('/serverScript/download?id='+id);

    }
};