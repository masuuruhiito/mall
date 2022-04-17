package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.model.PmsProductCategory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/22
 * 描述你的类：
 */
public interface IProductCategoryService {

    PmsProductCategory create(PmsProductCategory pmsProductCategory);
//
//
    PmsProductCategory update(Long id, PmsProductCategory pmsProductCategory);
//
    CommonPage getList(Long parentId, Integer pageSize, Integer pageNum);

    int delete(Long id);

    PmsProductCategory getItem(Long id);
//
    PmsProductCategory updateNavStatus(Long id, Integer navStatus);
//
    PmsProductCategory updateShowStatus(Long id, Integer showStatus);
//
    List<PmsProductCategory> listWithChildren();
}
