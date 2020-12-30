package com.offcn.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * 监听，用于审核通过后添加进索引库中
 */
@Component
public class itemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息");
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item : itemList) {
                System.out.println(item.getId()+" "+item.getTitle());
                //将item表中的specification字段中的json字符串转换为Map集合
                Map specMap = JSON.parseObject(item.getSpec());
                //转换后设置进item表中
                item.setSpecMap(specMap);
            }
            //调用导入索引库的方法
            itemSearchService.importList(itemList);
            System.out.println("成功导入到索引库");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
