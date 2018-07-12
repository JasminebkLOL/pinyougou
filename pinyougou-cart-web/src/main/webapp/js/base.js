var app=angular.module('pinyougou',[]);

/*  $sce服务写成过滤器    */ 
//'$sce'指的是加载$sce这个模块
//filter定义过滤器,相当于一个全局方法
app.filter('trustHTML',['$sce',function($sce){
	return function(data){//传入参数时被过滤的内容
		return $sce.trustAsHtml(data);//返回的是过滤后的内容,(信任html的转换)
	}
}]);