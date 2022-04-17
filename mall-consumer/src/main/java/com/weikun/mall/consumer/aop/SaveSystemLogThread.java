package com.weikun.mall.consumer.aop;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：
 */

import com.alibaba.fastjson.JSON;
import com.weikun.api.model.UMSLog;
import com.weikun.api.service.IUMSLogService;


/**
 * 保存日志至数据库
 */
public class SaveSystemLogThread implements Runnable {

    private UMSLog log;

    private IUMSLogService logService;

    public SaveSystemLogThread(UMSLog esLog, IUMSLogService logService) {
        this.log = esLog;
        this.logService = logService;
    }

    @Override
    public void run() {
        System.out.println(JSON.toJSONString(log));
        logService.insert(log);
    }
}
