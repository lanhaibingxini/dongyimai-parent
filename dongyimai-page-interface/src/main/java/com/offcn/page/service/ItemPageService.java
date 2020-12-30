package com.offcn.page.service;

/**
 * 商品详情页的接口
 */
public interface ItemPageService {
    /**
     * 根据商品id生成商品详情页的方法
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 根据商品id删除静态页面
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
