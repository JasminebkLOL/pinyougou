app.controller("userController",function($scope,$controller  ,userService){
	
	//注册
	$scope.reg=function(){
		if($scope.entity.password!=$scope.rePassword){
			alert('两次输入的密码不一致,请重新输入!!');
			$scope.entity.password='';
			$scope.rePassword='';
			return;
		}
		userService.add($scope.entity,$scope.code).success(
			function(response){
				alert(response.message);
			}
		);
		
	}
	
	//发送验证码..send Phone
	$scope.sendCode=function(){
		if($scope.entity.phone==null){
			alert("请输入手机号！");
			return ;
		}

		userService.sendCode($scope.entity.phone).success(
			function(response){
				if(response.success){
					alert(response.message);
				}else{
					alert(response.message);
				}
			}
		);
	}
	
	

	
});