package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.common.CommonPage;

import com.weikun.api.model.UmsLogType;
import com.weikun.api.model.UmsLogTypeExample;
import com.weikun.api.model.UmsUserView;
import com.weikun.api.service.IUserViewService;
import com.weikun.mall.provider.mapper.PmsBrandMapper;
import com.weikun.mall.provider.mapper.UmsLogTypeMapper;
import com.weikun.mall.provider.mapper.redis.mapper.RedisUtilMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */

@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IUserViewService",
        interfaceClass = IUserViewService.class
)

public class UserViewServiceImpl implements IUserViewService {
    @Autowired
    private UmsLogTypeMapper typeMapper;//

    @Autowired
    private RedisUtilMapper rMapper;//


//    @Bean
//    @ConditionalOnMissingBean
//    public StringRedisTemplate stringRedisTemplate(
//        RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
//        StringRedisTemplate template = new StringRedisTemplate();
//        template.setConnectionFactory(redisConnectionFactory);
//        return template;
//
//    }


    @Override
    public CommonPage listUV(String start,String end,String type) {

//        uvList.add(new UmsUserView("2020-02-07",10,5293));
//        uvList.add(new UmsUserView("2020-02-08",20,2293));
//        uvList.add(new UmsUserView("2020-02-09",30,8293));
//        uvList.add(new UmsUserView("2020-02-10",40,10293));
//        uvList.add(new UmsUserView("2020-02-11",50,1293));
//        uvList.add(new UmsUserView("2020-02-12",60,4293));
//        uvList.add(new UmsUserView("2020-02-13",70,4293));
//        uvList.add(new UmsUserView("2020-02-14",80,14293));
//        uvList.add(new UmsUserView("2020-02-15",90,22100));
//        uvList.add(new UmsUserView("2020-02-16",60,3400));
//        uvList.add(new UmsUserView("2020-02-17",30,4100));
//        uvList.add(new UmsUserView("2020-02-18",10,4891));

        List uvList=forDate(start,end,type);

        return CommonPage.restPage(uvList);
    }


    public List <UmsUserView> forDate(String start,String end,String type) {//知道开始和结束 循环日期
        // 日期格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<UmsUserView> uvList=new ArrayList();
        try {
            // 起始日期
            Date d1 = sdf.parse(start);
            // 结束日期
            Date d2 = sdf.parse(end);
            Date tmp = d1;
            Calendar dd = Calendar.getInstance();
            dd.setTime(d1);
            // 打印2018年2月25日到2018年3月5日的日期
            while (tmp.getTime() <=d2.getTime()) {
                tmp = dd.getTime();
                System.out.println(sdf.format(tmp));
                // 天数加上1
                Set<Object> s=rMapper.getAllKeys(sdf.format(tmp)+"_"+type);

                s.forEach(c->{
                    int count=Integer.parseInt(rMapper.get(c.toString()).toString());
                    uvList.add(new UmsUserView(c.toString().substring(0,10) ,count));//取出查询的日期
                });

                dd.add(Calendar.DAY_OF_MONTH, 1);
            }
            System.out.println(uvList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return uvList;
    }
    @Override
    public CommonPage listTypeUV() throws Exception {
        UmsLogTypeExample example=new UmsLogTypeExample();
        example.createCriteria().andFlagEqualTo(1);//只要flag为1的类型
        List<UmsLogType> list=typeMapper.selectByExample(example);


//        ConcurrentHashMap
        return CommonPage.restPage(list);
    }

}
