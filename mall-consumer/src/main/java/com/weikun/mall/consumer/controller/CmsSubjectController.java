package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.CmsSubject;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.ICmsSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：
 */
@Controller
@Api(tags = "CmsSubjectController", description = "商品专题管理")
@RequestMapping("/subject")
@CrossOrigin
public class CmsSubjectController {


    @Reference(
            version = "1.0.0",interfaceClass = ICmsSubjectService.class,
            interfaceName = "com.weikun.api.service.ICmsSubjectService",
            timeout =120000
    )
    private ICmsSubjectService subjectService;

    @ApiOperation("获取全部商品专题")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "获取全部商品专题", type = LogType.COMPANY_ADDRESS)
    public CommonResult<List<CmsSubject>> listAll() {
        List<CmsSubject> subjectList = subjectService.listAll();
        return CommonResult.success(subjectList);
    }

}
