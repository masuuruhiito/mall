package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.PmsSkuStock;
import com.weikun.api.model.PmsSkuStockExample;
import com.weikun.api.service.IPmsProductService;
import com.weikun.api.service.IPmsSkuStockService;
import com.weikun.mall.provider.mapper.PmsSkuStockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IPmsSkuStockService",
        interfaceClass = IPmsSkuStockService.class
)
@Transactional
public class PmsSkuStockService implements IPmsSkuStockService {
    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private CacheManager cacheManager;
//    @Autowired
//    private PmsSkuStockDao skuStockDao;
//注解方式 做缓存 判断是空的时候 用 在缓存中用如下区分'b'+#pageNum+'-'+#pageSize 如果是null 就用 #pageNum+'-'+#pageSize
    @Cacheable(cacheNames= {"SkuStockList"},unless="#result == null",
        key = "#keyword!=null?#keyword+'-'+#pid:#pid")
    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        PmsSkuStockExample example = new PmsSkuStockExample();
        PmsSkuStockExample.Criteria criteria = example.createCriteria().andProductIdEqualTo(pid);
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andSkuCodeLike("%" + keyword + "%");
        }
        return skuStockMapper.selectByExample(example);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        clearBrandBufferList();
        return skuStockMapper.replaceList(skuStockList);
    }

    private void clearBrandBufferList(){
        cacheManager.getCache("SkuStockList").clear();//删除::之前叫SkuStockList的所有集合

    }
}
