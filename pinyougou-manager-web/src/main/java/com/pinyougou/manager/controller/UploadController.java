package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

/**
 * 文件上传Controller
 * 
 * @author Administrator
 *
 */

@RestController
public class UploadController {

	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;

	@RequestMapping("/upload")
	public Result upload(MultipartFile uploadFile) {
		String oriiginalFilename = uploadFile.getOriginalFilename();
		// 获取文件拓展名
		String extName = oriiginalFilename.substring(oriiginalFilename.lastIndexOf(".") + 1);
		try {
			// 创建一个FastDFS客户端
			FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
			// 执行上传处理
			String path = fastDFSClient.uploadFile(uploadFile.getBytes(), extName);
			// 拼接返回的url和ip地址,
			String url = FILE_SERVER_URL + path;
			return new Result(true, url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败");
		}
	}
	
	
}
