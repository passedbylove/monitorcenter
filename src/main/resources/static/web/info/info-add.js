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
        location.href = tbyoung.returnURL("/info/list");
    },
    submit: function () {
        var data = {
            ipAddress: "input NotNull",
            linkPort: "input NotNull",
            userName: "input NotNull",
            pwd: "input NotNull",
            username: "input NotNull",
            password: "input NotNull"
        };
        var flag = V_CHECK(data);
        if (flag) {
            var index = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });
            $.post('/info', $('#form').serialize(), function (resp) {
                layer.close(index);
                layer.alert(resp.responseText);
                tbyoung.fh();
            }).fail(function (resp) {
                layer.close(index);
                layer.alert(resp.responseText);
            });
        }
    }
};