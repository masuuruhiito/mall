package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.PmsProductQueryParam;
import com.weikun.api.model.*;
import com.weikun.api.service.IPmsProductService;
import com.weikun.mall.provider.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.weikun.api.common.ConstVar.SHOW_PRODUCT_IMAGE_PATH;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IPmsProductService",
        interfaceClass = IPmsProductService.class
)
@Transactional
public class PmsProductServiceImpl implements IPmsProductService {

    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsMemberPriceMapper memberPriceMapper;//会员价格 批量加入

    @Autowired
    private PmsProductLadderMapper productLadderMapper;//阶梯价格 批量加入

    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;//满减价格

    @Autowired
    private PmsSkuStockMapper skuStockMapper;//库存

    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    private CmsSubjectProductRelationMapper subjectProductRelationMapper;//关联主题

    @Autowired  //关联优选
    private CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;

    @Autowired
    private CacheManager cacheManager;
    @CachePut(cacheNames = {"Product"},key = "#result.id") //注意：添加缓存时
    @Override //由于添加的商品 不适合使用缓存 因此，就不用了
    public PmsProduct create(PmsProduct product) {
        clearProductBufferList();
        int count;
        //创建商品
       // PmsProduct product = productParam;
        product.setId(null);//id为自动增加字段 不用赋值  需用@Options注解，取出增加后的id
        //处理商品图册 pic字段
        product.setPic(product.getPic());        //
        String pics=product.getAlbumPics();//多个图片，','号做分割
        StringBuilder sb = new StringBuilder();
        List list=Arrays.asList(pics.split(","));
        for(int i = 0; i < list.size(); i++) {
            if (sb.length() > 0) {//该步即不会第一位有逗号，也防止最后一位拼接逗号！
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        System.out.println(sb.toString());
        product.setAlbumPics(sb.toString());
        productMapper.insertSelective(product);
        //根据促销类型设置会员价格：、阶梯价格、满减价格
        Long productId = product.getId();
        //增加会员价格
        relateAndInsertList(memberPriceMapper, product.getMemberPriceList(), productId);
        //阶梯价格
        relateAndInsertList(productLadderMapper, product.getProductLadderList(), productId);
        //满减价格
        relateAndInsertList(productFullReductionMapper, product.getProductFullReductionList(), productId);
        //处理sku的编码
        handleSkuStockCode(product.getSkuStockList(),productId);
        //添加sku库存信息
        relateAndInsertList(skuStockMapper, product.getSkuStockList(), productId);
        //添加商品参数,添加自定义商品规格
        relateAndInsertList(productAttributeValueMapper, product.getProductAttributeValueList(), productId);
        //关联专题
        relateAndInsertList(subjectProductRelationMapper, product.getSubjectProductRelationList(), productId);
        //关联优选
        relateAndInsertList(prefrenceAreaProductRelationMapper, product.getPrefrenceAreaProductRelationList(), productId);
        count = 1;
        return product;
    }
    @Cacheable(cacheNames= {"ProductList"},
            unless="#result == null",
            //T(String).valueOf(#name).concat('-').concat(#password))
            //得有字母占位，否则 多条件查询 条件不一样 会出现雷同的结果
            key="T(String).valueOf(#pageNum+'-'+#pageSize)" +
                    ".concat(#productQueryParam.keyword!=null?#productQueryParam.keyword:'k')" +
                    ".concat(#productQueryParam.verifyStatus!=null?#productQueryParam.verifyStatus:'vs') "+
                    ".concat(#productQueryParam.productSn!=null?#productQueryParam.productSn:'ps') "+
                    ".concat(#productQueryParam.productCategoryId!=null?#productQueryParam.productCategoryId:'pc') "+
                    ".concat(#productQueryParam.brandId!=null?#productQueryParam.brandId:'b')") //当每个查询条件不为空时
    //商品部分字段查询
    @Override
    public CommonPage list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProductExample productExample = new PmsProductExample();
        PmsProductExample.Criteria criteria = productExample.createCriteria();
        criteria.andDeleteStatusEqualTo(0);//没被删除
        if (productQueryParam.getPublishStatus() != null) {
            criteria.andPublishStatusEqualTo(productQueryParam.getPublishStatus());
        }
        if (productQueryParam.getVerifyStatus() != null) {
            criteria.andVerifyStatusEqualTo(productQueryParam.getVerifyStatus());
        }
        if (!StringUtils.isEmpty(productQueryParam.getKeyword())) {
            criteria.andNameLike("%" + productQueryParam.getKeyword() + "%");
        }
        if (!StringUtils.isEmpty(productQueryParam.getProductSn())) {
            criteria.andProductSnEqualTo(productQueryParam.getProductSn());
        }
        if (productQueryParam.getBrandId() != null) {
            criteria.andBrandIdEqualTo(productQueryParam.getBrandId());
        }
        if (productQueryParam.getProductCategoryId() != null) {
            criteria.andProductCategoryIdEqualTo(productQueryParam.getProductCategoryId());
        }
        List list=productMapper.selectByExample(productExample);
        return CommonPage.restPage(list);
    }



    @Override//部分修改 不用加缓存

    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        clearProductBufferList();
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }


    @Override
    @CachePut(cacheNames = {"Product"},key = "#id")
    public PmsProduct updatePublishStatus(Long id, Integer publishStatus) {
        clearProductBufferList();
        PmsProduct record = getUpdateInfo(id);
        record.setPublishStatus(publishStatus);

        productMapper.updateByPrimaryKeySelective(record);
        return record;
    }

