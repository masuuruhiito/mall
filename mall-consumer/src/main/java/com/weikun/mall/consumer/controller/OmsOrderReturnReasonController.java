package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.OmsOrderReturnReason;
import com.weikun.api.service.IOmsOrderReturnReasonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：
 */
@RestController
@Api(tags = "OmsOrderReturnReasonController", description = "退货原因管理")
@RequestMapping("/returnReason")
@CrossOrigin
public class    OmsOrderReturnReasonController {
    @Reference(
            version = "1.0.0",interfaceClass = IOmsOrderReturnReasonService.class,
            interfaceName = "com.weikun.api.service.IOmsOrderReturnReasonService",
            timeout =120000
    )
    private IOmsOrderReturnReasonService service;


    @ApiOperation("添加退货原因")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "添加退货原因", type = LogType.ORDER_ADD_RETURN_REASON)
    public CommonResult create(@RequestBody OmsOrderReturnReason returnReason) {

        CommonResult commonResult;
        try{
            service.create(returnReason);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }


    @ApiOperation("分页查询全部退货原因")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "分页查询全部退货原因", type = LogType.ORDER_RETURN_REASON_LIST)
    public CommonResult<CommonPage<OmsOrderReturnReason>> list(@RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        CommonPage commonPage = service.list(pageSize, pageNum);
        return CommonResult.success(commonPage);
    }

    @ApiOperation("批量删除退货原因")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "批量删除退货原因", type = LogType.ORDER_DELETE_RETURN_REASON)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = service.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("修改退货原因")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改退货原因", type = LogType.ORDER_UPDATE_RETURN_REASON)
    public CommonResult update(@PathVariable Long id, @RequestBody OmsOrderReturnReason returnReason) {


        CommonResult commonResult;
        try{
            service.update(id, returnReason);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }


    @ApiOperation("获取单个退货原因详情信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "获取单个退货原因详情信息", type = LogType.ORDER_RETURN_REASON)
    public CommonResult<OmsOrderReturnReason> getItem(@PathVariable Long id) {
        OmsOrderReturnReason reason = service.getOmsOrderReturnReason(id);
        return CommonResult.success(reason);
    }

    @ApiOperation("修改退货原因启用状态")
    @RequestMapping(value = "/update/status", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改退货原因启用状态", type = LogType.ORDER_UPDATE_RETURN_REASON_STATUS)
    public CommonResult updateStatus(@RequestParam(value = "status") Integer status,
                                     @RequestParam("ids") Long id) {
        CommonResult commonResult;
        try{
            service.updateStatus(id, status);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;


    }


}
