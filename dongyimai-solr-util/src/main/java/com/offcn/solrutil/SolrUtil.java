package com.offcn.solrutil;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * solr工具类，用于导入数据
 */
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据
     */
    public void importItemData(){
        //首先要获取需要导入的数据
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        //只需要获取审核通过的商品
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);
        System.out.println("===============商品列表===============总记录条数：" + itemList.size());
        for (TbItem item : itemList) {
            System.out.println(item.getId() + ":" + item.getTitle());
            //将规格数据封装成Map对象，再转换成JSON字符串
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            //创建一个Map，存储拼音
            Map<String, String> pinyinMap = new HashMap<>();
            //遍历规格Map中所有的key，也就是规格名称，获取到每一个规格名称
            for (String key : specMap.keySet()) {
                //将每一个规格名称转换成拼音，中间用空串隔开，并转换成小写字母
                String pinyinKey = Pinyin.toPinyin(key, "").toLowerCase();
                //将拼音化的规格名称和规格选项，装入拼音Map
                pinyinMap.put(pinyinKey, specMap.get(key));
            }
            //循环结束后，将拼音Map放入规格Map中
            item.setSpecMap(pinyinMap);
        }

        //将数据导入到索引库当中
        solrTemplate.saveBeans(itemList);
        //事务提交
        solrTemplate.commit();

        System.out.println("===============结束===============");
    }

    /**
     * 主方法测试
     */
    public static void main(String[] args) {
        //加载配置文件
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        //获取要调用类的对象
        SolrUtil solrUtil = (SolrUtil)applicationContext.getBean("solrUtil");
        //调用方法
        solrUtil.importItemData();

//        //删除方法，删除全部数据
//        SimpleQuery query = new SimpleQuery("*:*");
//        solrUtil.solrTemplate.delete(query);
//        solrUtil.solrTemplate.commit();
    }
}
