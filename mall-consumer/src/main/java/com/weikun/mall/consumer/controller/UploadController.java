package com.weikun.mall.consumer.controller;

import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.CommonResult;
import com.weikun.api.common.ConstVar;
import com.weikun.api.common.UploadFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.weikun.api.common.ConstVar.SHOW_IMAGE_PATH;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 */
@RestController
@CrossOrigin
@Api(tags = "UploadController", description = "公共上传图片管理")
@RequestMapping("/upload")
public class UploadController {
    //上传多个与单个文件通用
    @ApiOperation(value = "添加商品相册，品牌Logo等照片")//brand pics
    @RequestMapping(value = "/pics", method = RequestMethod.POST)
    @ResponseBody

    public CommonResult upload(String name, HttpServletRequest request) {//dicname 存储的目录名字 也就是图片属性
        // 如果你现在是MultipartHttpServletRequest的一个对象
        if(request instanceof MultipartHttpServletRequest) {
            System.out.println(name);
            MultipartHttpServletRequest mrequest = (MultipartHttpServletRequest)request;
            List<MultipartFile> files = mrequest.getFiles("file");
            Iterator<MultipartFile> iter = files.iterator();
            while(iter.hasNext()) {
                MultipartFile photo = iter.next();
                // 现在有文件上传
                if(photo!=null) {

                    try {
                        // 拿到文件名
                       // String filename = photo.getOriginalFilename();
                        String filename= UUID.randomUUID().toString();//UUID 保证唯一

                        // 存放上传图片的文件夹
                        File fileDir = UploadFile.getImgDirFile();
                        // 输出文件夹绝对路径  -- 这里的绝对路径是相当于当前项目的路径而不是“容器”路径
                        System.out.println(fileDir.getAbsolutePath());




                        String suffix = photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf(".") + 1);
                        if((ConstVar.IMG_TYPE_DMG.equals(suffix.toUpperCase()) ||
                                ConstVar.IMG_TYPE_GIF.equals(suffix.toUpperCase()) ||
                                ConstVar.IMG_TYPE_JPEG.equals(suffix.toUpperCase()) ||
                                ConstVar.IMG_TYPE_JPG.equals(suffix.toUpperCase()) ||
                                ConstVar.IMG_TYPE_PNG.equals(suffix.toUpperCase()) ||
                                ConstVar.IMG_TYPE_SVG.equals(suffix.toUpperCase()))) {


                            // 构建真实的文件路径
                            File newFile = new File(fileDir.getAbsolutePath() + File.separator +filename+"."+suffix);
                            System.out.println(newFile.getAbsolutePath());
                            photo.transferTo(newFile);    // 上传图片到 -》 “绝对路径”
                            Map<String, String> result = new HashMap<>(16);
                            result.put("contentType", photo.getContentType());
                            result.put("suffix", suffix);
                            result.put("fileName",filename);
                            result.put("fileSize", photo.getSize() + "");
                            result.put("nav",SHOW_IMAGE_PATH+"/"+filename+"."+suffix);//浏览图片的文件名称
                            return CommonResult.success(result);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                   return  CommonResult.failed("上传失败,类型不匹配！");

                }
            }
        }
        return CommonResult.failed("上传失败！");
    }


}
