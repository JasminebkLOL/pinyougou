app.controller('payController',function($scope,$location,payService){
	//生成微信支付二维码
	$scope.createNative=function(){
		payService.createNative().success(
			function(response){
				
				$scope.outTradeNo=response.out_trade_no;
				$scope.money=(response.total_fee/100).toFixed(2);
				
				var qr = new QRious({
					   element:document.getElementById('qrious'),
					   size:250, 	   
					   level:'H',	   
					   value:response.code_url
				});
				//生成二维码立即调用查询用户支付订单状态方法(循环查询)
				queryPayStatus(response.out_trade_no);
			}
		);
	}
	
	//在生成微信支付二维码方法的回调函数中调用查询用户支付状态方法.
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){//用户支付成功
					alert(response.message);
					alert($scope.money);
					location.href="paysuccess.html#?money="+$scope.money;
				}else{//用户还没支付成功
					if(response.message=='二维码超时'){
						//二维码超时再次调用生成二维码方法
						$scope.createNative();
					}else{
						location.href="payfail.html";
					}
					
				}
			}
		);
	}
	
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
	
	
});