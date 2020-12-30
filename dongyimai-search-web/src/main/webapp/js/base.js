/*创建一个angularJS模块
        * 两个参数：第一个是模块名字；第二个是是否要引入其他模块
        * 引入分页模块*/
var app=angular.module("dongyimai",[]);

/*$sce服务写成过滤器：用于在页面显示HTML结果*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);