package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付接口
 * @author Administrator
 *
 */
public interface WeixinPayService {
	
	/**
	 * 生成微信支付二维码
	 * @param out_trade_no 订单号
	 * @param total_fee 金额(分)
	 * @return
	 */
	public Map createNative(String out_trade_no,String total_fee);
	
	/**
	 * 调用微信支付api,查询用户支付状态(
	 * 	trade_state值:
	 *  SUCCESS—支付成功
	 *  
		REFUND—转入退款
		
		NOTPAY—未支付
		
		CLOSED—已关闭
		
		REVOKED—已撤销（刷卡支付）
		
		USERPAYING--用户支付中
		
		PAYERROR--支付失败(其他原因，如银行返回失败))
		
	 * @param out_trade_no
	 * @return
	 */
	public Map queryPayStatus(String out_trade_no);

}
