package com.pinyougou.cart.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative(String out_trade_no, String total_fee){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbPayLog payLog = orderService.searchPayLogFromRedis(username);
		if(null!=payLog){//判断支付日志存在
			//依据,用户支付订单流水号,以及支付金额,生成支付二维码
//			payLog.getTotalFee()+"" 单位:分,写死推荐:  "1"
			Map resultMap = weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
			return resultMap;
		}else{
			return new HashMap<>();
		}
		
		
		
	}
	
	/**
	 * 查询用户支付状态方法.每隔3秒查询一次
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int flag = 0;
		while(true){
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if(map==null){//后台sevice出错了.
				result=new Result(false, "支付出错");
				break;
			}
			if("SUCCESS".equals(map.get("trade_state"))){
				result=new Result(true, "支付成功");
				//支付成功更新paylog以及订单状态
				TbPayLog payLog = orderService.searchPayLogFromRedis(username);
				payLog.setPayTime(new Date());
				payLog.setTransactionId(map.get("transaction_id"));
				payLog.setTradeState("1");
				orderService.updateOrderAndPayLogStatus(out_trade_no,map.get("transaction_id"));
				break;
			}
			
			if(flag>100){//falg>100为5分钟
				result=new Result(false, "二维码超时");
				break;
			}
			flag++;
			try {
				Thread.sleep(3000);//间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
}
