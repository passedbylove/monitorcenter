layui.use(['form','table','laydate','jquery','treetable'], function() {
    var laydate = layui.laydate,
        $ = layui.jquery,
        form = layui.form,
        table = layui.table
    treetable = layui.treetable;
    // //搜索处理
    // $("#search_btn").click(function (e) {
    //     var searchValue = $("#searchValue").val();
    //     location.href = "./tolist?searchValue="+searchValue;
    // });
    // function reverseMask(){
    //   if ($("#layui-layer-mask").length >0){
    //       $("#layui-layer-mask").remove();
    //   }else{
    //       $("body").append("<div class=\"layui-layer-shade\" id=\"layui-layer-mask\" times=\"18\" style=\"z-index: 100; background-color: rgb(0, 0, 0); opacity: 0.4;\"></div>");
    //   }
    // }
    $("#add_btn").click(function (e) {
        location.href ="./toadd";
    })
    //注册行点击事件捕捉
    function addBtnClick() {
        $("span[event='row_click']").click(function (e) {
            debugger;
            var target = $(e.target);
            var handle = target.data("handle");
            handle = !handle?target.parent().data("handle"):handle;
            if (!!handle) {
                var func = eval(handle);
                if (func instanceof Function){
                    var json = $(this).data('json');
                    func.apply(this,[json,target]);
                }
            }
        })
    }
    //操作方法
    //重新发布
    function redeploy(row){
        var formData = {};
        formData.id = row.id;
        formData.serverIp = row.serverIp;
        formData.gzId = row.gzId;
        layer.load(2);
        $.post("redeploy",formData,function (res) {
            if (res.code == "1"){
                // location.reload();
            }
        });
        showExecMessage(row.id);
    }
    //回退
    function rollback(row) {
        modifyGzOpen(row,"old-gzs","update");
    }
    //更新
    function update(row) {
        modifyGzOpen(row,"new-gzs","update");
    }
    function modifyGzOpen(param,selectUrl,submitUrl) {
        var serverGz = param["serverGz"];
        // var groupCode = !!serverGz?serverGz["groupCode"]:null;
        var parentId = !!serverGz?serverGz["parentId"]:null;
        var version = !!serverGz?serverGz["version"]:null;
        var id = param["id"];
        var serverIp = param["serverIp"];
        function buildRadioHtml(list){
            var html = "<input type='hidden' name='id' value='"+id+"'>";
            html += "<input type='hidden' name='serverIp' value='"+serverIp+"'>";
            list.forEach(function (item) {
                var value = item.id;
                var name = item.name + "-" + item.abbreviate + "-版本：" + item.version;
                var itemHtml = "<div class=\"layui-input-block\" style='margin-left: 5%;'" +
                    "><input type=\"radio\" name=\"gzId\" value=\""+value+"\" title=\""+name+"\" checked></div>";
                html += itemHtml;
            });
            return html;
        }
        layer.open({
            content:  $("#update_content_wrapper").html(),
            area: ['700px', '500px'],//自定义文本域宽高
            scrollbar: false,
            success: function(layero, index){
                var list = null;
                if (!!parentId&&!!version){
                    layer.load(2);
                    selectUrl += "?parentId="+parentId+"&version="+version;
                    $.get(selectUrl,function (res) {
                        if (!!res.code) {
                            var list = res.listData
                            if (!!list&&list.length>0){
                                var html = buildRadioHtml(list);
                                $(layero).find("#gz_modify").html(html);
                                form.render(); //更新全部
                            }else{
                                $(layero).find("#gz_modify").html("<p>没有可供选择的版本</p>");
                            }
                        }
                        layer.closeAll('loading');
                    });
                }else {
                    $(layero).find("#gz_modify").html("<p>没有可供选择的版本</p>");
                }
            },
            yes: function(index, layero){
                var id = $(layero).find(".layui-form").find("input[name = 'id']").val();
                if (!!id){
                    layer.load(2);
                    // var form = $(layero).find(".layui-form").attr("action",submitUrl);
                    var form = $(layero).find(".layui-form");
                    // $(layero).find(".layui-form").submit();
                    $.ajax({
                        url:submitUrl,
                        type:"post",
                        dataType:"json",
                        data:form.serializeArray(),
                        contentType:"application/x-www-form-urlencoded",
                        success:function(data){
                            if (!!data.code) {
                                console.log(data);
                            }
                        }
                    });
                    showExecMessage(id);
                }
                layer.close(index); //如果设定了yes回调，需进行手工关闭
            }
        });
    }
    function showExecMessage(id) {
        layer.open({
            title: "执行命令输出结果",
            type: 2,
            area: ['800px', '600px'],
            content: 'to-exec-msg?id='+id,
            end: function(layero, index){
                location.reload();
            }
        });
    }
    //卸载
    function uninstall(row) {
        layer.load(2);
        $.get(
            "uninstall/"+row.id,
            function (res) {
                if (!!res.code){
                    // layer.msg(res.msg);
                    // setTimeout(function () {
                    //     location.reload();
                    // },500);
                }
            }
        );
        showExecMessage(row.id);
    }
    //删除
    function remove(row){
        layer.confirm('删除后将不可恢复,确定?', function(index){
            layer.load(2);
            $.get(
                "delete/"+row.id,
                function (res) {
                    if (!!res.code){
                        layer.msg(res.msg);
                        setTimeout(function () {
                            location.reload();
                        },500)
                    }
                }
            );
        });
    }
    // 渲染表格
    var renderTable = function () {
        layer.load(2);
        treetable.render({
            treeColIndex: 0,
            treeSpid: -1,
            treeIdName: 'id',
            treePidName: 'pid',
            treeDefaultClose: false,
                treeLinkage: false,
            elem: '#table',
            data:treeJson,
            page: false,
            cols: [[
                {field: 'serverName', title: '服务名'},
                {field: 'version', title: '版本',templet:function (d) {
                    if (d.pid != -1){
                        var serverGz = d["serverGz"];
                        return !!serverGz ? serverGz["name"]+" version:"+serverGz["version"]:"";
                    }
                    return d["version"];
                }},
                {field: 'status', title: '状态',templet:function (d) {
                        var msg = "";
                        switch (d.status) {
                            case 0: msg = "部署失败";break;
                            case 1: msg = "部署成功";break;
                            case 2: msg = "卸载失败";break;
                            case 3: msg = "卸载成功";break;
                            default: msg = "";
                        }
                        return msg;
                }},
                {templet: '#oper-col', title: '操作'}
            ]],
            done: function () {
                addBtnClick();
                layer.closeAll('loading');
            }
        });
    };
    renderTable();
})