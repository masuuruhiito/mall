package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;

import com.weikun.api.common.CommonPage;
import com.weikun.api.common.ConstVar;
import com.weikun.api.model.PmsBrand;
import com.weikun.api.model.PmsBrandExample;
import com.weikun.api.model.PmsProduct;
import com.weikun.api.model.PmsProductExample;
import com.weikun.api.service.IBrandService;
import com.weikun.mall.provider.mapper.PmsBrandMapper;
import com.weikun.mall.provider.mapper.PmsProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static com.weikun.api.common.ConstVar.SHOW_BRAND_IMAGE_PATH;


/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IBrandService",
        interfaceClass = IBrandService.class
)
@Transactional
public class BrandServiceImpl implements IBrandService{

    @Autowired
    private PmsBrandMapper brandMapper;//


    @Autowired
    private PmsProductMapper productMapper;//

    @Autowired
    private CacheManager cacheManager;

    //注解方式 做缓存 判断是空的时候 用 在缓存中用如下区分'b'+#pageNum+'-'+#pageSize 如果是null 就用 #pageNum+'-'+#pageSize
    @Cacheable(cacheNames= {"BrandList"},unless="#result == null",
            key = "#keyword!=null?#keyword+'-'+#pageNum+'-'+#pageSize: #pageNum+'-'+#pageSize") //当结果为空时不缓存
    @Override
    public CommonPage listBrand(String keyword, int pageNum, int pageSize) {

        System.out.println("执行了！----------------------------------------------");
        /*
         *  pageNum  页码
         * pageSize 每页显示数量
         * PageHelper分页在Dubbo中必须在一端执行，调了 4个小时，妈的，且CommonPage对象需要序列化
         */
        PageHelper.startPage(pageNum, pageSize);
        PmsBrandExample pmsBrandExample = new PmsBrandExample();
        pmsBrandExample.setOrderByClause("sort desc");//倒叙
        PmsBrandExample.Criteria criteria = pmsBrandExample.createCriteria();
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andNameLike("%" + keyword + "%");//有关键字就查
        }
        List <PmsBrand> brandList= brandMapper.selectByExample(pmsBrandExample);
        //PageInfo<PmsBrand> pageInfo = new PageInfo<PmsBrand>(brandList);
//        //打印分页信息
//        System.out.println("数据总数：" + pageInfo.getTotal());
//        System.out.println("数据总页数：" + pageInfo.getPages());
//        System.out.println("每页行数：" + pageInfo.getPageSize());
//        System.out.println("当前页码：" + pageInfo.getPageNum());
//        System.out.println("最后一页页号：" + pageInfo.getNavigateLastPage());
//        System.out.println("第一页页号：" + pageInfo.getNavigateFirstPage());

        return CommonPage.restPage(brandList);
    }
   // @CachePut(cacheNames = {"brand"}, key="#ids[0]")修改部分字段 不应该用缓存 否则带不出来全部PmsBrand值，因此在缓存里字段内容为空
   @CachePut(cacheNames= {"Brand"},unless="#result == null",
           key = "#id")
    @Override
   public PmsBrand updateShowStatus(Long id, Integer showStatus) {
        //修改品牌 必须把品牌的集合缓存清除 否则 不一致
        clearBrandBufferList();

        PmsBrand pmsBrand =getBrand(id);
        pmsBrand.setShowStatus(showStatus);
        brandMapper.updateByPrimaryKeySelective(pmsBrand);
        return pmsBrand;
    }

    @CachePut(cacheNames= {"Brand"},unless="#result == null",
            key = "#id")
    @Override
    public PmsBrand updateFactoryStatus(Long id, Integer factoryStatus) {
        clearBrandBufferList();
       // clearBrandBuffer(ids.get(0));
        PmsBrand pmsBrand = getBrand(id);//取得缓存或者数据库值 否则数据仅改factoryStatus 其他字段都为空
        pmsBrand.setFactoryStatus(factoryStatus);
        pmsBrand.setId(id);
//        PmsBrandExample pmsBrandExample = new PmsBrandExample();
//        pmsBrandExample.createCriteria().andIdIn(ids); 缓存不支持多条修改
        //brandMapper.updateByExampleSelective(pmsBrand);
        brandMapper.updateByPrimaryKeySelective(pmsBrand);
        return pmsBrand;

    }
    @CachePut(cacheNames = {"Brand"},key = "#pmsBrand.id") //注意：添加缓存时
    @Override
    public PmsBrand create(PmsBrand pmsBrand) { //catch 走PmsBrandParam 类 必须有toString
        clearBrandBufferList();

        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        //处理一下 上传图片的存储路径 品牌logo 品牌专区大图 增加的数据是8.jpg
      //  pmsBrand.setLogo(SHOW_BRAND_IMAGE_PATH+""+pmsBrand.getLogo());

       // pmsBrand.setBigPic(SHOW_BRAND_IMAGE_PATH+""+pmsBrand.getBigPic());

        brandMapper.insertSelective(pmsBrand);
        return pmsBrand;
    }
    @CacheEvict(cacheNames = {"Brand"},
            key = "#id", allEntries = true,beforeInvocation=false )
    @Override
    public int deleteBrand(Long id) {
        clearBrandBufferList();

        return brandMapper.deleteByPrimaryKey(id);
    }
    //注解方式 做缓存
    @Cacheable(cacheNames= {"Brand"},unless="#result == null",key = "#id")
    @Override
    public PmsBrand getBrand(Long id) {
        PmsBrand p=brandMapper.selectByPrimaryKey(id);
        System.out.println(brandMapper.selectByPrimaryKey(id));
        return p;
    }
    //修改后 自动把Brand::id缓存覆盖
    @CachePut(cacheNames = {"Brand"},key = "#id")//如果用result，它是返回值对象的id 做缓存的键 直接用id 就是形参的名字
    @Override
    public PmsBrand updateBrand(Long id, PmsBrand pmsBrand) {//修改必须有修改后的对象 否则缓存没法转换
        clearBrandBufferList();

        pmsBrand.setId(id);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        //更新品牌时要更新商品中的品牌名称
        PmsProduct product = new PmsProduct();
        product.setBrandName(pmsBrand.getName());
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andBrandIdEqualTo(id);
        productMapper.updateByExampleSelective(product,example);
        brandMapper.updateByPrimaryKeySelective(pmsBrand);
        return pmsBrand;
    }



    private void clearBrandBufferList(){
        cacheManager.getCache("BrandList").clear();//删除::之前叫BrandList的所有集合

    }
}
