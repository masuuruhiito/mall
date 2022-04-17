package com.weikun.mall.provider.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.model.*;
import com.weikun.api.service.IProductAttributeService;
import com.weikun.api.service.IProductCategoryService;
import com.weikun.mall.provider.mapper.PmsProductCategoryAttributeRelationMapper;
import com.weikun.mall.provider.mapper.PmsProductCategoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IProductCategoryService",
        interfaceClass = IProductCategoryService.class
)
@Transactional
public class ProductCategoryServiceImpl implements IProductCategoryService {

    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PmsProductCategoryAttributeRelationMapper productCategoryAttributeRelationMapper;

    @CachePut(cacheNames = {"ProductCategory"},key="#productCategory.id") //注意：添加缓存时
    @Override
    public PmsProductCategory create(PmsProductCategory productCategory) {
        clearAttributeBufferList();
        productCategory.setProductCount(0);//产品个数 新分类 下面还没有产品
        //没有父分类时为一级分类  //有父分类时选择根据父分类level设置
        setCategoryLevel(productCategory);
        productCategoryMapper.insertSelective(productCategory);
        //创建筛选属性关联
        List<Long> productAttributeIdList = productCategory.getProductAttributeIdList();

        if(!CollectionUtils.isEmpty(productAttributeIdList)){//增加当前分类的筛选属性
            insertRelationList(productCategory.getId(), productAttributeIdList);
        }
        return productCategory;
    }
    @CachePut(cacheNames = {"ProductCategory"},key = "#productCategory.id")
    @Override
    public PmsProductCategory update(Long id, PmsProductCategory productCategory) {
        clearAttributeBufferList();
        productCategory.setId(id);

        setCategoryLevel(productCategory);
        //更新商品分类时要更新商品中的名称
//        PmsProduct product = new PmsProduct();
//        product.setProductCategoryName(productCategory.getName());
//        PmsProductExample example = new PmsProductExample();
//        example.createCriteria().andProductCategoryIdEqualTo(id);
//        productMapper.updateByExampleSelective(product,example);
//        //同时更新筛选属性的信息
        if(!CollectionUtils.isEmpty(productCategory.getProductAttributeIdList())){
            PmsProductCategoryAttributeRelationExample relationExample = new PmsProductCategoryAttributeRelationExample();
            relationExample.createCriteria().andProductCategoryIdEqualTo(id);
            productCategoryAttributeRelationMapper.deleteByExample(relationExample);
            insertRelationList(id,productCategory.getProductAttributeIdList());
        }else{
            PmsProductCategoryAttributeRelationExample relationExample = new PmsProductCategoryAttributeRelationExample();
            relationExample.createCriteria().andProductCategoryIdEqualTo(id);
            productCategoryAttributeRelationMapper.deleteByExample(relationExample);
        }
        productCategoryMapper.updateByPrimaryKeySelective(productCategory);
        return productCategory;



    }

    /**
     * 批量插入商品分类与筛选属性关系表
     * @param productCategoryId 商品分类id
     * @param productAttributeIdList 相关商品筛选属性id集合
     */
    private void insertRelationList(Long productCategoryId, List<Long> productAttributeIdList) {
        List<PmsProductCategoryAttributeRelation> relationList = new ArrayList<>();
        for (Long productAttrId : productAttributeIdList) {
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            relation.setProductAttributeId(productAttrId);
            relation.setProductCategoryId(productCategoryId);
            relationList.add(relation);
        }
        productCategoryAttributeRelationMapper.insertList(relationList);
    }
    /**
     * 根据分类的parentId设置分类的level
     */
    private void setCategoryLevel(PmsProductCategory productCategory) {
        //没有父分类时为一级分类
        if (productCategory.getParentId() == 0) {
            productCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = productCategoryMapper.selectByPrimaryKey(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                productCategory.setLevel(0);
            }
        }
    }

    @Cacheable(cacheNames= {"ProductCategoryList"},
            unless="#result == null",key = "#parentId+'-'+#pageNum+'-'+#pageSize")
    @Override
    public CommonPage getList(Long parentId, Integer pageSize, Integer pageNum) {

        PageHelper.startPage(pageNum, pageSize);
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.setOrderByClause("sort desc");
        example.createCriteria().andParentIdEqualTo(parentId);
        List<PmsProductCategory> list=productCategoryMapper.selectByExample(example);
        return CommonPage.restPage(list);

    }
    @CacheEvict(cacheNames = {"ProductCategory"},
            key = "#id", allEntries = true,beforeInvocation=false )
    @Override
    public int delete(Long id) {
        clearAttributeBufferList();
        //先把儿子的id找到，在逐个删除 这样可以删除儿子所对应的属性
        //儿子先要删除
        PmsProductCategoryExample example=new PmsProductCategoryExample();
        example.createCriteria().andParentIdEqualTo(id);
        List<PmsProductCategory> list=productCategoryMapper.selectByExample(example);
        list.forEach(s->{
            productCategoryMapper.deleteByPrimaryKey(s.getId());
            PmsProductCategoryAttributeRelationExample example1=new PmsProductCategoryAttributeRelationExample();
            example1.createCriteria().andProductCategoryIdEqualTo(s.getId());

            productCategoryAttributeRelationMapper.deleteByExample(example1);

        });

        //父亲再删除
        //还要删除此种类对应的属性
        PmsProductCategoryAttributeRelationExample example1=new PmsProductCategoryAttributeRelationExample();
        example1.createCriteria().andProductCategoryIdEqualTo(id);
        productCategoryAttributeRelationMapper.deleteByExample(example1);
        return productCategoryMapper.deleteByPrimaryKey(id);



    }
    //注解方式 做缓存
    @Cacheable(cacheNames= {"ProductCategory"},unless="#result == null",key = "#id")
    @Override
    public PmsProductCategory getItem(Long id) {

        return productCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    @CachePut(cacheNames = {"ProductCategory"},key = "#id")
    public PmsProductCategory updateNavStatus(Long id, Integer navStatus) {
        clearAttributeBufferList();
        PmsProductCategory productCategory = this.getItem(id);
        productCategory.setNavStatus(navStatus);

        productCategoryMapper.updateByPrimaryKeySelective(productCategory);
        return productCategory;

    }

    @Override
    @CachePut(cacheNames = {"ProductCategory"},key = "#id")
    public PmsProductCategory updateShowStatus(Long id, Integer showStatus) {
        clearAttributeBufferList();
        PmsProductCategory productCategory = this.getItem(id);
        productCategory.setShowStatus(showStatus);
        productCategoryMapper.updateByPrimaryKeySelective(productCategory);
        return productCategory;
    }
    @Cacheable(cacheNames= {"ProductCategoryList"},unless="#result == null")
    @Override
    public List<PmsProductCategory> listWithChildren() {
        return productCategoryMapper.listWithChildren();
    }
    private void clearAttributeBufferList(){
        cacheManager.getCache("ProductCategoryList").clear();//删除查询大集合

    }
}
