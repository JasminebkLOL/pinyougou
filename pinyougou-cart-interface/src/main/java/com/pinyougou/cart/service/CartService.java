package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {
	
	
	/**
	 * 添加商品到cart
	 * @param list
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> list,Long itemId,Integer num);
	
	/**
	 * 从redis中查询购物车
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	/**
	 * 插入购物车数据到redis中
	 * @param list
	 * @param username
	 */
	public void saveCartListToRedis(List<Cart> list,String username);
	
	/**
	 * 合并redis与cookie中的数据方法
	 * @param list1
	 * @param list2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> list1,List<Cart> list2);

}
