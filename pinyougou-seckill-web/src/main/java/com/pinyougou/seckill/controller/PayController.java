package com.pinyougou.seckill.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	@Reference
	private SeckillOrderService seckillOrderService;

	@RequestMapping("/createNative")
	public Map createNative(String out_trade_no, String total_fee) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
		if (null != seckillOrder) {// 判断支付日志存在
			// 依据,用户支付订单流水号,以及支付金额,生成支付二维码
			long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);
			Map resultMap = weixinPayService.createNative(seckillOrder.getId() + "", fen + "");
			return resultMap;
		} else {
			return new HashMap<>();
		}

	}

	/**
	 * 查询用户支付状态方法.每隔3秒查询一次
	 * 
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int flag = 0;
		while (true) {
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {// 后台sevice出错了.
				result = new Result(false, "支付出错");
				break;
			}
			if ("SUCCESS".equals(map.get("trade_state"))) {
				result = new Result(true, "支付成功");
				// 支付成功更新秒杀订单信息
				TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
				seckillOrderService.saveOrderFromRedisToDb(username, Long.parseLong(out_trade_no),
						map.get("transaction_id"));
				break;
			}

			if (flag > 100) {// falg>100为5分钟
				result = new Result(false, "二维码超时");
				Map<String, String> payresult = weixinPayService.closePay(out_trade_no);
				
				if (!"SUCCESS".equals(payresult.get("result_code"))) {// 如果返回结果是正常关闭
					if ("ORDERPAID".equals(payresult.get("err_code"))) {
						result = new Result(true, "支付成功");
						seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no),
								map.get("transaction_id"));
					}
				}
				if (result.isSuccess() == false) {
					System.out.println("超时，取消订单");
					// 2.调用删除
					seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
				}

				break;
			}
			flag++;
			try {
				Thread.sleep(3000);// 间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}
