package com.weikun.api.service;

import com.weikun.api.model.OmsCompanyAddress;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：收货地址管理
 */
public interface IOmsCompanyAddressService {
    /**
     * 获取全部收货地址
     */
    List<OmsCompanyAddress> list();
}
