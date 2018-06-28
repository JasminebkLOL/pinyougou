package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	/**
	 * 品牌接口
	 * @return
	 */
	public List<TbBrand> findAll();
	
	/**
	 * 返回分页列表
	 */
	public PageResult findPage(int pageNum,int pageSize);

	public PageResult findPage(int pageNum,int pageSize,TbBrand brand);
	
	/**
	 * 添加商品
	 */
	public void add(TbBrand brand);
	
	/**
	 * 修改商品
	 */
	public void update(TbBrand brand);

	/**
	 * 根据id获取商品
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 根据id批量删除商品
	 */
	public void delete(Long [] ids);
	
	/**
	 * 品牌多选下拉框,数据访问接口
	 * @return
	 */
	List<Map> selectOptionList();

}
