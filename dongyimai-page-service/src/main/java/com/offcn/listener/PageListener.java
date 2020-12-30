package com.offcn.listener;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 发布订阅消息，用于生成静态页面
 */
@Component
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("接收到消息："+text);
            long goodsId = Long.parseLong(text);
            itemPageService.genItemHtml(goodsId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
