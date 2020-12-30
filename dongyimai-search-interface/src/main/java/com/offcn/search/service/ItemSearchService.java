package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 将数据库数据导入到索引库当中
     * @param list
     */
    public void importList(List<TbItem> list);

    /**
     * 删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);
}