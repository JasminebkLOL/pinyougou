app.service('uploadService' ,function($http){
	this.uploadFile = function(){
		var formData = new FormData();
		//uploadFile必须与uploadController中的 MultipartFile的形参相同
		//file必须与前台上传文件的file标签的id或name相同
		formData.append("uploadFile",file.files[0]);
		return $http({
			method:'POST',
			url:"../upload.do",
			data: formData,
			headers: {'Content-Type':undefined},
			transformRequest: angular.identity
		});
	}
});