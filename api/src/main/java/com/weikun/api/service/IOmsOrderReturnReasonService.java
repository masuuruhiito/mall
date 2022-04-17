package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.model.OmsOrderReturnReason;

import java.util.List;


/**
 * 创建人：SHI
 * 创建时间：2021/11/10
 * 描述你的类：
 */
public interface IOmsOrderReturnReasonService {

    /**
     * 添加订单原因
     */
    OmsOrderReturnReason create(OmsOrderReturnReason returnReason);

    /**
     * 修改退货原因
     */
    OmsOrderReturnReason update(Long id, OmsOrderReturnReason returnReason);

    /**
     * 批量删除退货原因
     */
    int delete(List<Long> ids);

    /**
     * 分页获取退货原因
     */
    CommonPage list(Integer pageSize, Integer pageNum);

    /**
     * 批量修改退货原因状态
     */
    OmsOrderReturnReason updateStatus(Long id, Integer status);

    /**
     * 获取单个退货原因详情信息
     */
    OmsOrderReturnReason getOmsOrderReturnReason(Long id);
}
