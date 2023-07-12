layui.use(['form', 'layedit', 'laydate'], function () {
    var form = layui.form
        , layer = layui.layer
        , layedit = layui.layedit
        , laydate = layui.laydate;

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
    del: function (id) {
        layer.confirm('确定要删除此信息？', {
            btn: ['确定', '取消'] //按钮
        }, function () {
            $.post("/serverScript/" + id,
                {_method: "DELETE"},
                function (resp) {
                    if(resp.code == 'SUCCESS'){
                        location.reload();
                    }else{
                        layer.alert(resp.msg);
                    }
                });
        });
    },
    edit: function (serverId) {
        customer.targer_url('/serverScript/' + serverId);
    },
    add: function () {
        customer.targer_url('/serverScript/add');
    }
};