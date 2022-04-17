package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.PmsProductAttributeCategoryItem;
import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.model.PmsProductAttributeCategory;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/10
 * 描述你的类：
 */
public interface IProductAttributeCategoryService {
    PmsProductAttributeCategory create(PmsProductAttributeCategory productAttributeCategory);

    PmsProductAttributeCategory update(Long id, String name);

    int delete(Long id);

    PmsProductAttributeCategory getItem(Long id);

    CommonPage getList(Integer pageSize, Integer pageNum);

    List<PmsProductAttributeCategory> getListWithAttr();


}
