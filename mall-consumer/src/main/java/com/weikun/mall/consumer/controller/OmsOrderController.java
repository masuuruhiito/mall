package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.dto.OmsMoneyInfoParam;
import com.weikun.api.dto.OmsOrderDeliveryParam;
import com.weikun.api.dto.OmsOrderQueryParam;
import com.weikun.api.dto.OmsReceiverInfoParam;
import com.weikun.api.model.OmsOrder;
import com.weikun.api.service.IOmsOrderService;
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
@Controller
@Api(tags = "OmsOrderController", description = "订单管理")
@RequestMapping("/order")
@CrossOrigin
public class OmsOrderController {
    @Reference(
            version = "1.0.0",interfaceClass = IOmsOrderService.class,
            interfaceName = "com.weikun.api.service.IOmsOrderService",
            timeout =120000
    )
    private IOmsOrderService orderService;

    @ApiOperation("获取订单详情:订单信息、商品信息、操作记录")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "获取订单详情:订单信息、商品信息、操作记录", type = LogType.ORDER_DETAIL)
    public CommonResult<OmsOrder> detail(@PathVariable Long id) {
        OmsOrder orderDetailResult = orderService.detail(id);
        return CommonResult.success(orderDetailResult);
    }

    @ApiOperation("备注订单")
    @RequestMapping(value = "/update/note", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "修改备注订单", type = LogType.ORDER_NOTE_UPDATE)
    public CommonResult updateNote(@RequestParam("id") Long id,
                                   @RequestParam("note") String note,
                                   @RequestParam("status") Integer status) {


        CommonResult commonResult;
        try{
            orderService.updateNote(id, note, status);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }

    @ApiOperation("批量删除订单")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "批量删除订单", type = LogType.ORDER_DELETE)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {

        CommonResult commonResult;
        try{
            ids.forEach(c-> orderService.delete(c));
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }


    @ApiOperation("修改收货人信息")
    @RequestMapping(value = "/update/receiverInfo", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "修改收货人信息", type = LogType.ORDER_RECEIVER_UPDATE)
    public CommonResult updateReceiverInfo(@RequestBody OmsReceiverInfoParam receiverInfoParam) {
        CommonResult commonResult;
        try{
            orderService. updateReceiverInfo(receiverInfoParam);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }

    @ApiOperation("修改订单费用信息")
    @RequestMapping(value = "/update/moneyInfo", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "修改订单费用信息", type = LogType.ORDER_MONEY_UPDATE)
    public CommonResult updateReceiverInfo(@RequestBody OmsMoneyInfoParam moneyInfoParam) {
        CommonResult commonResult;
        try{
            orderService.updateMoneyInfo(moneyInfoParam);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }

    @ApiOperation("批量关闭订单")
    @RequestMapping(value = "/update/close", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "批量关闭订单", type = LogType.ORDER_CLOE_UPDATE)
    public CommonResult close(@RequestParam("ids") List<Long> ids, @RequestParam String note) {
        CommonResult commonResult;
        try{
            ids.stream().forEach(orderid->{
                orderService.close(orderid, note);
            });
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }

        return commonResult;

    }


    @ApiOperation("查询订单")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "查询订单", type = LogType.ORDER_LIST)
    public CommonResult<CommonPage<OmsOrder>> list(OmsOrderQueryParam queryParam,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {

        return CommonResult.success(orderService.list(queryParam, pageSize, pageNum));
    }


    @ApiOperation("批量发货")
    @RequestMapping(value = "/update/delivery", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "批量发货", type = LogType.ORDER_DELIVERY)
    public CommonResult delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParamList) {

        CommonResult commonResult;
        try{
            deliveryParamList.stream().forEach(deliveryParam->{
                orderService.delivery(deliveryParam);
            });
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }

        return commonResult;

    }


    @ApiOperation("取消单个超时订单")
    @RequestMapping(value = "/cancelOrder",method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "取消单个超时订单", type = LogType.ORDER_CANCEL)
    public CommonResult cancelOrder(@RequestParam("ids") List<Long> ids,@RequestParam("minute") int minute){
        //演示之前 先把OmsOrder中的 status改为0，这样就可以演示自动取消订单了
        orderService.sendDelayMessageCancelOrder(ids.get(0),minute);
        return CommonResult.success(null);
    }
}
