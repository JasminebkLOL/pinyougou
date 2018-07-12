app.controller("indexController",function($scope,$controller  ,loginService){
	
	loginService.showName().success(
		function(response){
			$scope.loginName = response.loginName;
		}
	);

});