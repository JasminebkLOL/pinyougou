package com.pinyougou.search.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class itemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage)message;
			Long[] goodsIds = (Long[])objectMessage.getObject();
			System.out.println(goodsIds);
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
