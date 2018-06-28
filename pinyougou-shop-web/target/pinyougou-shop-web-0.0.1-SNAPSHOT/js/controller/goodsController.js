 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location   ,goodsService,uploadService,
		itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
	//通过数组角标定位数据,0代表未审核,1代表已审核,2代表审核未通过,3代表关闭
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	//定义一个数组接收所有的分类信息[{id:"",name:""}]
	$scope.itemCatList=[];//商品分类列表
	
	//加载商品分类列表
	$scope.findItemCatList= function(){
		itemCatService.findAll().success(function(response){
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id] = response[i].name
			}
		});
	}

	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		
		var id = $location.search()['id'];//获取参数值
		if(id==null){
			return;
		}
		
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html(response.goodsDesc.introduction);
				//显示图片
				$scope.entity.goodsDesc.itemImages= 
					JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示拓展信息
				$scope.entity.goodsDesc.customAttributeItems= 
					JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格				
				$scope.entity.goodsDesc.specificationItems=
					JSON.parse($scope.entity.goodsDesc.specificationItems);	
				//读取sku列表
				//SKU列表规格列转换
				var list = $scope.entity.itemList;
				for(var i=0;i<list.length;i++){
					list[i].spec = JSON.parse(list[i].spec);
				}
			}
		);				
	}
	
	//填充修改页面规格选项 的勾选状态
	$scope.checkAttributeValue=function(specName,optionName){
		var item = $scope.entity.goodsDesc.specificationItems;
		//[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},
		//{"attributeName":"机身内存","attributeValue":["16G","32G"]}]
		var obj = $scope.searchObjectByKey(item,'attributeName',specName);
		if(obj==null){
			return false;
		} else{
			if(obj.attributeValue.indexOf(optionName)>=0){
				return true;
			} else{
				return false;
			}
		}
		
	}
	
	//保存 :包括更新以及新增
	$scope.save=function(){	
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id==null){
			serviceObject = goodsService.add( $scope.entity  );
		} else{
			serviceObject = goodsService.update( $scope.entity  );
		}
		serviceObject.success(function(response){
			if(response.success){
				alert('保存成功');
				$scope.entity={};//清空除了富文本编辑以外的所有input标签
				editor.html('');//清空富文本编辑器
			}else{
				alert(response.message);
			}
		});
						
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//声明装载图片url,color的实体list,声明实体的格式注意事项
	//注意,这样声明是有问题的,会报undefined错误$scope.entity.goodsDesc.itemImages = {goods:{},goodsDesc:{itemImages:[]}};
	$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	
	//上传图片
	$scope.uploadFile = function(){
		uploadService.uploadFile().success(function(response){
			if(response.success){
				$scope.image_entity.url = response.message;
			} else{
				alert(response.message);
			}
		}).error(function(){
			alert("文件上传发送错误!!");
		});
	}
	
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	
	//移除图片列表
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	//读取一级分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(function(response){
			$scope.itemCat1List=response; 
		});
	}
	
	//二级分类监听一级分类
	$scope.$watch('entity.goods.category1Id',function(newValue, oldValue){
		if(newValue!=oldValue){
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat2List=response; 
				}
			)
		}
	});
	
	//三级分类监听二级分类
	$scope.$watch('entity.goods.category2Id',function(newValue, oldValue){
		if(newValue!=oldValue){
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat3List=response; 
				}
			)
		}
	});
	
	//模板id监听三级分类
	$scope.$watch('entity.goods.category3Id',function(newValue, oldValue){
		if(newValue!=oldValue){
			itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId; 
				}
			)
		}
	});
	
	//品牌下拉监听模板id,拓展属性,用户更新模板id时,更新
	$scope.$watch('entity.goods.typeTemplateId',function(newValue, oldValue){
		if(newValue!=oldValue){
			//查询品牌下拉列表
			typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplate=response;//获取类型模板实体
					//品牌列表
	       			$scope.typeTemplate.brandIds= JSON.parse($scope.typeTemplate.brandIds);
	       			//如果没有ID，则加载模板中的扩展数据
	       			if($location.search()['id']==null){
	       				$scope.entity.goodsDesc.customAttributeItems=
	       					JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
	       			}
				}
			);
			//查询规格列表,返回一个speclist={ ... options:[规格选项]}
			typeTemplateService.findSpecList(newValue).success(
				function(response){
					$scope.specList = response;
				}
			);
		}
	});
	
	//添加选项,该方法用于获取用户勾选规格选项的状态,并与对象进行绑定,封装.
	$scope.updateSpecAttribute=function($event,name,value){//name规格名称,value:用户勾选的规格选项
		//方法封装在baseController中,用于判断集合中某个属性的的某个值是否存在,若存在返回该list中存在那个值的对象.若无则返回null
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		//若集合中已经存在
		if(object!=null){
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{
				//{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				//如果选项都移除了,清空该选项
				if(object.attributeValue.length==0){
					//清空该选项
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{//若集合中不存在该属性,以及值
			//新增一个结构
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	//创建sku列表
	$scope.createItemList=function(){	
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
		var items = $scope.entity.goodsDesc.specificationItems;
		//[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}
		//     ,{"attributeName":"机身内存","attributeValue":["32G","16G"]}]
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
	
	//页面不需要调用的方法,不需要加$scope,相当于private的意思.
	addColumn=function(list,columnName,columnValues){
		//[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ]
		var newList=[];
		for(var i=0;i<list.length;i++){
			var oldRow = list[i];
			for(var j=0;j<columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	
	
	
});	
