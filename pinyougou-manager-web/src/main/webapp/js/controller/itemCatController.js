 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			$scope.entity.parentId=$scope.parentId;//赋予上级ID
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
		        	$scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					alert(response.message+"666");
					if(response.message!=null){
						alert("因为有子分类,id为:"+ response.message+"的分类删除失败,其余分类删除成功.");
					}
					$scope.findByParentId(0);//刷新列表
				} else{
					alert(response.message);
				}					
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.parentId=0;//上级ID
	
	//获取下级目录
	$scope.findByParentId=function(parentId){	
		$scope.parentId=parentId;//记住上级ID
		itemCatService.findByParentId(parentId).success(
			function(response){
				$scope.list=response;	
			}			
		);
	}
	//定义一个变量确定目录级别,默认为1
	$scope.grade = 1;
	//定义一个方法,获取当前页page;
	$scope.setGrade=function(value){
		$scope.grade = value;
	}
	
	//定义一个selectList方法封装查询下级目录详情findByParentId
    $scope.selectList = function(p_entity){
    	if($scope.grade==1){
    		$scope.entity_1=null;
    		$scope.entity_2=null;
    	}
    	if($scope.grade==2){
    		$scope.entity_1=p_entity;
    		$scope.entity_2=null;
    	}
    	if($scope.grade==3){
    		$scope.entity_2=p_entity;
    	}
    	//根据上级目录的id来查询下级的所有子目录
    	$scope.findByParentId(p_entity.id);
    }
});	
