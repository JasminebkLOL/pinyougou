package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 每一个cart对象都代表一个商家以及商家下属的商品详情
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据skuId(itemId)查询商品明细对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null){
			throw new RuntimeException("商品找不到");
		}
		if(!item.getStatus().equals("1")){
			throw new RuntimeException("商品状态无效");
		}
		//2.根据sku对象得到商家ID
		String sellerId = item.getSellerId();
		//3.根据商家ID在购物车列表中查询: 商家是否存在,分两种情况
		Cart cart = searchCartListBySellerId(cartList,sellerId);
		//判断一: 判断Cart对象中是否以及存在(要插入购物车商品对应的)商家的id  
		//4如果购物车列表中不存在该商家的购物车(即商家id不存在)
		if(cart==null){
			//4.1创建一个 新的商家购物车对象.
			cart =new Cart();//新建一个商家篮子
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem = createOrderItem(num, item);//为OrderItem赋值方法抽取
			List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();//创建商家篮子的详情列表
			orderItemList.add(orderItem);
			//4.2将新的购物车详情对象添加到商家购物车中.
			cart.setOrderItemList(orderItemList);
			cartList.add(cart);//将新增的商家购物车对象加入到 购物篮中
		}else{//5如果购物车列表中已经存在该商家的购物车.
			//判断二:  判断该商品是否在该商家购物车的明细列表中存在
			List<TbOrderItem> orderItemList = cart.getOrderItemList();//商家的商品详情列表
			TbOrderItem orderItem = searchOrderItemByItemId(orderItemList,itemId);
			if(orderItem==null){
				//5.1如果不存在,添加新的购物车明细到购物车列表中
				orderItemList.add(createOrderItem(num, item));
			}else{
				//5.2如果存在,在原有的明细中,数量添加n.且更新金额
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().longValue()*orderItem.getNum()));
				//如果操作后数量小于0移除这个商品
				if(orderItem.getNum()<=0){
					orderItemList.remove(orderItem);
				}	
				//如果商家商品详情等于0
				if(orderItemList.size()==0){
					//移除这个商家
					cartList.remove(cart);
				}
			}
		}
		return cartList;
	}

	/**
	 * 根据商品明细ID查询
	 * @param orderItemList
	 * @param item
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem tbOrderItem : orderItemList) {
			if(tbOrderItem.getItemId().longValue()==itemId.longValue()){
				return tbOrderItem;
			}
		}
		return null;
		
	}

	/**
	 * 根据sellerId查询cartList中是否存在某个商家,存在则返回那个商家的cart对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	public Cart searchCartListBySellerId(List<Cart> cartList,String sellerId){
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)){
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 抽取TbOrderItem赋值的方法
	 * @param num
	 * @param item
	 * @return
	 */
	private TbOrderItem createOrderItem(Integer num, TbItem item) {
		if(num<=0){
			throw new RuntimeException("数量非法");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*num));
		return orderItem;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车数据..."+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null){//若redis中不存在数据,也不能返回null,要new一个数组
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(List<Cart> list, String username) {
		redisTemplate.boundHashOps("cartList").put(username, list);
	}

	/**
	 * 以 list1作为主体,遍历 list2的所有元素,调用addGoodsToCartList(传入list1),实现合并的逻辑
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> list1, List<Cart> list2) {
		for(Cart cart:list2){
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				list1 = addGoodsToCartList(list1,orderItem.getItemId(),orderItem.getNum());
			}
		}
		return list1;
	}

}
