package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private RedisTemplate<String,Object> redisTemplate;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加 依据cartList购物车中有几个商家,生成几个订单
	 */
	@Override
	public void add(TbOrder order) {//方法所含参数是这几个订单的共有信息部分(用户的信息)
		//得到购物车数据
		List<Cart> cartList;
		try {
			cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(order.getUserId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("redis挂了,请迅速处理!!!");
		}
		double totalFee = 0.0;//定义需要插入到paylog表中的总金额(单位:分)
		List<String> orderList = new ArrayList<>();//定义需要插入到payLog表中的order集合
		//1.循环购物车,得到每个商家的购物车对象
		if(cartList!=null){//先做非null判断
			for (Cart cart : cartList) {
				TbOrder tbOrder = new TbOrder();//创建商家订单对象
				List<TbOrderItem> orderItemList = cart.getOrderItemList();//获取商家订单明细列表
				long orderId = idWorker.nextId();
				tbOrder.setOrderId(orderId);//snowflake id生成算法
				orderList.add(orderId+"");
				double money = 0.0;//初始化A商家订单总金额
				//2.遍历商家订单明细列表,计算A商家订单总金额
				for (TbOrderItem tbOrderItem : orderItemList) {
					money+=tbOrderItem.getTotalFee().doubleValue();
					tbOrderItem.setOrderId(orderId);//主表主键外键关联,(订单ID)
					tbOrderItem.setId(idWorker.nextId());
					tbOrderItem.setSellerId(cart.getSellerId());
					orderItemMapper.insert(tbOrderItem);
				}
				tbOrder.setSellerId(cart.getSellerId());//设置订单所属商家
				tbOrder.setPayment(new BigDecimal(money));//插入商户订单总金额
				totalFee+=money;//加总  用户支付总金额
				tbOrder.setPaymentType(order.getPaymentType());//设置支付方式
				
				tbOrder.setStatus("1");//未付款
				tbOrder.setCreateTime(new Date());
				tbOrder.setUpdateTime(new Date());
				
				tbOrder.setReceiverAreaName(order.getReceiverAreaName());//设置收货地址
				tbOrder.setReceiver(order.getReceiver());//设置收货人
				tbOrder.setReceiverMobile(order.getReceiverMobile());//设置收货人电话
				tbOrder.setSourceType(order.getSourceType());//设置订单来源类型
				orderMapper.insert(tbOrder);
			}
		}
		
		//3.生成多个商户订单同时,生成下多个商户订单的  用户支付日志对象
		if("1".equals(order.getPaymentType())){//判断,如果用户下单的方式是wechat pay,就生成用户支付订单
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId()+"");//设置用户支付 订单流水号
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId());
			payLog.setPayType("1");//wechatPay:1
			
			payLog.setTradeState("0");//设置订单为,0未支付状态
			payLog.setOrderList(orderList.toString().replace("[", "").replace("]", ""));//一个用户待支付的多个商户订单号
			payLog.setTotalFee((long) (totalFee*100));//总金额(分)
			payLogMapper.insert(payLog);
			
			try {
				//4.以用户登录名为key.将payLog对象存入redis,以方便controller层取出,调wechatPay接口生成支付二维码,
				redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			//5.生成订单后清空redis中的购物车数据
			redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog)redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 更新用户支付订单(payLog)状态
	 * 以及订单(order)支付状态.
	 */
	@Override
	public void updateOrderAndPayLogStatus(String out_trade_no, String transaction_id) {
		//1.更新用户支付订单(payLog)状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//设置已支付.
		payLog.setTransactionId(transaction_id);//设置微信返回的交易流水号
		
		String[] orderList = payLog.getOrderList().split(",");
		if(orderList!=null){
			for (String orderId : orderList) {
				//2.更新订单(order)支付状态.
				TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
				if(order!=null){
					order.setPaymentTime(new Date());
					order.setStatus("2");//已付款
					orderMapper.updateByPrimaryKey(order);
				}
			}
		}
		
		try {
			//清空redis中,用户的支付的相关数据
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("redis挂了");
		}
		
	}

}
