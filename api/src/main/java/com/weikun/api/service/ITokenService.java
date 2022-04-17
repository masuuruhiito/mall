package com.weikun.api.service;

/**
 * 创建人：SHI
 * 创建时间：2021/11/22
 * 描述你的类：
 */
public interface ITokenService {
    public  String getToken(String userId,String password);//需要写token的生成方法
    public  String getUserId(String token);
    public  boolean checkSign(String token,String password);
}
