package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.OmsOrderSetting;
import com.weikun.api.service.IOmsOrderSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@RestController
@Api(tags = "OmsOrderSettingController", description = "订单设置管理")
@RequestMapping("/orderSetting")
@CrossOrigin
public class OmsOrderSettingController {

    @Reference(
            version = "1.0.0",interfaceClass = IOmsOrderSettingService.class,
            interfaceName = "com.weikun.api.service.IOmsOrderSettingService",
            timeout =120000
    )
    private IOmsOrderSettingService orderSettingService;

    @ApiOperation("获取指定订单设置")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "获取指定订单设置", type = LogType.ORDER_DETAIL_SETTING)
    public CommonResult<OmsOrderSetting> getItem(@PathVariable Long id) {
        OmsOrderSetting orderSetting = orderSettingService.getItem(id);
        return CommonResult.success(orderSetting);
    }

    @ApiOperation("修改指定订单设置")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "获取指定订单设置", type = LogType.ORDER_UPDATE_DETAIL_SETTING)
    public CommonResult update(@PathVariable Long id, @RequestBody OmsOrderSetting orderSetting) {
        CommonResult commonResult;
        try{
            orderSettingService.update(id,orderSetting);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }

}
