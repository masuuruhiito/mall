package com.weikun.api.service;

import com.weikun.api.model.CmsSubject;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：
 */
public interface ICmsSubjectService {

    /**
     * 查询所有专题
     */
    List<CmsSubject> listAll();
}
