app.controller('cartController',function($scope, cartService){
	
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				//response=[{sellerId:'',sellerName:'',orderItemList:[]}]
				$scope.cartList=response;
				$scope.total= cartService.sum($scope.cartList);
			}
		);
	}
	
	//添加删除购物车元素方法,通过后台可以实现对购物车的详情栏的删除,修改 数量.新增详情
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){
					$scope.findCartList();//刷新购物车
				}else{
					alert(response.message);
				}
			}
		);
	}
	
	//获取地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				$scope.address = $scope.addressList[0];//后台做了排序
			}		
		);		
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address = address;//用一个address变量存储当前选择的地址
	}
	
	//判断是否当前选择地址
	$scope.isSelectedAddress=function(address){
		if($scope.address==address){
			return true;
		}else{
			return false;
		}
	}
	
	//默认支付方式为微信支付1
	$scope.order={paymentType:'1'};	
	
	//选择支付方式
	$scope.selectPayType=function(type){
		$scope.order.paymentType= type;
	}
	
	
	$scope.submitOrder=function(){
		//添加用户基本信息
		$scope.order.receiverAreaName=$scope.address.address;
		$scope.order.receiverMobile=$scope.address.mobile;
		$scope.order.receiver = $scope.address.contact;
		
		cartService.submitOrder($scope.order).success(
			function(response){
				if(response.success){
					//提交订单成功跳转支付页面
					if($scope.order.paymentType=="1"){//微信支付
						location.href="pay.html";
					}else{//如果货到付款，跳转到提示页面
						location.href="paysuccess.html";
					}
				}else{
					alert(response.message);//也可以跳转到提示页面
				}
			}
		);
	}


	
	
});