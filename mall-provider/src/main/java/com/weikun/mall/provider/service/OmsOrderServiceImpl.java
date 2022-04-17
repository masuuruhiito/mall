package com.weikun.mall.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.OmsMoneyInfoParam;
import com.weikun.api.dto.OmsOrderDeliveryParam;
import com.weikun.api.dto.OmsOrderQueryParam;
import com.weikun.api.dto.OmsReceiverInfoParam;
import com.weikun.api.model.*;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IOmsOrderService;
import com.weikun.mall.provider.component.CancelOrderSender;
import com.weikun.mall.provider.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 创建人：SHI
 * 创建时间：2021/12/13
 * 描述你的类：
 */
@Service(
        version = "1.0.0",
        interfaceName = "com.weikun.api.service.IOmsOrderService",
        interfaceClass = IOmsOrderService.class
)
@Transactional
public class OmsOrderServiceImpl implements IOmsOrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;

    @Autowired
    private OmsOrderSettingMapper orderSettingMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private CancelOrderSender cancelOrderSender;


    @Autowired
    private PmsSkuStockMapper pmsSkuStockMapper;


    @CacheEvict(cacheNames = {"Order"},
            key = "#id", allEntries = true, beforeInvocation = false)
    @Override
    public int delete(Long id) {
        clearOrder();
        OmsOrder record = new OmsOrder();
        record.setDeleteStatus(1);
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdEqualTo(id);
        return orderMapper.updateByExampleSelective(record, example);
    }

    @Cacheable(cacheNames = {"Order"}, unless = "#result == null", key = "#id")
    @Override
    public OmsOrder detail(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    @CachePut(cacheNames = {"Order"}, key = "#id")
    @Override
    public OmsOrder updateNote(Long id, String note, Integer status) {
        clearOrder();
        OmsOrder order = this.detail(id);
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        //增加订单历史
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(status);
        history.setNote("修改备注信息：" + note);
        orderOperateHistoryMapper.insert(history);
        return order;
    }

    @CachePut(cacheNames = {"Order"}, key = "#result.id")
    @Override
    public OmsOrder updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        clearOrder();
        OmsOrder order = this.detail(receiverInfoParam.getOrderId());
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);

        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(receiverInfoParam.getStatus());
        history.setNote("修改收货人信息");
        orderOperateHistoryMapper.insert(history);
        return order;
    }

    @CachePut(cacheNames = {"Order"}, key = "#result.id")
    @Override
    public OmsOrder updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        clearOrder();
        OmsOrder order = this.detail(moneyInfoParam.getOrderId());
        order.setId(moneyInfoParam.getOrderId());
        order.setFreightAmount(moneyInfoParam.getFreightAmount());
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKey(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(moneyInfoParam.getStatus());
        history.setNote("修改费用信息");
        orderOperateHistoryMapper.insert(history);
        return order;
    }
    @CachePut(cacheNames = {"Order"}, key = "#id")
    @Override
    public OmsOrder close(Long id, String note) {
        clearOrder();
        OmsOrder record = this.detail(id);
        record.setStatus(4);
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdEqualTo(id);
        int count = orderMapper.updateByPrimaryKeySelective(record);

        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(4);
        history.setNote("订单关闭:" + note);


        orderOperateHistoryMapper.insert(history);
        return record;
    }

    private void clearOrder() {
        cacheManager.getCache("OrderList").clear();//删除::之前叫BrandList的所有集合

    }

    @Cacheable(cacheNames= {"OrderList"},unless="#result == null",
            key="T(String).valueOf(#pageNum+'-'+#pageSize)" +
                    ".concat(#queryParam.orderSn!=null?#queryParam.orderSn:'os')"+//得有字母占位，否则 多条件查询 条件不一样 会出现雷同的结果
                    ".concat(#queryParam.receiverKeyword!=null?#queryParam.receiverKeyword:'r') "+
                    ".concat(#queryParam.status!=null?#queryParam.status:'s') "+
                    ".concat(#queryParam.orderType!=null ?#queryParam.orderType:'ot') "+
                    ".concat(#queryParam.sourceType!=null ?#queryParam.sourceType:'st')"+
                    ".concat(#queryParam.createTime!=null?#queryParam.createTime:'ct')"
    ) //当结果为空时不缓存
    @Override
    public CommonPage list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        List<OmsOrder> list=orderMapper.getOrderList(queryParam);
        return CommonPage.restPage(list);

    }
    @CachePut(cacheNames = {"Order"}, key = "#result.id")
    @Override
    public OmsOrder delivery(OmsOrderDeliveryParam deliveryParam) {//更改物流公司
        clearOrder();
        //批量发货
        OmsOrder omsOrder=this.detail(deliveryParam.getOrderId());
        omsOrder.setDeliverySn(deliveryParam.getDeliverySn());
        omsOrder.setDeliveryCompany(deliveryParam.getDeliveryCompany());
        omsOrder.setDeliveryTime(new Date());
        omsOrder.setStatus(2);
        orderMapper.updateByPrimaryKeySelective(omsOrder);
        //添加操作记录

        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(deliveryParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(2);
        history.setNote("完成发货");

        orderOperateHistoryMapper.insert(history);
        return omsOrder;
    }

    @Override
    public void sendDelayMessageCancelOrder(Long orderId,int minute) {
        //获取所有订单的超时时间
        //OmsOrderSetting orderSetting = orderSettingMapper.selectByPrimaryKey(1L);
        //long delayTimes = orderSetting.getNormalOrderOvertime() * 60 * 1000;
        //此处是改了一下 用的是每笔订单 均有自己的超时时间 因为 我们仅是做个消息的功能罢了，没有移动端APP，慢慢就有了
        //发送延迟消息
        long delayTimes=minute*60*1000;
        cancelOrderSender.sendMessage(orderId, delayTimes);
    }
    @CachePut(cacheNames = {"Order"}, key = "#orderId")
    @Override
    public OmsOrder cancelOrder(Long orderId) {
        clearOrder();
        OmsOrder cancelOrder =this.detail(orderId);

        if (cancelOrder != null) {
            //修改订单状态为取消
            cancelOrder.setStatus(4);
            orderMapper.updateByPrimaryKeySelective(cancelOrder);

            OmsOrderItemExample orderItemExample = new OmsOrderItemExample();
            orderItemExample.createCriteria().andOrderIdEqualTo(orderId);
            List<OmsOrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
            //解除订单商品库存锁定
            /**
             * 解除取消订单的库存锁定
             */

            List<Long> productIdList=orderItemList.stream().map(c->c.getProductId()).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(orderItemList)) {
                PmsSkuStock pmsSkuStock=new PmsSkuStock();
                PmsSkuStockExample example1=new PmsSkuStockExample();
                example1.createCriteria().andProductIdIn(productIdList);

                pmsSkuStock.setLockStock(0);//把数量释放
                pmsSkuStockMapper.updateByExampleSelective(pmsSkuStock,example1);
            }


        }
        return cancelOrder;
    }


}
