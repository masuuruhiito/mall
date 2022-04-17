package com.weikun.mall.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableCaching //开启缓存注解
public class MallProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProviderApplication.class, args);


    }

}
