package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.OmsOrderSetting;

import com.weikun.api.service.IOmsOrderSettingService;
import com.weikun.mall.provider.mapper.OmsOrderSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IOmsOrderSettingService",
        interfaceClass = IOmsOrderSettingService.class
)
@Transactional
public class OmsOrderSettingServiceImpl implements IOmsOrderSettingService {
    @Autowired
    private OmsOrderSettingMapper orderSettingMapper;


    //注解方式 做缓存
    @Cacheable(cacheNames= {"OrderSetting"},unless="#result == null",key = "#id")
    @Override
    public OmsOrderSetting getItem(Long id) {
        return orderSettingMapper.selectByPrimaryKey(id);
    }

    @CachePut(cacheNames = {"OrderSetting"},key = "#id")
    @Override
    public OmsOrderSetting update(Long id, OmsOrderSetting orderSetting) {
        orderSetting.setId(id);
        orderSettingMapper.updateByPrimaryKey(orderSetting);
        return orderSetting;
    }
}
