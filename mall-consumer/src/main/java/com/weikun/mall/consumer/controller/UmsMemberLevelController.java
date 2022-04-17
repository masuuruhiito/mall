package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.UmsMemberLevel;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IUmsMemberLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@Controller
@Api(tags = "UmsMemberLevelController", description = "会员等级管理")
@RequestMapping("/memberLevel")
@CrossOrigin
public class UmsMemberLevelController {
    @Reference(
            version = "1.0.0",interfaceClass = IUmsMemberLevelService.class,
            interfaceName = "com.weikun.api.service.IUmsMemberLevelService",
            timeout =120000
    )
    private IUmsMemberLevelService memberLevelService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation("查询所有会员等级")
    @ResponseBody
    @UserLoginToken
   //@SystemLog(description = "查询所有会员等级", type = LogType.UMS_MEMBER_LEVEL_LIST)
    public CommonResult<List<UmsMemberLevel>> list(@RequestParam("defaultStatus") Integer defaultStatus) {
        List<UmsMemberLevel> memberLevelList = memberLevelService.list(defaultStatus);
        return CommonResult.success(memberLevelList);
    }
}
