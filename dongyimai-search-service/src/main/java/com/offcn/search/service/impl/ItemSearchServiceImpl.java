package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //初始化
        Map<String, Object> map = new HashMap<>();
//        Query query = new SimpleQuery();
//
//        // is：基于分词后的结果 和 传入的参数匹配
//        //创建查询条件，根据keywords关键字搜索，然后与item_keywords复制域中的数据相匹配
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        // 添加查询条件
//        query.addCriteria(criteria);
//        //开始查询
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        //将查询到的数据封装到map集合中
//        map.put("rows", page.getContent());

        //关键字空格处理
        if(searchMap.get("keywords")!=null){
            //将搜索关键字转换成字符串，再以空格为索引，查找有几个空格
            int index= searchMap.get("keywords").toString().indexOf(" ");
            if(index>=0){//如果空格数大于等于0
                //就将空格替换成空串
                String keywords = searchMap.get("keywords").toString().replaceAll(" ","");
                //再将替换后的搜索关键字放入Map集合
                searchMap.put("keywords",keywords);
            }

        }

        //1.将调用高亮方法查询后的结果放入查询Map中
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3、根据商品类目查询对应的品牌、规格
        //读取分类名称
        String categoryName = (String)searchMap.get("category");
        if(!"".equals(categoryName)) {
            //按照分类名称重新读取对应品牌、规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if (categoryList.size() > 0) {
                Map mapBrandAndSpec = searchBrandAndSpecList((String) categoryList.get(0));
                map.putAll(mapBrandAndSpec);
            }
        }

        return map;
    }

    //根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap){
        Map map=new HashMap();

        //1、创建一个支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、设定需要高亮处理字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3、设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4、设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5、关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);

        //6、设定查询条件 根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //添加查询条件到查询器对象
        query.addCriteria(criteria);

        //1.2按商品分类查找
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按品牌查找
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4按规格查找
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                //需要使用动态域
                Criteria filterCriteria=new Criteria("item_spec_"+ Pinyin.toPinyin(key, "").toLowerCase()).is( specMap.get(key) );
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5按价格筛选
        if(!"".equals(searchMap.get("price"))){
            //获取价格区间数组，如：price=[0,500]
            String[] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){//如果区间起点不等于0
                //根据比0更大的数进行查询
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if(!price[1].equals("*")){//如果区间终点不等于*
                Criteria filterCriteria=new  Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6 分页查询
        Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize==null){
            pageSize=20;//默认20
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);

        //1.7排序
        String sortValue= (String) searchMap.get("sort");//ASC  DESC
        String sortField= (String) searchMap.get("sortField");//排序字段
        if(sortValue!=null && !sortValue.equals("")){
            //如果是升序
            if(sortValue.equals("ASC")){
                //动态域
                Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
                query.addSort(sort);
            }
        }

        //7、发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8、获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9、遍历高亮集合
        for(HighlightEntry<TbItem> highlightEntry:highlightEntryList){
            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            //如果高亮集合不为空
            if(highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }

        //把带高亮数据集合存放map
        map.put("rows",page.getContent());

        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数

        return map;
    }

    /**
     * 查询商品分类列表
     * @param searchMap
     * @return
     */
    private  List searchCategoryList(Map searchMap){
        //初始化
        List<String> list=new ArrayList();
        Query query=new SimpleQuery();
        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        //添加查询条件
        query.addCriteria(criteria);
        //设置分组选项，通过商品分类字段进行分组
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        //将商品字段分组后设置进分组选项
        query.setGroupOptions(groupOptions);
        //发出查询请求，得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据商品分类字段得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        //初始化
        Map map=new HashMap();
        //从缓存中获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        //如果id不为空
        if(typeId!=null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    /**
     * 导入item数据库数据到索引库
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        System.out.println("=====开始导入索引库=====共有" + list.size() + "条记录");
        for (TbItem item : list) {
            System.out.println(item.getId() + ":" + item.getTitle());
            //将item表中的spec转换成map集合
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            Map map = new HashMap();
            for (String key : specMap.keySet()) {
                map.put("item_spec_" + Pinyin.toPinyin(key,"").toLowerCase(), specMap.get(key));
            }
            item.setSpecMap(map);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
        System.out.println("=====导入索引库结束=====");
    }

    /**
     * 根据商品id数组删除索引库中的数据
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品的ID"+goodsIdList);
        Query query=new SimpleQuery();
        //先通过数组查找有哪些数据
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        //再进行删除
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
