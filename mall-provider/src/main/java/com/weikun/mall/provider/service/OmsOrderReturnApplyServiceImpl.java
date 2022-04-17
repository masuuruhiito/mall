package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.OmsReturnApplyQueryParam;
import com.weikun.api.dto.OmsUpdateStatusParam;
import com.weikun.api.model.OmsOrderReturnApply;
import com.weikun.api.model.OmsOrderReturnApplyExample;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IOmsOrderReturnApplyService;
import com.weikun.mall.provider.mapper.OmsOrderReturnApplyMapper;
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
        interfaceName = "com.weikun.api.service.IOmsOrderReturnApplyService",
        interfaceClass = IOmsOrderReturnApplyService.class
)
@Transactional
public class OmsOrderReturnApplyServiceImpl implements IOmsOrderReturnApplyService {

    @Autowired
    private CacheManager cacheManager;


    @Autowired
    private OmsOrderReturnApplyMapper returnApplyMapper;

    //得有字母占位，否则 多条件查询 条件不一样 会出现雷同的结果

    @Cacheable(cacheNames= {"OrderReturnApplyList"},unless="#result == null",
            key="T(String).valueOf(#pageNum+'-'+#pageSize)" +
                    ".concat(#queryParam.id!=null?#queryParam.id:'id')"+
                    ".concat(#queryParam.status!=null?#queryParam.status:'s') "+
                    ".concat(#queryParam.handleMan!=null?#queryParam.handleMan:'hm') "+
                    ".concat(#queryParam.createTime!=null ?#queryParam.createTime:'ct') "+
                    ".concat(#queryParam.handleTime!=null ?#queryParam.handleTime:'ht')"+
                    ".concat(#queryParam.receiverKeyword!=null?#queryParam.receiverKeyword:'rk')"
    )
    @Override
    public CommonPage list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        List<OmsOrderReturnApply> list= returnApplyMapper.getOrderReturnApplyList(queryParam);
        return CommonPage.restPage(list);
    }
    @CacheEvict(cacheNames = {"OrderReturnApply"},
            key = "#id", allEntries = true,beforeInvocation=false )
    @Override
    public int delete(List<Long> ids) {
        clearBrandBufferList();
        OmsOrderReturnApplyExample example = new OmsOrderReturnApplyExample();
        example.createCriteria().andIdIn(ids).andStatusEqualTo(3);
        return returnApplyMapper.deleteByExample(example);
    }
    @CachePut(cacheNames= {"OrderReturnApply"},unless="#result == null",key = "#id")
    @Override
    public OmsOrderReturnApply updateStatus(Long id, OmsUpdateStatusParam statusParam) {
        Integer status = statusParam.getStatus();
        OmsOrderReturnApply returnApply = this.getItem(id);
        if(status.equals(1)){
            //确认退货
            returnApply.setId(id);
            returnApply.setStatus(1);
            returnApply.setReturnAmount(statusParam.getReturnAmount());
            returnApply.setCompanyAddressId(statusParam.getCompanyAddressId());
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else if(status.equals(2)){
            //完成退货
            returnApply.setId(id);
            returnApply.setStatus(2);
            returnApply.setReceiveTime(new Date());
            returnApply.setReceiveMan(statusParam.getReceiveMan());
            returnApply.setReceiveNote(statusParam.getReceiveNote());
        }else if(status.equals(3)){
            //拒绝退货
            returnApply.setId(id);
            returnApply.setStatus(3);
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else{
            return returnApply;
        }
        returnApplyMapper.updateByPrimaryKeySelective(returnApply);
        return returnApply;
    }

    @Override
    public OmsOrderReturnApply getItem(Long id) {
        return returnApplyMapper.selectByPrimaryKey(id);
    }
    private void clearBrandBufferList(){
        cacheManager.getCache("OrderReturnApplyList").clear();//删除::之前叫BrandList的所有集合

    }
}
