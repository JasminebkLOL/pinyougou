 //控制层 
app.controller('seckillOrderController' ,function($scope,$controller   ,seckillOrderService){	
	
	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
			function(response){
				if(response.success){
					alert("下单成功，请在5分钟内完成支付");
					location.href="pay.html";
				}else{
					alert(response.message);
				}
			}
		);		
	}

    
});	
