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
    start: function (id,status,runStatus) {
        if (id == null || id == '') {
            layer.alert("服务部署ID为空，操作失败");
            return;
        }
        if (status !== 1) {
            layer.alert("当前服务器部署状态异常，无法进行操作");
            return;
        }
        if (runStatus == 1) {
            layer.alert("当前服务已启动");
            return;
        }

        let index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/serverManage/start/" + id,
            function (resp) {
                layer.alert(resp.responseText);
                layer.close(index);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp.responseText);
        })
    },
    stop: function (id,status,runStatus) {
        if (id == null || id == '') {
            layer.alert("服务部署ID为空，操作失败");
            return;
        }
        if (status !== 1) {
            layer.alert("当前服务器部署状态异常，无法进行操作");
            return;
        }

        if (runStatus == 2) {
            layer.alert("当前服务未运行");
            return;
        }

        let index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/serverManage/stop/" + id,
            function (resp) {
                layer.alert(resp);
                layer.close(index);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp.responseText);
        })
    },
    restart: function (id,status) {
        if (id == null || id == '') {
            layer.alert("服务部署ID为空，操作失败");
            return;
        }
        if (status !== 1) {
            layer.alert("当前服务器部署状态异常，无法进行操作");
            return;
        }
        let index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/serverManage/restart/" + id,
            function (resp) {
                layer.alert(resp);
                layer.close(index);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp.responseText);
            window.location.reload();
        })
    },
    check: function (id,status) {
        if (id == null || id == '') {
            layer.alert("服务部署ID为空，操作失败");
            return;
        }
        if (status !== 1) {
            layer.alert("当前服务器部署状态异常，无法进行操作");
            return;
        }
        let index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/serverManage/check/" + id,
            function (resp) {
                layer.alert(resp);
                layer.close(index);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp.responseText);
            window.location.reload();
        })
    },
    openFireWall:function (id,status) {
        if (id == null || id == '') {
            layer.alert("服务部署ID为空，操作失败");
            return;
        }
        let index = layer.load(1, {
            shade: [0.1, '#fff'] //0.1透明度的白色背景
        });
        $.post("/serverManage/openFireWall/" + id,
            function (resp) {
                layer.alert(resp);
                layer.close(index);
                window.location.reload();
            }).fail(function (resp) {
            layer.close(index);
            layer.alert(resp.responseText);
            window.location.reload();
        })
    }
};