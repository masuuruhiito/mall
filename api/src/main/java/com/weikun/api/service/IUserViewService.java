package com.weikun.api.service;

import com.weikun.api.common.CommonPage;

import java.text.ParseException;

/**
 * 创建人：SHI
 * 创建时间：2021/11/22
 * 描述你的类：UserView统计
 */
public interface IUserViewService {




    CommonPage listUV(String start,String end,String type) ;

    CommonPage listTypeUV() throws Exception;
}
