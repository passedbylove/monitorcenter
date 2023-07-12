Array.prototype.pushArray = function(arr) {
    this.push.apply(this, arr);
};

//定时刷新
function autoRefresh() {
    setTimeout(function () {
        renderMemChart();
        renderTrafficChart();
        renderCPUChart();
        renderLoadChart();
    },60*1000)
}


function dataSourceBinding(domId,label,url) {
    var dom = document.getElementById(domId);
    var chart = echarts.init(dom);
    var app = {},option = {};
    var option = {
        title: {
            text: label
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data:[]
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        yAxis: {
            type: 'value'
        },
        series: []
    };
    $.ajax({
        type: "GET",
        url: url,
        dataType: 'json',
        async: 'true',
        success: function (response) {
            option.legend.data.pushArray(response.objData.legend);
            option.xAxis.data.pushArray(response.objData.xAxis);
            option.series.pushArray(response.objData.series);
            if (option && typeof option === "object") {
                chart.setOption(option, true);
            }
            console.log(JSON.stringify(option));
        }, error: function (err) {
            console.log(err.responseText);
        }
    });

}
function dataSourceBinding2(domId,label,url) {
    var chat = echarts.init(document.getElementById(domId));
    var option = {
        title: {
            text: label
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data:[]
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: []
        },
        yAxis: {
            type: 'value'
        },
        series: []
    };
    $.ajax({
        type: "GET",
        url: url,
        dataType: 'json',
        async: 'true',
        success: function (response) {
            option.legend.data.pushArray(response.objData.legend);
            option.xAxis.data.pushArray(response.objData.xAxis);
            option.series.pushArray(response.objData.series);
            chat.setOption(option);
            console.log(JSON.stringify(option));
        }, error: function (err) {
            console.log(err.responseText);
        }
    });
}

function renderMemChart()
{
    dataSourceBinding('memChart','内存使用率(单位:%)',"/perf/mem.json");
}

function renderCPUChart()
{
    dataSourceBinding('cpuChart','CPU使用率(单位:%)',"/perf/cpu.json");

}

function renderLoadChart()
{
    dataSourceBinding('loadChart','1分钟负载',"/perf/load.json");
}

function renderTrafficChart()
{
    dataSourceBinding('trafficChart','网口上行流量(单位:M)',"/perf/traffic.json");
}

$(function () {
    renderMemChart(),renderLoadChart(),renderCPUChart(),renderTrafficChart();
    autoRefresh();
})

