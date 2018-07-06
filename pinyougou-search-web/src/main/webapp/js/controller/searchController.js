app.controller("searchController",function($scope,$location,searchService){
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':50,'sortField':'','sort':'' };
	
	//重置查询参数
	$scope.reset=function(){
		$scope.searchMap.brand='';
		$scope.searchMap.category='';
		$scope.searchMap.spec={};
		$scope.searchMap.pageNo=1;
	}
	
	$scope.search=function(){
		//后台需要的是int类型,而html页面绑定了searchMap.pageNo变量,因而需要调用js的parseInt方法转一下.
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);
		
		searchService.search($scope.searchMap).success(
				function(response){
					//因为后台的定义:返回的是{rows:[]},所以前台要绑定变量
					$scope.resultMap = response;
					
					buildPageLabel();
				}
			);
	}
	//buildPageLabel构建分页标签页 $scope.searchMap.pageNo,
	buildPageLabel=function(){
		$scope.pageLabel=[];//新增分页栏属性
		var lastPage = $scope.resultMap.totalPages;//定义返回的最后一页
		var firstPage = 1; //初始化firstPage为1
		var nowPage =  $scope.searchMap.pageNo;//定义当前页的遍历
		
		$scope.beginDot = true;
		$scope.endDot = true;
		/**
		 * 数学公式,末项-首项+1=项数
		 */
		if(lastPage>5){//界面上显示5页,如果返回的总页数大于5...
			if(nowPage<=3){//如果当前页在前四页,则最后一页必定是5
				lastPage = 5;
				$scope.beginDot = false;//若在前4页的话,前面的省略号隐藏
			}else if(nowPage>=lastPage-3+1){//若当前页在最后一页,取中位数3处理,由数学公式,有:
				firstPage = lastPage-5+1;//同理由数学公式:假设最后一页是100,则firstPage应是96
				$scope.endDot = false;//若在最后几页,后面省略号隐藏
			}else{//中间则省略号走默认值,显示
				firstPage = nowPage-2;
				lastPage = nowPage+2;
			}
		} else{//如果返回的总页数小于7,两个点都不显示
			$scope.beginDot = false;
			$scope.endDot = false;
		}
		//遍历,插入页码
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}		
		$scope.searchMap.pageNo=pageNo;			
		$scope.search();
	}

	
	//添加方法
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]=value;
		} else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除方法
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]="";
		} else{
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//执行搜索
	}
	
	//设置排序规则sortField排序字段,sort排序顺序ASC,DESC
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		
		if(sort=="1"){
			sort='0';
		} else{
			sort='1';
		}
		if($scope.searchMap.sortField=='updatetime'){
			sort='0';
		}
		$scope.searchMap.sort=sort;	
		$scope.search();
	}
	
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<resultMap.brandList;i++){
			if($scope.searchMap.keywords.indexOf(resultMap.brandList[i].text)>=0){
				//若搜索关键字   包含品牌列表中的某一个品牌,则隐藏品牌这个选项
				return true;
			}
		}
		return false;
	}
	
	
	//加载首页传递的参数并执行查询
	$scope.loadkeywords=function(){
		
		$scope.searchMap.keywords= $location.search()['keywords'];
		if($scope.searchMap.keywords==null){
			$scope.searchMap.keywords='手机';
		}
		$scope.search();
	}

	
});