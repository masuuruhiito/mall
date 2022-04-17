package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.ProductAttrInfo;
import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.model.PmsProductAttributeCategory;
import com.weikun.api.model.PmsProductAttributeExample;
import com.weikun.api.service.IProductAttributeService;
import com.weikun.mall.provider.mapper.PmsProductAttributeCategoryMapper;
import com.weikun.mall.provider.mapper.PmsProductAttributeMapper;
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
        interfaceName = "com.weikun.api.service.IProductAttributeService",
        interfaceClass = IProductAttributeService.class
)
@Transactional
public class ProductAttributeServiceImpl implements IProductAttributeService {

    @Autowired
    private PmsProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Autowired
    private PmsProductAttributeMapper productAttributeMapper;
    @Autowired
    private CacheManager cacheManager;

    @Cacheable(cacheNames= {"ProductAttributeList"},
            unless="#result == null",key = "#cid+'-'+#type+'-'+#pageNum+'-'+#pageSize")
    @Override
    public CommonPage getList(Long cid, Integer type, Integer pageSize, Integer pageNum) {

        PageHelper.startPage(pageNum, pageSize);
        PmsProductAttributeExample example = new PmsProductAttributeExample();
        example.setOrderByClause("sort desc");
        example.createCriteria().andProductAttributeCategoryIdEqualTo(cid).andTypeEqualTo(type);
        List<PmsProductAttribute> list=productAttributeMapper.selectByExample(example);
        return CommonPage.restPage(list);
    }
    @CachePut(cacheNames = {"ProductAttribute"},key="#pmsProductAttribute.id") //注意：添加缓存时
    @Override
    public PmsProductAttribute create(PmsProductAttribute pmsProductAttribute) {
        clearAttributeBufferList();
        int count = productAttributeMapper.insertSelective(pmsProductAttribute);
//        //新增商品属性以后需要更新商品属性分类数量 属性必须做数量说明。
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper.selectByPrimaryKey(pmsProductAttribute.getProductAttributeCategoryId());
        if(pmsProductAttribute.getType()==0){
            pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount()+1);
        }else if(pmsProductAttribute.getType()==1){
            pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount()+1);
        }

        productAttributeCategoryMapper.updateByPrimaryKey(pmsProductAttributeCategory);

        return pmsProductAttribute;
    }
    @CachePut(cacheNames = {"ProductAttribute"},key = "#productAttribute.id")
    @Override
    public PmsProductAttribute update(PmsProductAttribute productAttribute) {//修改必须有修改后的对象 否则缓存没法转换
        clearAttributeBufferList();
        productAttributeMapper.updateByPrimaryKeySelective(productAttribute);
        return productAttribute;

    }
    @Cacheable(cacheNames= {"ProductAttribute"},unless="#result == null",key = "#id")
    @Override
    public PmsProductAttribute getItem(Long id) {
        return productAttributeMapper.selectByPrimaryKey(id);
    }
    @CacheEvict(cacheNames = {"ProductAttribute"},
            key = "#ids[0]", allEntries = true,beforeInvocation=false )
    @Override
    public int delete(List<Long> ids) {
        //获取分类
        clearAttributeBufferList();
        PmsProductAttribute pmsProductAttribute = productAttributeMapper.selectByPrimaryKey(ids.get(0));
        Integer type = pmsProductAttribute.getType();
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper.selectByPrimaryKey(pmsProductAttribute.getProductAttributeCategoryId());
        PmsProductAttributeExample example = new PmsProductAttributeExample();
        example.createCriteria().andIdIn(ids);
        int count = productAttributeMapper.deleteByExample(example);
        //删除完成后修改数量
        if(type==0){
            if(pmsProductAttributeCategory.getAttributeCount()>=count){
                pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount()-count);
            }else{
                pmsProductAttributeCategory.setAttributeCount(0);
            }
        }else if(type==1){
            if(pmsProductAttributeCategory.getParamCount()>=count){
                pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount()-count);
            }else{
                pmsProductAttributeCategory.setParamCount(0);
            }
        }
        productAttributeCategoryMapper.updateByPrimaryKey(pmsProductAttributeCategory);
        return count;
    }

    //ProductAttrInfo 两个表中的字段组合的临时类，其中
    //attributeCategoryId：pms_product_attribute_category表的id字段
    //attributeId:pms_product_attribute表的id字段

    @Cacheable(cacheNames= {"ProductAttrInfoList"},
            unless="#result == null",key = "#productCategoryId")
    @Override
    public List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId){//根据种类id，取出所有的属性
        List<ProductAttrInfo> list=productAttributeMapper.getProductAttrInfo(productCategoryId);
        return list;
    }



    private void clearAttributeBufferList(){
        cacheManager.getCache("ProductAttributeList").clear();//删除查询大集合
        cacheManager.getCache("ProductAttrInfoList").clear();//删除查询大集合

    }
}
