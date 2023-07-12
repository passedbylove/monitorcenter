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

var customer = {
    index: null,
    returnURL: function (url) {
        return /*[[@{*/url/*}]]*/;
    },
    targer_url: function (url) {
        location.href = customer.returnURL(url);
    },
    fh:function(){
        location.href = customer.returnURL("/group/list");
    },
    del: function (id) {
        layer.confirm('确定要删除此分组？', {
            btn: ['确定', '取消'] //按钮
        }, function () {
            $.post("/group/delete/" + id,
                {_method: "DELETE"},
                function (resp) {
                    if (resp.code==="SUCCESS") {
                        layer.alert("删除成功！",function(){
                            customer.fh();
                        });
                    } else if(resp.code === "ERROR") {
                        layer.alert(resp.msg);
                    }else{
                        window.location.reload();
                    }
                })
        });
    }
};