	//品牌控制层
	app.controller('brandController',function($scope,$controller,brandService){
		//伪继承,{$scope:$scope}的 写法  可以实现类似继承的效果
		$controller('baseController',{$scope:$scope});//继承
		//查询所有商品数据
		$scope.findAll=function(){
			brandService.findAll().success(
				function(response){
					$scope.list=response;
				}			
			);
		}
		
		//分页查询方法,和reloadList关联,通过其调用findPage获得参数
		$scope.findPage = function(page,rows){
			brandService.findPage(page,rows).success(
				function(response){
					$scope.list = response.rows;//显示当前数据
					$scope.paginationConf.totalItems = response.total;//更新总记录数
				}
			);
		}
		
		//定义一个搜索体的绑定变量
		$scope.searchEntity={};
		//search方法,代替findPage方法
		$scope.search = function(page,rows){
			brandService.search(page,rows,$scope.searchEntity).success(
				function(response){
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;
				}
			);
		}
		
		//商品保存以及更新方法
		$scope.save = function(){
			if($scope.entity.id!=null){
				//更新方法
				brandService.update($scope.entity).success(function(response){
					if(response.success){
						//重新查询
						$scope.reloadList();
					} else {
						alert(response.message);
					}
				});
			}else{
				//增加方法
				brandService.add($scope.entity).success(function(response){
					if(response.success){
						//重新查询
						$scope.reloadList();
					} else {
						alert(response.message);
					}
				});
			}
		}
		
		//根据id获取实体
		$scope.findOne = function(id){
			brandService.findOne(id).success(function(response){
				$scope.entity = response;
			});
		}
		
		//批量删除
		$scope.dele = function(){
			if($scope.selectIds.length == 0){
				alert('请至少选择一个进行删除!');
				return;
			}
			//根据选中的id进行删除操作
			brandService.dele($scope.selectIds).success(function(response){
				if(response.success){
					$scope.reloadList();
					//删除成功,清空删除id数组
					$scope.selectIds = [];
				} else{
					alert(response.message);
				}
			});
		}
		
	});