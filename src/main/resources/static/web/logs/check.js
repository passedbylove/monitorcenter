var tbyoung = {
    index: null,
    returnURL: function (url) {
        return /*[[@{*/url/*}]]*/;
    },
    fh: function () {
        location.href = tbyoung.returnURL("/logs/list?flag=true");
    }
}