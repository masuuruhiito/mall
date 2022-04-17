package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.OmsCompanyAddress;
import com.weikun.api.model.OmsCompanyAddressExample;
import com.weikun.api.service.IOmsCompanyAddressService;
import com.weikun.mall.provider.mapper.OmsCompanyAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
        interfaceName = "com.weikun.api.service.IOmsCompanyAddressService",
        interfaceClass = IOmsCompanyAddressService.class
)
@Transactional
public class OmsCompanyAddressService implements IOmsCompanyAddressService {
    @Autowired
    private OmsCompanyAddressMapper companyAddressMapper;


    @Cacheable(cacheNames= {"AddressList"},unless="#result == null")
    @Override
    public List<OmsCompanyAddress> list() {
        return companyAddressMapper.selectByExample(new OmsCompanyAddressExample());
    }
}
