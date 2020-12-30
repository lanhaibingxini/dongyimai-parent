app.service('loginService',function ($http) {
    //向后台发送显示用户名称的方法
    this.showName=function () {
        return $http.get('../login/name.do');
    }
});