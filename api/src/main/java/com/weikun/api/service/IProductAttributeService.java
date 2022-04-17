package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.ProductAttrInfo;
import com.weikun.api.model.PmsProductAttribute;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/10
 * 描述你的类：
 */
public interface IProductAttributeService {
    /**
     * 根据分类分页获取商品属性
     * @param cid 分类id
     * @param type 0->属性；2->参数
     * @return
     */
    CommonPage getList(Long cid, Integer type, Integer pageSize, Integer pageNum);

    /**
     * 添加商品属性
     */

    PmsProductAttribute create(PmsProductAttribute pmsProductAttribute);

    /**
     * 修改商品属性
     */
    PmsProductAttribute update(PmsProductAttribute productAttribute);

    /**
     * 获取单个商品属性信息
     */
    PmsProductAttribute getItem(Long id);


    int delete(List<Long> ids);

    List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId);
}
