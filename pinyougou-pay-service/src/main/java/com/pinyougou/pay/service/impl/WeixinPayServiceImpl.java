package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;

	@Value("${partner}")
	private String partner;

	@Value("${partnerkey}")
	private String partnerkey;

	/**
	 * 调用微信支付接口,生成用户订单支付信息二维码
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		// 1.创建参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);// 公众账户ID
		param.put("mch_id", partner);// 商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 生成随机字符串

		param.put("body", "pinyougou");
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("total_fee", total_fee);// 总金额

		param.put("spbill_create_ip", "127.0.0.1");
		param.put("notify_url", "http://test.itcast.cn");// 回调地址..
		param.put("trade_type", "NATIVE");// JSAPI(公众号支付) NATIVE(扫码支付) APP
											// (APP支付)
		// MAP转换为XML字符串（自动添加签名）
		try {
			// 2.生成要发送的xml
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

			// System.out.println("发送的xml:"+xmlParam);
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();// 发送.
			// 3.获得结果
			String resultMap = httpClient.getContent();

			// System.out.println("resultMap"+resultMap);//返回的String形式的xml.
			Map<String, String> map = WXPayUtil.xmlToMap(resultMap);// 返回的map
			// 因为wechat返回的map含有许多敏感信息,不适合返回 前台
			Map<String, String> returnMap = new HashMap<String, String>();// 返回前台map

			returnMap.put("code_url", map.get("code_url"));// 支付地址
			returnMap.put("out_trade_no", out_trade_no);// 总金额
			returnMap.put("total_fee", total_fee);// 订单号
			return returnMap;
		} catch (Exception e) {
			// 出现问题返回一个 空map
			e.printStackTrace();
			return new HashMap<>();
		}

	}

	/**
	 * 调用微信接口,查询订单支付状态
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		// 1.封装查询参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			// 2.生成要发送的xml
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setXmlParam(xmlParam);
			client.setHttps(true);
			client.post();
			// 3.获得结果
			String result = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
			// System.out.println(resultMap);//微信端返回来的map形式的回调数据
			// ,含transaction_id等信息
			return resultMap;

		} catch (Exception e) {
			// 若出现问题返回null
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Map<String,String> closePay(String out_trade_no) {
		Map<String,String> param = new HashMap<String,String>();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		String url = "https://api.mch.weixin.qq.com/pay/closeorder";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
