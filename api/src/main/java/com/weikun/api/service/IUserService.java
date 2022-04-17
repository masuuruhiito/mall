package com.weikun.api.service;


import com.weikun.api.dto.UmsAdminLoginParam;
import com.weikun.api.model.UmsAdmin;

/**
 * 创建人：SHI
 * 创建时间：2021/11/22
 * 描述你的类：
 */

public interface IUserService {

    public UmsAdmin login(UmsAdminLoginParam user) ;
    public UmsAdmin findUserById(Long userId);
    public UmsAdmin findByUsername(String username);
    public UmsAdmin reg(UmsAdmin user) ;
    public UmsAdmin findByUmsAdmin(String token);
}
