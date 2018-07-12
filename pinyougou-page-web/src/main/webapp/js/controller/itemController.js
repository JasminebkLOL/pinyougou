//商品详细页（控制层）
app.controller('itemController',function($scope,$location,$http){
	//数量操作
	$scope.addNum=function(i){
		$scope.num=$scope.num+i;
		//控制数量不能小于1
		if($scope.num<1){
			$scope.num=1;
		}
	}		
	
	$scope.specificationItems={};//记录用户选择的规格
	
	//用户选择规格
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]= value;
		searchSku();//读取sku
	}
	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;//被选中
		}else{
			return false;//没被选中
		}
		
	}
	
	//加载默认sku,,初始化调用该函数
	$scope.loadSku=function(){
		$scope.sku = skuList[0];//定义一个变量,绑定用户选择的规格选项
		//深克隆,将sku默认的选项填充到页面上
		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
	}
	
	//匹配两个对象,判断两个map是否相同
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		return true;
	}
	
	//查询SKU,
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems)){//如果用户选择的和skuList没有一样的..
				$scope.sku=skuList[i];
				return;
			}
			//如果用户选择的规格选项与skuList没有匹配的...这里应该不能让用户选择到 skuList中不存在的规格选项
			//初步的做法是,给用户提示,该商品卖光了
			$scope.sku={id:0,'title':'-----',price:0};
			
		}
		
	}
	
	//通过$location获取solr搜索页传递过来的skuId,匹配页面  skuList,得到应该展示的规格
	$scope.matchSkuWithSkuId=function(){
		//获取搜索页传来的sku
		var skuId= $location.search()['id'];
		//遍历skuList,如果匹配上了,就让页面展示相应的规格
		for(var i=0;i<skuList.length;i++){
			if(skuList[i].id==skuId){
				$scope.sku=skuList[i];
				return;
			}
		}
		//如果匹配不到,则让sku显示脱销了..
		$scope.sku={id:0,'title':'-----',price:0};
	}

	
	//添加商品到购物车
	$scope.addToCart=function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
			+ $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
			 function(response){
				 if(response.success){
					 location.href='http://localhost:9107/cart.html';//跳转到购物车页面
				 }else{
					 alert(response.message);
				 }					 
			 }				
		);
		
	}
	
	
});
