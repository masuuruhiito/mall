package com.weikun.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：UserView
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UmsUserView implements Serializable {
    //{date: '2020-02-07', orderCount: 10, orderAmount: 1093},
    private String updateTime;
    private int count;
    //private String name;

}
