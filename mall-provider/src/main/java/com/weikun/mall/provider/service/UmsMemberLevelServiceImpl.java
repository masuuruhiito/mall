package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.UmsMemberLevel;
import com.weikun.api.model.UmsMemberLevelExample;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IUmsMemberLevelService;
import com.weikun.mall.provider.mapper.UmsMemberLevelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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
        interfaceName = "com.weikun.api.service.IUmsMemberLevelService",
        interfaceClass = IUmsMemberLevelService.class
)
@Transactional
public class UmsMemberLevelServiceImpl implements IUmsMemberLevelService {
    @Autowired
    private UmsMemberLevelMapper memberLevelMapper;
    @Autowired
    private CacheManager cacheManager;

    //注解方式 做缓存 判断是空的时候 用 在缓存中用如下区分'b'+#pageNum+'-'+#pageSize
    @Cacheable(cacheNames= {"MemberLevelList"},unless="#result == null",
            key = "#defaultStatus") //当结果为空时不缓存

    @Override
    public List<UmsMemberLevel> list(Integer defaultStatus) {
        UmsMemberLevelExample example = new UmsMemberLevelExample();
        example.createCriteria().andDefaultStatusEqualTo(defaultStatus);
        return memberLevelMapper.selectByExample(example);
    }

    private void clearUmsMemberLevelBufferList(){
        cacheManager.getCache("MemberLevelList").clear();//删除::之前叫BrandList的所有集合

    }
}
