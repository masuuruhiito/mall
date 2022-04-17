package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.PmsBrand;
import com.weikun.api.model.UmsUserView;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IUserViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：取出UV统计数据
 */
@RestController
@CrossOrigin
@Api(tags = "UVController", description = "UserView统计数据管理")
@RequestMapping("/uv")
public class UmsUVController {


    @Reference(
            version = "1.0.0",interfaceClass = IUserViewService.class,
            interfaceName = "com.weikun.api.service.IUserViewService",
            timeout =120000
    )
    private IUserViewService service;


    @ApiOperation(value = "UserView统计数据管理")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @SystemLog(description = "UserView统计数据管理", type = LogType.UMS_USER_VIEW_LIST)
    @ResponseBody
    @UserLoginToken
    public CommonResult<CommonPage<UmsUserView>> getUVList(

            @RequestParam(value = "start", required = true) String start,//开始日期
            @RequestParam(value = "end", required = true) String end,//结束日期
            @RequestParam(value = "type", required = true) String type//查询的TypeID 操作种类的id号
    ){

        CommonPage c= service.listUV(start,end,type);

        return CommonResult.success(c);
    }

    @ApiOperation(value = "UserView统计类型管理")
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    //@SystemLog(description = "UserView统计类型管理", type = LogType.UMS_USER_VIEW_TYPE_LIST)
    @ResponseBody
    @UserLoginToken
    public CommonResult<CommonPage<UmsUserView>> getUVType() throws Exception {
        CommonPage c= service.listTypeUV();

        return CommonResult.success(c);
    }
}
