app.controller('baseController',function($scope){
	
	//重新加载列表 数据
	$scope.reloadList = function(){
		$scope.search($scope.paginationConf.currentPage,
					$scope.paginationConf.itemsPerPage);
	}
	
	//分页控件配置 
	$scope.paginationConf = {
			 currentPage: 1,//当前页码
			 totalItems: 10,//总记录数
			 itemsPerPage: 10,//每页记录数
			 perPageOptions: [10, 20, 30, 40, 50],//记录数选项
			 onChange: function(){
			        $scope.reloadList();//重新加载
			 }
	}; 
	
	//声明复选框选中
	$scope.selectIds = [];
	//更新复选
	$scope.updateSelection = function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id);
		} else{
			var index = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(index,1);//splice删除方法,删除指定位置的,指定数量的方法.
		}
	}
	
	//提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
	$scope.jsonToString = function(jsonString,key){
		var json = JSON.parse(jsonString);
		var value = "";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value += ",";
			}
			value += json[i][key];
		}
		return value;
	}
	
	
	
	
	
	
});