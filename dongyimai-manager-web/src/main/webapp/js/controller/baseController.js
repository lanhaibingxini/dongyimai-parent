app.controller('baseController',function ($scope) {
//重新加载列表 数据
    $scope.reloadList=function(){
        //清除数组中的数据
        $scope.selectIds=[];
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        //切换页码
        //$scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

//分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//当前页，默认第一页
        totalItems: 10,//总条目数
        itemsPerPage: 10,//每页显示条数，默认10条
        perPageOptions: [10, 20, 30, 40, 50],//可选择每页显示多少条
        onChange: function(){//页面改变事件，可理解为翻页
            $scope.reloadList();//重新加载
        }
    };

//确定是否选中
    $scope.selectIds=[];//创建一个存放id的数组
    $scope.updateSelection=function ($event,id) {
        //如果选中了复选框
        if ($event.target.checked){//哪一个复选框被选中就将那个id存入数组
            //就将id添加进数组
            $scope.selectIds.push(id);
        }else{
            //如果复选框被取消选中，name就根据id查询到对应的数组索引
            var index=$scope.selectIds.indexOf(id);
            $scope.splice(index,1);//根据数组索引，从数组中删除这个id，1代表只删除一个
        }
    }

    //由于目前列表中是以json字符串显示的，不美观，因此提取出其中需要显示的字段显示到页面即可
    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                //每个需要显示字段中间用","隔开
                value+=","
            }
            //根据key获取对应的value，不断追加
            value+=json[i][key];
        }
        return value;
    }
});
