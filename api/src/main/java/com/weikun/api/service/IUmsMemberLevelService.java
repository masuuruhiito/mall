package com.weikun.api.service;

import com.weikun.api.model.UmsMemberLevel;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/22
 * 描述你的类：
 */
public interface IUmsMemberLevelService {
    /**
     * 获取所有会员登录
     * @param defaultStatus 是否为默认会员
     */
    List<UmsMemberLevel> list(Integer defaultStatus);
}
