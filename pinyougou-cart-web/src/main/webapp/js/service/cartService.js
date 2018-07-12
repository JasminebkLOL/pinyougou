app.service('cartService',function($http){
	
	this.findCartList=function(){
		return $http.get("../cart/findCartList.do");
	}
	
	this.addGoodsToCartList=function(itemId,num){
		return $http.get("../cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
	}
	
	this.sum=function(cartList){
		var total = {totalNum:0,totalMoney:0.00};
		for(var i=0;i<cartList.length;i++){
			var orderItemList = cartList[i].orderItemList;
			for(var j=0;j<orderItemList.length;j++){
				total.totalNum+=orderItemList[j].num;
				total.totalMoney+=orderItemList[j].totalFee;
			}
		}
		return total;
	}
	
	//获取地址列表
	this.findAddressList=function(){
		return $http.get('address/findListByLoginUser.do');	
	}
	
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);
	}

	
});