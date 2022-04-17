package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;


import com.weikun.api.model.OmsOrderReturnReason;
import com.weikun.api.model.OmsOrderReturnReasonExample;
import com.weikun.api.service.IOmsOrderReturnReasonService;
import com.weikun.mall.provider.mapper.OmsOrderReturnReasonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */

@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IOmsOrderReturnReasonService",
        interfaceClass = IOmsOrderReturnReasonService.class
)
@Transactional
public class OmsOrderReturnReasonServiceImpl implements IOmsOrderReturnReasonService {

    @Autowired
    private OmsOrderReturnReasonMapper returnReasonMapper;

    @Autowired
    private CacheManager cacheManager;

    @CachePut(cacheNames = {"OrderReturnReason"},key = "#returnReason.id")
    @Override
    public OmsOrderReturnReason create(OmsOrderReturnReason returnReason) {
        clearOrderReturnReason();
        returnReason.setCreateTime(new Date());
        returnReasonMapper.insert(returnReason);
        return returnReason;
    }


    @CacheEvict(cacheNames = {"OrderReturnReason"},
            key = "#ids[0]", allEntries = true,beforeInvocation=false )
    @Override
    public int delete(List<Long> ids) {
        clearOrderReturnReason();
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        example.createCriteria().andIdIn(ids);
        return returnReasonMapper.deleteByExample(example);
    }

    @Cacheable(cacheNames= {"OrderReturnReasonList"},unless="#result == null",
            key = "#pageNum+'-'+#pageSize")
    @Override
    public CommonPage list(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        example.setOrderByClause("sort desc");
        return CommonPage.restPage(returnReasonMapper.selectByExample(example));
    }
    @CachePut(cacheNames= {"OrderReturnReason"},unless="#result == null",
            key = "#id")
    @Override
    public OmsOrderReturnReason updateStatus(Long id, Integer status) {
        clearOrderReturnReason();
        OmsOrderReturnReason record=null ;
        if(!status.equals(0)&&!status.equals(1)){
            return record;
        }
        record= getOmsOrderReturnReason(id);
        record.setStatus(status);
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();

        example.createCriteria().andIdEqualTo(id);
        returnReasonMapper.updateByExampleSelective(record,example);
        return record;
    }
    @Cacheable(cacheNames= {"OrderReturnReason"},unless="#result == null",key = "#id")
    @Override
    public OmsOrderReturnReason getOmsOrderReturnReason(Long id) {
        return returnReasonMapper.selectByPrimaryKey(id);
    }
    @CachePut(cacheNames = {"OrderReturnReason"},key = "#id")
    @Override
    public OmsOrderReturnReason update(Long id, OmsOrderReturnReason returnReason) {
        clearOrderReturnReason();
        returnReason.setId(id);
        returnReasonMapper.updateByPrimaryKey(returnReason);
        return returnReason;
    }
    private void clearOrderReturnReason(){
        cacheManager.getCache("OrderReturnReasonList").clear();//删除::之前叫BrandList的所有集合

    }
}
