package com.offcn.listener;

import com.offcn.util.SmsUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 短信验证码的消息监听类
 */
@Component
public class SmsListener implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        //判断传入的消息的类型是否是MapMessage类型的
        if(message instanceof MapMessage){//如果不是
            //就强转
            MapMessage mapMessage=(MapMessage)message;
            try {
                System.out.println("收到短信发送请求------>>");
                //1.接收手机号码
                String mobile=mapMessage.getString("mobile");
                String param=mapMessage.getString("param");
                //发送验证信息
                HttpResponse response = smsUtil.sendSms(mobile, param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
