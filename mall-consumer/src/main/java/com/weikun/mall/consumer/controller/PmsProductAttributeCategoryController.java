package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.dto.PmsProductAttributeCategoryItem;
import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.model.PmsProductAttributeCategory;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.IProductAttributeCategoryService;
import com.weikun.api.service.IProductAttributeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@RestController
@CrossOrigin
@Api(tags = "PmsProductAttributeCategoryController", description = "商品属性分类管理")
@RequestMapping("/productAttribute/category")
public class PmsProductAttributeCategoryController {
    @Reference(
            version = "1.0.0",interfaceClass = IProductAttributeCategoryService.class,
            interfaceName = "com.weikun.api.service.IProductAttributeCategoryService",
            timeout =120000
    )
    private IProductAttributeCategoryService service;

    @ApiOperation("分页获取所有商品属性分类")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "分页获取所有商品属性分类", type = LogType.PRODUCT_ATTRIBUTE)
    public CommonResult<CommonPage<PmsProductAttributeCategory>> getList(@RequestParam(defaultValue = "5") Integer pageSize, @RequestParam(defaultValue = "1") Integer pageNum) {

        return CommonResult.success(service.getList(pageSize, pageNum));
    }


    @ApiOperation("添加商品属性分类")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "添加商品属性分类", type = LogType.PRODUCT_ADD_ATTRIBUTE)
    public CommonResult create(@RequestParam String name) {
        PmsProductAttributeCategory productAttributeCategory =new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);

        CommonResult commonResult;
        try{
            service.create(productAttributeCategory);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }

    @ApiOperation("删除单个商品属性分类")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "删除单个商品属性分类", type = LogType.PRODUCT_DELETE_ATTRIBUTE)
    public CommonResult delete(@PathVariable Long id) {
        int count = service.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }
    @ApiOperation("修改商品属性分类")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改商品属性分类", type = LogType.PRODUCT_UPDATE_ATTRIBUTE)
    public CommonResult update(@PathVariable Long id, @RequestParam String name) {
        CommonResult commonResult;
        try{
            service.update(id, name);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }
    @ApiOperation("获取单个商品属性分类信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "获取单个商品属性分类信息", type = LogType.PRODUCT_GET_ATTRIBUTE_CATEGORY)
    public CommonResult<PmsProductAttributeCategory> getItem(@PathVariable Long id) {
        PmsProductAttributeCategory productAttributeCategory = service.getItem(id);
        return CommonResult.success(productAttributeCategory);
    }


    @ApiOperation("获取所有商品属性分类及其下属性")
    @RequestMapping(value = "/list/withAttr", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "获取所有商品属性分类及其下属性", type = LogType.PRODUCT_ATTRIBUTE_CATEGORY)
    public CommonResult<List<PmsProductAttributeCategory>> getListWithAttr() {

        List<PmsProductAttributeCategory> productAttributeCategoryResultList = service.getListWithAttr();
        return CommonResult.success(productAttributeCategoryResultList);
    }
}
