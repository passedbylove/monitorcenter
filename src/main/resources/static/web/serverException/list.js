layui.use(['form', 'layedit', 'laydate','table','laypage'], function(){
    var form = layui.form
        ,layer = layui.layer
        ,layedit = layui.layedit
        ,laydate = layui.laydate;
    form.render();

});
var customer = {
    index:null,
    returnURL:function(url){
        return /*[[@{*/url/*}]]*/;
    },
    targer_url:function(url){
        location.href = customer.returnURL(url);
    }

}

function check(id) {
    layui.use("layer",function(){
        ol = parent.layer.open({
            type:2,
            title:'查看服务异常报警',
            closeBtn:1,
            area:['700px','400px'],
            content:[customer.returnURL('/serverException/view?id='+id),'no'],
            end:function(){
                location.reload();
            }
        });

    });

}

$(function () {
    layui.use(['form', 'layedit', 'laydate','table','laypage'], function(){
        layui.laydate.render({
            elem:"#startTime",
            type:"datetime"
        });
        layui.laydate.render({
            elem:"#endTime",
            type:"datetime"
        });

    });

});
