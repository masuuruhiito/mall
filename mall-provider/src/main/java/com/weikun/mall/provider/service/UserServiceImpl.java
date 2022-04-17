package com.weikun.mall.provider.service;



import com.alibaba.dubbo.config.annotation.Service;

import com.weikun.api.dto.UmsAdminLoginParam;
import com.weikun.api.model.UmsAdmin;
import com.weikun.api.service.IUserService;
import com.weikun.mall.provider.mapper.UmsAdminMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IUserService",
        interfaceClass = IUserService.class
)
@Transactional
public class UserServiceImpl implements IUserService {
    @Autowired
    private UmsAdminMapper dao;

    @Autowired
    private TokenServiceImpl tokenService;

    @Override
    public UmsAdmin login(UmsAdminLoginParam user) {
        return new UmsAdmin();
    }

    @Override
    public UmsAdmin findUserById(Long userId) {

        return dao.selectByPrimaryKey(userId);

    }
    @Override
    public UmsAdmin findByUsername(String username){
        //必须保证用户名不能重复
        return dao.selectByUsername(username);


    }

    @Override
    public UmsAdmin reg(UmsAdmin user) {
        //icon 需要对http://www.japygo.com/images/first/10.jpg 图片 1-15随机访问
        int i=new Random().nextInt(16);
        user.setIcon("http://www.japygo.com/images/first/"+i+".jpg");
        dao.insert(user);
        return user;
    }

    @Override
    public UmsAdmin findByUmsAdmin(String token) {
        String userid=tokenService.getUserId(token);
        return this.findUserById(Long.parseLong(userid));
    }


}
