package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
public class CartController {
	
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	/**
	 * 刷新购物车方法
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//得到登陆人账号,判断当前是否有人登陆
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//存入cookie
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(cartListString==null||"".equals(cartListString)){
			cartListString="[]";//如果cookie中的数据为空,则给一个空list[],让后面fastJSON转换不会报错
		}
		List<Cart> cartList_cookie = JSONArray.parseArray(cartListString, Cart.class);
		if("anonymousUser".equals(username)){//用户未登录
			
			System.out.println("controller走cookie..");
			return cartList_cookie;
		}else{//用户登录了!!!
			System.out.println("controller走redis..");
			List<Cart> cartList_Redis = cartService.findCartListFromRedis(username);
			if(cartList_cookie.size()>0){//如果本地存在购物车
				//合并
				System.out.println("go cartList_merge()");
				List<Cart> cartList_merge = cartService.mergeCartList(cartList_cookie, cartList_Redis);
				//清除cookie
				util.CookieUtil.deleteCookie(request, response, "cartList");
				//合并后数据存入 redis
				cartService.saveCartListToRedis(cartList_merge, username);
				return cartService.findCartListFromRedis(username);//合并后返回合并后的数据
			}
			
			return cartList_Redis;
		}
		
		
	}
	
	/**
	 * 添加商品到购物车
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num){
		String username = SecurityContextHolder.getContext().getAuthentication().getName(); 
		System.out.println("当前登录用户："+username);
		try {
			List<Cart> cartList = findCartList();
			//添加后的购物车赋给原购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if("anonymousUser".equals(username)){//如果未登录,保存购物车数据到cookie
				String cartListStr = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListStr, 3600*24, "UTF-8");
			}else{
				cartService.saveCartListToRedis(cartList, username);
			}
			
			return new Result(true, "购物车添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "购物车添加失败");
		}
	
		
	}

}
