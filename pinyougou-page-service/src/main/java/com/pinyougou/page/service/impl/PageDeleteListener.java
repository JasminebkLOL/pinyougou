package com.pinyougou.page.service.impl;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class PageDeleteListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;
	
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage)message;
			Long[] ids = (Long[])objectMessage.getObject();
			for (Long goodsId : ids) {
				itemPageService.deleteItemHtml(goodsId);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
