package com.weikun.mall.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})//不用DataSource自动配置 去掉)
public class MallConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallConsumerApplication.class, args);
    }

}
