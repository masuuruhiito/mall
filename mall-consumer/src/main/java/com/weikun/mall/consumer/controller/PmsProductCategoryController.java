package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.PmsProductCategory;
import com.weikun.api.service.IProductAttributeService;
import com.weikun.api.service.IProductCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@RestController
@CrossOrigin
@Api(tags = "PmsProductCategoryController", description = "商品分类管理")
@RequestMapping("/productCategory")
public class PmsProductCategoryController {


    @Reference(
            version = "1.0.0",interfaceClass = IProductCategoryService.class,
            interfaceName = "com.weikun.api.service.IProductCategoryService",
            timeout =120000
    )
    private IProductCategoryService productCategoryService;

    @ApiOperation("分页查询商品分类")
    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "分页查询商品分类", type = LogType.PRODUCT_CATEGORY_LIST)
    public CommonResult<CommonPage<PmsProductCategory>> getList(@PathVariable Long parentId,
                                                                @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {

        return CommonResult.success(productCategoryService.getList(parentId, pageSize, pageNum));
    }

    @ApiOperation("添加产品分类")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "添加产品分类", type = LogType.PRODUCT_CATEGORY_ADD)
    public CommonResult create(@Validated @RequestBody PmsProductCategory productCategory) {

        CommonResult commonResult;
        try{
            productCategoryService.create(productCategory);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }


    @ApiOperation("删除商品分类")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "删除商品分类", type = LogType.PRODUCT_CATEGORY_DELETE)
    public CommonResult delete(@PathVariable Long id) {

        CommonResult commonResult;
        try{
            productCategoryService.delete(id);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }
    @ApiOperation("根据id获取商品分类")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "根据id获取商品分类", type = LogType.PRODUCT_CATEGORY_ID)
    public CommonResult<PmsProductCategory> getItem(@PathVariable Long id) {
        PmsProductCategory productCategory = productCategoryService.getItem(id);
        return CommonResult.success(productCategory);
    }

    @ApiOperation("修改商品分类")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改商品分类", type = LogType.PRODUCT_CATEGORY_UPDATE)
    public CommonResult update(@PathVariable Long id,
                               @Validated
                               @RequestBody PmsProductCategory productCategory) {


        CommonResult commonResult;
        try{
            productCategoryService.update(id, productCategory);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }


    @ApiOperation("修改导航栏显示状态")
    @RequestMapping(value = "/update/navStatus", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "修改导航栏显示状态", type = LogType.PRODUCT_NAV_STATUS)
    public CommonResult updateNavStatus(@RequestParam("ids") List<Long> ids,
                                        @RequestParam("navStatus") Integer navStatus) {

        CommonResult commonResult;
        try{
            ids.forEach(c->productCategoryService.updateNavStatus(c, navStatus));//循环修改 因为批量修改不能做缓存
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }

    @ApiOperation("修改显示状态")
    @RequestMapping(value = "/update/showStatus", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "修改显示状态", type = LogType.PRODUCT_SHOW_STATUS)
    public CommonResult updateShowStatus(@RequestParam("ids") List<Long> ids,
                                         @RequestParam("showStatus") Integer showStatus) {
        CommonResult commonResult;
        try{
            ids.forEach(c->productCategoryService.updateShowStatus(c, showStatus));//循环修改 因为批量修改不能做缓存
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;

    }

    //添加商品菜单用
    @ApiOperation("查询所有一级分类及子分类")
    @RequestMapping(value = "/list/withChildren", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "查询所有一级分类及子分类", type = LogType.PRODUCT_WITH_CHILDREN)
    public CommonResult<List<PmsProductCategory>> listWithChildren() {
        List<PmsProductCategory> list = productCategoryService.listWithChildren();
        return CommonResult.success(list);
    }
}
