$(function(){
    loadPage('prepare/textInput');
});

var dynamicCss;


var words = {};



function loadPage(url, onLoadFunction) {
    if (typeof onLoadFunction === 'undefined') {
        var functionName = url.replace(/^.*\//, '');
        functionName = "start" + functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "()";
//            console.log("Execute function: " + functionName);
        onLoadFunction = eval(functionName);
    }
    console.log("Load page: " + url);
//    console.log("Load script: " + url + ".js");
    $.getScript(url + ".js", function() {
//        console.log("Load page: " + url + ".html");
        $('#RootContainer').load(url + ".html #PageContainer", function() {
//            console.log("Load css: " + url + ".css");
            if (dynamicCss == null) {
                $('head').append('<link rel="stylesheet" type="text/css" />');
                dynamicCss = $("head").children(":last");
            }
            dynamicCss.attr('href', url + '.css');
            onLoadFunction.call(this);
        });
    });
}


function hijackLinks() {
    $('#container a').click(function(e){
        e.preventDefault();
        loadPage(e.target.href);
    });
}


function dumpObject(s, o) {
    console.log("Dump object " + s + "[" + o + "]");
//    console.log("Object: " + o);
    for (var name in o) {
        var value = o[name];
        console.log("    " + name +" = " + value);
    }
}
