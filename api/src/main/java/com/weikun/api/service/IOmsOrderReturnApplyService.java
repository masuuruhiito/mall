package com.weikun.api.service;

import com.weikun.api.common.CommonPage;
import com.weikun.api.dto.OmsReturnApplyQueryParam;
import com.weikun.api.dto.OmsUpdateStatusParam;
import com.weikun.api.model.OmsOrderReturnApply;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：
 */
public interface IOmsOrderReturnApplyService {
    /**
     * 分页查询申请
     */
    CommonPage list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum);

    /**
     * 批量删除申请
     */
    int delete(List<Long> ids);
//
//    /**
//     * 修改申请状态
//     */
    OmsOrderReturnApply updateStatus(Long id, OmsUpdateStatusParam statusParam);

    /**
     * 获取指定申请详情
     */
    OmsOrderReturnApply getItem(Long id);
}
