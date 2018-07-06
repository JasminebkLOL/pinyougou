package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {

		try {
			TextMessage textmessage = (TextMessage)message;
			List<TbItem> list = JSON.parseArray(textmessage.getText(),TbItem.class);
			//System.out.println("true"+textmessage.getText());
			itemSearchService.importList(list);
			//System.out.println("successfully imported");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
