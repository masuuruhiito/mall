package com.weikun.api.service;

import com.weikun.api.model.PmsSkuStock;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/10
 * 描述你的类：SKU库存管理
 */
public interface IPmsSkuStockService {
    /**
     * 根据产品id和skuCode模糊搜索
     */
    List<PmsSkuStock> getList(Long pid, String keyword);

    /**
     * 批量更新商品库存信息
     */
    int update(Long pid, List<PmsSkuStock> skuStockList);
}
