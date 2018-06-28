package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

//ResponseBody and Controller的结合体
@RestController
@RequestMapping("/brand")
public class BrandController {
	
	@Reference
	private BrandService brandService;
	
	/**
	 * 查询所有
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	} 
	
	/**
	 * 普通查询方法,不带检索
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int rows){
		return brandService.findPage(page,rows);
	} 
	/**
	 * 替代了上面的findPage方法,这是带检索的查询方法
	 * @param page
	 * @param rows
	 * @param brand
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult findPage(int page,int rows,@RequestBody TbBrand brand){
		return brandService.findPage(page,rows,brand);
	} 
	
	/**
	 * 添加商品
	 * @param brand
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			return new Result(true,"Successfully add!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"Errors had occurred,add failed!");
		}
	} 
	
	/**
	 * 更新商品
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			return new Result(true,"Successfully updated!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"Errors had occurred,updated failed!");
		}
	} 
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	} 
	
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true,"Successfully delete!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"Errors had occurred,delete failed!");
		}
	} 
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	} 


}
