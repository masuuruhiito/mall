package com.weikun.mall.provider.mapper.redis.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Set;

/**
 * 创建人：Jason
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Repository
public class RedisUtilMapper {
    @Autowired
    private RedisTemplate<String, Object> redisTemplateS;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    public Object get(String key) {
        return key == null ? null : redisTemplateS.opsForValue().get(key);
    }

    public Set<Object> getAllKeys(Serializable pattern){
        Set<Object> s=redisTemplate.keys(pattern);
        return s;
    }
}
