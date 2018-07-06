app.controller("contentController",function($scope,contentService){	
	$scope.contentList=[];//广告集合	
	
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}	
	
	//首页传递参数到搜索页方法
	$scope.search=function(){
		location.href="http://localhost:9104#?keywords="+$scope.keywords;
	}
	
});