    @Override
    @CachePut(cacheNames = {"Product"},key = "#id")
    public PmsProduct updateRecommendStatus(Long id, Integer recommendStatus) {
        clearProductBufferList();
        PmsProduct record = getUpdateInfo(id);
        record.setRecommandStatus(recommendStatus);

        productMapper.updateByPrimaryKeySelective(record);
        return record;
    }
    @CachePut(cacheNames = {"Product"},key = "#id")//如果用result，它是返回值对象的id 做缓存的键 直接用id 就是形参的名字
    @Override
    public PmsProduct update(Long id, PmsProduct product) {
        clearProductBufferList();
        int count;
        //更新商品信息
       // PmsProduct product = productParam;
        product.setId(id);
        productMapper.updateByPrimaryKeySelective(product);
        //会员价格
        PmsMemberPriceExample pmsMemberPriceExample = new PmsMemberPriceExample();
        pmsMemberPriceExample.createCriteria().andProductIdEqualTo(id);
        memberPriceMapper.deleteByExample(pmsMemberPriceExample);
        relateAndInsertList(memberPriceMapper, product.getMemberPriceList(), id);
        //阶梯价格
        PmsProductLadderExample ladderExample = new PmsProductLadderExample();
        ladderExample.createCriteria().andProductIdEqualTo(id);
        productLadderMapper.deleteByExample(ladderExample);//先删除 在关联
        relateAndInsertList(productLadderMapper, product.getProductLadderList(), id);
        //满减价格
        PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
        fullReductionExample.createCriteria().andProductIdEqualTo(id);
        productFullReductionMapper.deleteByExample(fullReductionExample);
        relateAndInsertList(productFullReductionMapper, product.getProductFullReductionList(), id);
        //修改sku库存信息
        PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
        skuStockExample.createCriteria().andProductIdEqualTo(id);
        skuStockMapper.deleteByExample(skuStockExample);
        handleSkuStockCode(product.getSkuStockList(),id);
        relateAndInsertList(skuStockMapper, product.getSkuStockList(), id);
        //修改商品参数,添加自定义商品规格
        PmsProductAttributeValueExample productAttributeValueExample = new PmsProductAttributeValueExample();
        productAttributeValueExample.createCriteria().andProductIdEqualTo(id);
        productAttributeValueMapper.deleteByExample(productAttributeValueExample);
        relateAndInsertList(productAttributeValueMapper, product.getProductAttributeValueList(), id);
        //关联专题
        CmsSubjectProductRelationExample subjectProductRelationExample = new CmsSubjectProductRelationExample();
        subjectProductRelationExample.createCriteria().andProductIdEqualTo(id);
        subjectProductRelationMapper.deleteByExample(subjectProductRelationExample);
        relateAndInsertList(subjectProductRelationMapper, product.getSubjectProductRelationList(), id);
        //关联优选
        CmsPrefrenceAreaProductRelationExample prefrenceAreaExample = new CmsPrefrenceAreaProductRelationExample();
        prefrenceAreaExample.createCriteria().andProductIdEqualTo(id);
        prefrenceAreaProductRelationMapper.deleteByExample(prefrenceAreaExample);
        relateAndInsertList(prefrenceAreaProductRelationMapper, product.getPrefrenceAreaProductRelationList(), id);
        count = 1;
        return product;
    }

    @Cacheable(cacheNames= {"Product"},unless="#result == null",key ="#id" )
    @Override
    public PmsProduct getUpdateInfo(Long id) {
        return productMapper.getUpdateInfo(id);
    }

    @CachePut(cacheNames = {"Product"},key = "#id")
    @Override
    public PmsProduct updateNewStatus(Long id, Integer newStatus) {
        clearProductBufferList();
        PmsProduct record = this.getUpdateInfo(id);
        record.setNewStatus(newStatus);
        productMapper.updateByPrimaryKeySelective(record);
        return record;
    }

    private void clearProductBufferList(){

        cacheManager.getCache("ProductList").clear();//删除::之前叫BrandList的所有集合

    }


    //单独生成每个商品在库存中的SKU代码
    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        if(CollectionUtils.isEmpty(skuStockList))return;
        for(int i=0;i<skuStockList.size();i++){
            PmsSkuStock skuStock = skuStockList.get(i);
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                StringBuilder sb = new StringBuilder();
                //日期
                sb.append(sdf.format(new Date()));
                //四位商品id
                sb.append(String.format("%04d", productId));
                //三位索引id
                sb.append(String.format("%03d", i+1));
                skuStock.setSkuCode(sb.toString());
            }
        }
    }


    /**
     * 建立和插入关系表操作
     *
     * @param dao       可以操作的dao
     * @param dataList  要插入的数据
     * @param productId 建立关系的id
     *                               memberPriceDao, productParam.getMemberPriceList(), productId
     *                  利用反射技术执行dao中的insertList方法  ，把dataList的每笔数据保存进数据库，
     *                  并关联productId给每个对象
     */
    private void relateAndInsertList(Object dao, List dataList, Long productId) {
        try {
            if (!CollectionUtils.isEmpty(dataList)) {
                for (Object item : dataList) {
                    Method setId = item.getClass().getMethod("setId", Long.class);
                    setId.invoke(item, (Long) null);
                    Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                    setProductId.invoke(item, productId);
                }//把要增加的商品id 弄进去 自己的id 自动加一 不用管
                Method insertList = dao.getClass().getMethod("insertList", List.class);
                insertList.invoke(dao, dataList);
            } else {
                return;
            }
        } catch (Exception e) {
            System.out.printf("创建产品出错:%s", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
