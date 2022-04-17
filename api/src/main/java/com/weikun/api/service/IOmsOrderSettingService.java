package com.weikun.api.service;

import com.weikun.api.model.OmsOrderSetting;

/**
 * 订单设置Service
 */
public interface IOmsOrderSettingService {
    /**
     * 获取指定订单设置
     */
    OmsOrderSetting getItem(Long id);

    /**
     * 修改指定订单设置
     */
    OmsOrderSetting update(Long id, OmsOrderSetting orderSetting);
}
