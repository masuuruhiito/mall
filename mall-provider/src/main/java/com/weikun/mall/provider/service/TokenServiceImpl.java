package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.weikun.api.model.UmsAdmin;
import com.weikun.api.service.ITokenService;

import java.util.Date;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：token的生成
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.ITokenService",
        interfaceClass = ITokenService.class
)
public class TokenServiceImpl implements ITokenService {


    /**
     * 过期时间5分钟
     */
    private static final long EXPIRE_TIME = 5 * 600 * 10000;

    /**
     * 生成签名，五分钟后过期
     * @param
     * @return
     */
    @Override
    public  String getToken(String userId, String password) {//生成token
        String token="";
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);

            token= JWT.create().
                    withAudience(userId) .
                    withExpiresAt(date)
                .sign(Algorithm.HMAC256(password));

        } catch (Exception e) {
            return null;
        }
        return token;
    }
    /**
     * 根据token获取userId
     * @param token
     * @return
     */
    @Override
    public  String getUserId(String token) {
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            return userId;
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    //    /**
//     * 校验token
//     * @param token
//     * @return
//     */
    @Override
    public  boolean checkSign(String token, String password) {
        if(token==null){
            throw new RuntimeException("无token，请重新登录！");
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(password)).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("token 无效，请重新获取");

        }
        return true;
    }
}
