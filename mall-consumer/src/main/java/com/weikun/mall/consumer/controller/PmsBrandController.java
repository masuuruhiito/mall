package com.weikun.mall.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonPage;
import com.weikun.api.common.CommonResult;
import com.weikun.api.model.PmsBrand;
import com.weikun.api.service.IBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "BrandController", description = "商品品牌管理")
@RequestMapping("/brand")
public class PmsBrandController {
    @Reference(
            version = "1.0.0",interfaceClass = IBrandService.class,
            interfaceName = "com.weikun.api.service.IBrandService",
            timeout =120000
    )
    private IBrandService service;



    @ApiOperation(value = "根据品牌名称分页获取品牌列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @SystemLog(description = "品牌列表", type = LogType.BRAND_LIST)
    @ResponseBody
    @UserLoginToken
    public CommonResult<CommonPage<PmsBrand>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        CommonPage c= service.listBrand(keyword, pageNum, pageSize);

        return CommonResult.success(c);
    }


    @ApiOperation(value = "批量更新显示状态")
    @RequestMapping(value = "/update/showStatus", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "修改品牌", type = LogType.BRAND_UPDATE)
    public CommonResult updateShowStatus(@RequestParam("ids") List<Long> ids,
                                         @RequestParam("showStatus") Integer showStatus) {


        CommonResult commonResult;
        try{
            ids.forEach(c->service.updateShowStatus(c,showStatus));
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;


    }
    @ApiOperation(value = "添加品牌")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "增加品牌", type = LogType.BRAND_ADD)
    public CommonResult create( @RequestBody PmsBrand pmsBrand) {
        CommonResult commonResult;
        try{
            service.create(pmsBrand);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }


    @ApiOperation(value = "批量更新厂家制造商状态")
    @RequestMapping(value = "/update/factoryStatus", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "批量更新厂家制造商状态", type = LogType.BRAND_UPDATE_FACTORY_STATUS)
    public CommonResult updateFactoryStatus(@RequestParam("ids") List<Long> ids,
                                            @RequestParam("factoryStatus") Integer factoryStatus) {
        CommonResult commonResult;
        try{
            ids.forEach(c->service.updateFactoryStatus(c,factoryStatus));//循环修改 因为批量修改不能做缓存
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;




    }
    @ApiOperation(value = "删除品牌")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "删除品牌", type = LogType.USER_DELETE)
    public CommonResult delete(@PathVariable("id") Long id) {
        int count = service.deleteBrand(id);
        if (count == 1) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "根据编号查询品牌信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @UserLoginToken
   // @SystemLog(description = "根据编号查询品牌信息", type = LogType.BRAND_LIST)
    public CommonResult<PmsBrand> getBrand(@PathVariable("id") Long id) {
        return CommonResult.success(service.getBrand(id));
    }


    @ApiOperation(value = "更新品牌")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "更新品牌", type = LogType.BRAND_UPDATE)
    public CommonResult update(@PathVariable("id") Long id,
                               @Validated @RequestBody PmsBrand pmsBrand) {
        CommonResult commonResult;
        try{
            service.updateBrand(id, pmsBrand);
            commonResult = CommonResult.success(1);//修改成功一条记录
        }catch(Exception e){
            commonResult = CommonResult.failed();
            e.printStackTrace();
        }
        return commonResult;
    }

}
