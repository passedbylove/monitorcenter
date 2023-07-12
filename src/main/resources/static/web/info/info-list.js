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
    del: function (id) {
        layer.confirm('确定要删除此信息？', {
            btn: ['确定', '取消'] //按钮
        }, function () {
            $.post("/info/" + id,
                {_method: "DELETE"},
                function (resp) {
                    if (resp) {
                        layer.alert(resp);
                        window.location.reload();
                    } else {
                        layer.alert(resp);
                    }
                })
        });
    },
    edit: function (serverId) {
        customer.targer_url('/info/' + serverId);
    },
    add: function () {
        customer.targer_url('/info/add');
    },
    recover: function (serverId) {
        var index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/info/recover/" + serverId,
            {_method: "put"},
            function (resp) {
                layer.close(index);
                layer.alert(resp);
                window.location.reload();
            });
    },
    checkStatus: function (serverId) {
        var index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.get("/info/checkStatus/" + serverId,
            function (resp) {
                layer.close(index);
                layer.alert(resp);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp);
        });
    }
};