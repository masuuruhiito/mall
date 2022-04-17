package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.OmsMoneyInfoParam;
import com.weikun.api.dto.OmsOrderDeliveryParam;
import com.weikun.api.dto.OmsOrderQueryParam;
import com.weikun.api.dto.OmsReceiverInfoParam;
import com.weikun.api.model.OmsOrder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/10
 * 描述你的类：
 */
public interface IOmsOrderService {

    /**
     * 批量删除订单
     */
    int delete(Long id);

    /**
     * 获取指定订单详情
     */
    OmsOrder detail(Long id);

    /**
     * 修改订单备注
     */

    OmsOrder updateNote(Long id, String note, Integer status);


    /**
     * 修改订单收货人信息
     */

    OmsOrder updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam);


    /**
     * 修改订单费用信息
     */

    OmsOrder updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam);


    /**
     * 批量关闭订单
     */

    OmsOrder close(Long id, String note);


    public CommonPage  list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) ;

    /**
     * 批量发货
     */

    OmsOrder delivery(OmsOrderDeliveryParam deliveryParam);



    /**
     * 发送延迟消息取消订单
     */

    //根据id 取消订单
    public void sendDelayMessageCancelOrder(Long orderId,int minute);


    public OmsOrder cancelOrder(Long orderId) ;

}
