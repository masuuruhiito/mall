package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.dto.ProductAttrInfo;
import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.service.IProductAttributeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@Controller
@CrossOrigin
@Api(tags = "PmsProductAttributeController", description = "商品属性管理")
@RequestMapping("/productAttribute")
public class PmsProductAttributeController {

    @Reference(
            version = "1.0.0",interfaceClass = IProductAttributeService.class,
            interfaceName = "com.weikun.api.service.IProductAttributeService",
            timeout =120000
    )
    private IProductAttributeService productAttributeService;

    @ApiOperation("根据分类查询属性列表或参数列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "type", value = "0表示属性，1表示参数", required = true, paramType = "query", dataType = "integer")})
    @RequestMapping(value = "/list/{cid}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
 //   @SystemLog(description = "根据分类查询属性列表或参数列表", type = LogType.PRODUCT_ATTRIBUTE_LIST)
    public CommonResult<CommonPage<PmsProductAttribute>> getList(@PathVariable Long cid,
                                                                 @RequestParam(value = "type") Integer type,
                                                                 @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {

        return CommonResult.success( productAttributeService.getList(cid, type, pageSize, pageNum));
    }

    @ApiOperation("添加商品属性信息")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "添加商品属性信息", type = LogType.PRODUCT_ATTRIBUTE_ADD)
    public CommonResult create(@RequestBody PmsProductAttribute productAttribute) {

        CommonResult commonResult;
        try{
            productAttributeService.create(productAttribute);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }


    @ApiOperation("修改商品属性信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改商品属性信息", type = LogType.PRODUCT_UPDATE_ATTRIBUTE)
    public CommonResult update(@PathVariable Long id,
                               @RequestBody PmsProductAttribute productAttribute) {
        CommonResult commonResult;
        try{
            productAttributeService.update(productAttribute);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;


    }

    @ApiOperation("查询单个商品属性")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    //@SystemLog(description = "查询单个商品属性", type = LogType.PRODUCT_ATTRIBUTE_DETAILS)
    public CommonResult<PmsProductAttribute> getItem(@PathVariable Long id) {
        PmsProductAttribute productAttribute = productAttributeService.getItem(id);
        return CommonResult.success(productAttribute);
    }

    @ApiOperation("批量删除商品属性")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "批量删除商品属性", type = LogType.PRODUCT_DELETE_ATTRIBUTE)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = productAttributeService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }
    //前端刷选属性的下拉框
    @ApiOperation("根据商品分类的id获取商品属性及属性分类")
    @RequestMapping(value = "/attrInfo/{productCategoryId}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "根据商品分类的id获取商品属性及属性分类", type = LogType.PRODUCT_CATEGORY_ID)
    public CommonResult<List<ProductAttrInfo>> getAttrInfo(@PathVariable Long productCategoryId) {
        List<ProductAttrInfo> productAttrInfoList = productAttributeService.getProductAttrInfo(productCategoryId);

        return CommonResult.success(productAttrInfoList);
    }
}
