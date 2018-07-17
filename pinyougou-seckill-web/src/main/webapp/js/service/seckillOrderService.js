//服务层
app.service('seckillOrderService',function($http){ 
	
	//提交订单
	this.submitOrder=function(seckillId){
		return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
	}

});
