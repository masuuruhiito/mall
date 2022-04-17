package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.model.CmsSubject;
import com.weikun.api.model.CmsSubjectExample;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.ICmsSubjectService;
import com.weikun.mall.provider.mapper.CmsSubjectMapper;
import com.weikun.mall.provider.mapper.PmsBrandMapper;
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
        interfaceName = "com.weikun.api.service.ICmsSubjectService",
        interfaceClass = ICmsSubjectService.class
)
@Transactional
public class CmsSubjectServiceImpl  implements ICmsSubjectService {

    @Autowired
    private CmsSubjectMapper cmsSubjectMapper;

    @Autowired
    private CacheManager cacheManager;

    //注解方式 做缓存 判断是空的时候 用 在缓存中用如下区分'b'+#pageNum+'-'+#pageSize
    @Cacheable(cacheNames= {"CmsSubjectList"},unless="#result == null") //当结果为空时不缓存
    @Override
    public List<CmsSubject> listAll() {
        return cmsSubjectMapper.selectByExample(new CmsSubjectExample());
    }

    private void clearCmsSubjectBufferList(){
        cacheManager.getCache("CmsSubjectList").clear();//删除::之前叫BrandList的所有集合

    }
}
