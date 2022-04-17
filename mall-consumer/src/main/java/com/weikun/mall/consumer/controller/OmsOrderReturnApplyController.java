package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.dto.OmsReturnApplyQueryParam;
import com.weikun.api.dto.OmsUpdateStatusParam;
import com.weikun.api.model.OmsOrderReturnApply;
import com.weikun.api.service.IOmsOrderReturnApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单退货申请管理
 * 创建人：SHI
 * 创建时间：2021/12/5
 */
@RestController
@Api(tags = "OmsOrderReturnApplyController", description = "订单退货申请管理")
@RequestMapping("/returnApply")
@CrossOrigin
public class OmsOrderReturnApplyController {

    @Reference(
            version = "1.0.0",interfaceClass = IOmsOrderReturnApplyService.class,
            interfaceName = "com.weikun.api.service.IOmsOrderReturnApplyService",
            timeout =120000
    )
    private IOmsOrderReturnApplyService returnApplyService;

    @ApiOperation("分页查询退货申请")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "分页查询退货申请", type = LogType.ORDER_RETURN_APPLY)
    public CommonResult<CommonPage<OmsOrderReturnApply>> list(OmsReturnApplyQueryParam queryParam,
                                                              @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                              @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {

        return CommonResult.success(returnApplyService.list(queryParam, pageSize, pageNum));
    }
//
    @ApiOperation("批量删除申请")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "删除退货申请", type = LogType.ORDER_DELETE_RETURN)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = returnApplyService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
//
    @ApiOperation("获取退货申请详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "获取退货申请详情", type = LogType.ORDER_RETURN_DETAIL)
    public CommonResult getItem(@PathVariable Long id) {
        OmsOrderReturnApply result = returnApplyService.getItem(id);
        return CommonResult.success(result);
    }
//
    @ApiOperation("修改申请状态")
    @RequestMapping(value = "/update/status/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改申请状态", type = LogType.ORDER_UPDATE_RETURN_STATUS)
    public CommonResult updateStatus(@PathVariable Long id, @RequestBody OmsUpdateStatusParam statusParam) {

        CommonResult commonResult;
        try{
            returnApplyService.updateStatus(id, statusParam);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }

}
