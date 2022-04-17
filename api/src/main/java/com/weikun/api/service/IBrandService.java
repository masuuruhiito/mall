package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.model.PmsBrand;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：
 */
//@Transaction已经在实现service标记了
public interface IBrandService {
    CommonPage listBrand(String keyword, int pageNum, int pageSize);

    PmsBrand updateShowStatus(Long id, Integer showStatus) ;

    PmsBrand updateFactoryStatus(Long id, Integer factoryStatus) ;

    PmsBrand create(PmsBrand pmsBrand) ;

    public int deleteBrand(Long id) ;
    public PmsBrand getBrand(Long id) ;
    public PmsBrand updateBrand(Long id, PmsBrand pmsBrandParam) ;
}
