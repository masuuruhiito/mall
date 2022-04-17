package com.weikun.mall.provider.service;


import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.UMSLog;
import com.weikun.api.service.IUMSLogService;

import com.weikun.mall.provider.mapper.UMSLogMapper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IUMSLogService",
        interfaceClass = IUMSLogService.class
)
public class UMSLogServiceImpl implements IUMSLogService {

    @Autowired
    private UMSLogMapper dao;


    @Override
    public Integer insert(UMSLog log) {
        return dao.insert(log);

    }
}
