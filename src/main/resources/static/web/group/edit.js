layui.use(['form', 'layedit', 'laydate'], function(){
    var form = layui.form
        ,layer = layui.layer
        ,layedit = layui.layedit
        ,laydate = layui.laydate;

    laydate.render({
        elem: '#fdate'
    });
    form.render();
});
var tbyoung = {
    index:null,
    returnURL:function(url){
        return /*[[@{*/url/*}]]*/;
    },
    fh:function(){
        location.href = tbyoung.returnURL("/group/list");
    },
    submit:function(){
        var data = {name:"input NotNull",code:"input NotNull",type:"input NotNull"};
        var flag = V_CHECK(data);
        if(flag){
            var index = layer.load(1, {
                shade: [0.1,'#fff'] //0.1透明度的白色背景
            });
            $.ajax({
                type: 'get',
                data: $('#form').serialize(),
                url:  document.getElementById("form").action,
                cache:false,
                dataType:'json',
                success: function (data) {
                    layer.close(index);
                    if(data.code === "ERROR"){
                        layer.alert(data.msg);
                    }else if(data.code === "SUCCESS"){
                        layer.alert(data.msg,function(){
                            tbyoung.fh();
                        });
                    }else{
                        window.location.reload();
                    }

                }
            })
        }

    }
}

