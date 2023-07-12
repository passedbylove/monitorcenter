layui.use(['form', 'jquery', 'treeSelect'], function () {
    var $ = layui.jquery,
        form = layui.form,
        treeSelect = layui.treeSelect;
    function disableServiceAdditional(){
        $('.service-additional').attr("disabled","disabled");
        $('.service-additional').val(null);
    }
    disableServiceAdditional();
    form.render();
    treeSelect.render({
        // 选择器
        elem: '#gzId',
        // 数据
        data: 'gzstree',
        // 异步加载方式：get/post，默认get
        type: 'get',
        // 占位符
        placeholder: '请选择安装包',
        // 是否开启搜索功能：true/false，默认false
        search: true,
        // 点击回调
        click: function (d) {
            if ("服务安装包" == d.current.groupCode){
                $('.service-additional').removeAttr("disabled");
            }else {
                disableServiceAdditional();
            }
            form.render();
        },
        // 加载完成后的回调函数
        success: function (d) {
            console.log(d);
        }
    });
    form.verify({
        //验证发布端口是否重复
        asyCheckServerPort: function (value, dom) {
            debugger;
            if (!!$(dom).attr("disabled")) {
                return;
            }
            if (!value) {
                return "服务发布端口不可为空";
            } else {
                if (!/^\d+$/.test(value)) {
                    return '只能填写数字';
                }
            }
            var msg;
            var id = $("select[name='serverId']").val();
            if (!id) {
                return "请选择服务器";
            }
            var url = "check-deployport?serverId=" + id + "&deployPort=" + value;
            $.ajax({
                async: false, // 使用同步的方法
                method: 'get',
                success: function (res) { //栗子，返回的数据结果：{"rel":true,"message":"用户名已存在！"}
                    if (res.code == "1" && !res.objData) {
                        msg = "服务器端口重复";
                    }
                },
                url: url
            });
            return msg;
        },
        checkFilePath: function (value, dom) {
            var filePathRegExp = /^([\/][\w-]+)*(\/)?$/i;
            if (!!value) {
                if (!filePathRegExp.test(value)) {
                    return "部署路径有误";
                }
            } else {
                return "部署路径不可为空";
            }

        }
    });
    form.on('submit(server_deploy)', function (data) {
        $.ajax({
            url:"add",
            type:"post",
            dataType:"json",
            data:$("#form").serializeArray(),
            contentType:"application/x-www-form-urlencoded",
            success:function(data){
                if (!!data.code) {
                    layer.open({
                        title: "执行命令输出结果",
                        type: 2,
                        area: ['800px', '600px'],
                        content: 'to-exec-msg?id='+data.objData,
                        success: function(layero, index){
                            console.log(layero, index);
                        }
                    });
                }
            }
        });
        return false;
    })
});

function returnURL(url) {
    return /*[[@{*/url/*}]]*/;
}

function fh() {
    location.href = returnURL("/server/tolist");
}