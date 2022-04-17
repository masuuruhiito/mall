package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.OmsCompanyAddress;
import com.weikun.api.service.IOmsCompanyAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 收货地址管理Controller
 * 创建人：SHI
 * 创建时间：2021/12/2
 */
@Controller
@Api(tags = "OmsCompanyAddressController", description = "收货地址管理")
@RequestMapping("/companyAddress")
@CrossOrigin
public class    OmsCompanyAddressController {

    @Reference(
            version = "1.0.0",interfaceClass = IOmsCompanyAddressService.class,
            interfaceName = "com.weikun.api.service.IOmsCompanyAddressService",
            timeout =120000
    )
    private IOmsCompanyAddressService companyAddressService;

    @ApiOperation("获取所有收货地址")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "获取所有收货地址", type = LogType.SUBJECT_LIST)
    public CommonResult<List<OmsCompanyAddress>> list() {
        List<OmsCompanyAddress> companyAddressList = companyAddressService.list();
        return CommonResult.success(companyAddressList);
    }
}
