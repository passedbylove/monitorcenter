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

String.prototype.endWith=function(endStr){
    var d=this.length-endStr.length;
    return (d>=0&&this.lastIndexOf(endStr)==d)
}

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
            scriptId: "select NotNull",
            serverId:"select NotNull"
        };
        
        var flag = V_CHECK(data);
        if (flag) {
            var index = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });

            $.ajax({
                type:"POST",
                url:"/serverScript/start",
                data:$("#form").serialize(),
                dataType:"json",
                cache:false,
                success:function (result) {
                    layer.close(index);
                    if(result.code == 'SUCCESS'){
                        layer.alert(result.msg,function () {
                            tbyoung.fh();
                        });

                    }else{
                        layer.alert(result.msg);
                    }
                }
            });

        }
    }
};