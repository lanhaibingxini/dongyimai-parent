//登录服务层
app.service('loginService',function($http){
    //读取登录人名称
    this.showLoginName=function(){
        return $http.get('../login/name.do');
    }
});