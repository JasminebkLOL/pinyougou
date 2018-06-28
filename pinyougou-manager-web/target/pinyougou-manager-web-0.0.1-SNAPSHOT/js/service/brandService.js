	//品牌服务层
	app.service('brandService',function($http){
		this.findAll=function(){
			return $http.get('../brand/findAll.do');
		}
		this.findPage=function(page,rows){
			return $http.get('../brand/findPage.do?page='+page+'&rows='+rows);
		}
		this.search=function(page,rows,searchEntity){
			return $http.post('../brand/search.do?page='+page+'&rows='+rows,searchEntity);
		}
		this.add=function(entity){
			return $http.post("../brand/add.do",entity);
		}
		this.update=function(entity){
			return $http.post("../brand/update.do",entity);
		}
		this.findOne=function(id){
			return $http.get("../brand/findOne.do?id="+id);
		}
		this.dele=function(selectIds){
			return $http.get('../brand/delete.do?ids='+selectIds);
		}
		this.selectOptionList=function(){
			return $http.get('../brand/selectOptionList.do');
		}
		
	});