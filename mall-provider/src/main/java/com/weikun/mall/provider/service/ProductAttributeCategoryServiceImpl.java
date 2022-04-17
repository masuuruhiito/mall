package com.weikun.mall.provider.service;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.PmsProductAttributeCategoryItem;
import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.model.PmsProductAttributeCategory;

import com.weikun.api.model.PmsProductAttributeCategoryExample;
import com.weikun.api.service.IProductAttributeCategoryService;
import com.weikun.mall.provider.mapper.PmsProductAttributeCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */

@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IProductAttributeCategoryService",
        interfaceClass = IProductAttributeCategoryService.class
)
@Transactional
public class ProductAttributeCategoryServiceImpl implements IProductAttributeCategoryService {
    @Autowired
    private PmsProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Autowired
    private CacheManager cacheManager;

    @CachePut(cacheNames = {"ProductAttributeCategory"},key = "#productAttributeCategory.id") //注意：添加缓存时
    @Override
    public PmsProductAttributeCategory create(PmsProductAttributeCategory productAttributeCategory) {
        clearProductAttributeCategoryBufferList();
        productAttributeCategoryMapper.insertSelective(productAttributeCategory);
        return productAttributeCategory;

    }
    //修改后 自动把Brand::id缓存覆盖
    @CachePut(cacheNames = {"ProductAttributeCategory"},key = "#id")
    @Override
    public PmsProductAttributeCategory update(Long id, String name) {//修改必须有修改后的对象 否则缓存没法转换
        clearProductAttributeCategoryBufferList();
        PmsProductAttributeCategory productAttributeCategory = new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);
        productAttributeCategory.setId(id);
        productAttributeCategoryMapper.updateByPrimaryKeySelective(productAttributeCategory);
        return productAttributeCategory;
    }

    @CacheEvict(cacheNames = {"ProductAttributeCategory"},
            key = "#id", allEntries = true,beforeInvocation=false )
    @Override
    public int delete(Long id) {
        clearProductAttributeCategoryBufferList();
        return productAttributeCategoryMapper.deleteByPrimaryKey(id);

    }
    @Cacheable(cacheNames= {"ProductAttributeCategory"},unless="#result == null",key = "#id")
    @Override
    public PmsProductAttributeCategory getItem(Long id) {
        return productAttributeCategoryMapper.selectByPrimaryKey(id);
    }
    @Cacheable(cacheNames= {"ProductAttributeCategoryList"},
            unless="#result == null",key = "'acl'+#pageNum+'-'+#pageSize") //当结果为空时不缓存
    @Override
    public CommonPage getList(Integer pageSize, Integer pageNum) {
        clearProductAttributeCategoryBufferList();
        PageHelper.startPage(pageNum, pageSize);
        List<PmsProductAttributeCategory> alist =productAttributeCategoryMapper.selectByExample(new PmsProductAttributeCategoryExample());
        return  CommonPage.restPage(alist);
    }
    @Cacheable(cacheNames= {"ProductAttributeCategoryItemList"},unless="#result == null") //当结果为空时不缓存
    @Override
    public List<PmsProductAttributeCategory> getListWithAttr() {//取出上机分类列表
        return productAttributeCategoryMapper.getListWithAttr();
    }

    private void clearProductAttributeCategoryBufferList(){
        cacheManager.getCache("ProductAttributeCategoryList").clear();//删除查询大集合
        cacheManager.getCache("ProductAttributeCategoryItemList").clear();//删除查询大集合
    }
}
