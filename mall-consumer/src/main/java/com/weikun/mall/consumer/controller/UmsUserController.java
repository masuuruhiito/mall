package com.weikun.mall.consumer.controller;




import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.face.AipFace;
import com.weikun.api.annotation.LogType;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.common.AIFactoryUtil;
import com.weikun.api.common.CommonResult;
import com.weikun.api.dto.AIBaiduFaceBean;
import com.weikun.api.dto.UmsAdminLoginParam;

import com.weikun.api.dto.AIFaceBean;
import com.weikun.api.model.UmsAdmin;
import com.weikun.api.service.ITokenService;
import com.weikun.api.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 * admin
 * macro123
 */

@RestController
@Api(tags = "AdminController", description = "后台用户管理")
@RequestMapping("/admin")
@CrossOrigin//解决跨域问题，
public class UmsUserController {

    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Reference(
            version = "1.0.0",interfaceClass = IUserService.class,
            interfaceName = "com.weikun.api.service.IUserService",
            timeout =120000
    )
    private IUserService service;

    @Reference(
            version = "1.0.0",interfaceClass = ITokenService.class,
            interfaceName = "com.weikun.api.service.ITokenService",
            timeout =120000
    )
    private ITokenService tokenService;

    //人脸模块对象
    private AipFace aipFace = AIFactoryUtil.getAipFace();

    /**
     * 脸部登录一个用户
     * @param
     * @return
     */
    @ApiOperation(value = "脸部登录以后返回token")
    @PostMapping(value = "/flogin")
    @ResponseBody
    public CommonResult faceLogin(@RequestBody AIFaceBean faceBean) {
        Map<String, String> tokenMap = new HashMap<>();
        //部分人脸信息
        //log.info("发送过来的参数{}", JSONObject.toJSONString(faceBean));
        String groupIdList = "login";//用户组id（由数字、字母、下划线组成），长度限制128B
        //非阿里巴巴JSON 在已经注册的脸中搜寻
        org.json.JSONObject resultObject = aipFace.search(faceBean.getImgdata(),
                "BASE64", groupIdList, null);
        //使用fastjson处理返回的内容 直接用javabean接收 方便取值 百度要求的bean
        AIBaiduFaceBean faceSerachResponse = JSON.parseObject(resultObject.toString(), AIBaiduFaceBean.class);
        if("0".equals(faceSerachResponse.getError_code())&&"SUCCESS".equals(faceSerachResponse.getError_msg())){//成功
            //这里对人脸先检索，是否已经录入，设置判定条件为返回score大于80即代表同一个人
            if(faceSerachResponse.getResult().getUser_list().get(0).getScore()>80f){
                faceBean.setError_code(faceSerachResponse.getError_code());
                faceBean.setError_msg(faceSerachResponse.getError_msg());
                String userid=faceSerachResponse.getResult().getUser_list().get(0).getUser_id();//也是图片的名称
                //password 应该从后端取出
                UmsAdmin a=service.findUserById(Long.valueOf(userid));
                String password=a.getPassword();
                String username=a.getUsername();
                //人脸验证成功
                String token= tokenService.getToken(userid,password);
                //得到token码 和用户的json 并且存储id 方便进行其他的登录
                tokenMap.put("error_code",faceSerachResponse.getError_code());
                tokenMap.put("token", token);
                tokenMap.put("tokenHead", tokenHead);
                tokenMap.put("username", username);
                tokenMap.put("password", password);
                return CommonResult.success(tokenMap);
            }else{
                tokenMap.put("error_code",faceSerachResponse.getError_code());
                CommonResult.failed(tokenMap);
            }
        }else{
            tokenMap.put("error_code",faceSerachResponse.getError_code());
            CommonResult.failed(tokenMap);
        }
        return CommonResult.success(faceBean);
    }

    /**
     * 人脸注册


     * @return
     */
    @ApiOperation(value = "用户注册")
    @PostMapping("/reg")
    @ResponseBody
    public CommonResult reg(@RequestBody UmsAdmin umsAdmin){
        //先判断用户名是否已经注册了
        if(service.findByUsername(umsAdmin.getUsername())!=null){//已经被注册
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("error_code","500");
            return CommonResult.success(tokenMap);
        }

        String groupId = "login";//用户组id（由数字、字母、下划线组成），长度限制128B
        //必须换成新对象 否则id主键值 返不回来
        UmsAdmin a=new UmsAdmin();



        a=service.reg(umsAdmin);
        String userId=a.getId().toString();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info",umsAdmin.getPassword());//其实可以写个json信息 存到user_inco 就这么样吧 懒惰战胜了我
        //删除字符串前的提示信息 "data:image/png;base64,"
        String b64=umsAdmin.getPic().substring(22);//22之后开始
        org.json.JSONObject resultObject = aipFace.addUser(b64, "BASE64", groupId, userId, options);
        Map<String, String> tokenMap = new HashMap<>();

        tokenMap.put("error_code","200");

        return CommonResult.success(tokenMap);

    }

    /**
     * 登录一个用户
     * @param
     * @return
     */
    @ApiOperation(value = "登录以后返回token")
    @PostMapping(value = "/login")
    @ResponseBody
    //还没进入到系统 因此token没有产生
    //@SystemLog(description = "登录", type = LogType.USER_LOGIN)
    public CommonResult login(@RequestBody UmsAdminLoginParam user) {
        System.out.println(user);
        UmsAdmin user1=service.findByUsername(user.getUsername());
        if(user1==null){
            return CommonResult. validateFailed("用户不存在！");
        }else{
            if(!user1.getPassword().equals(user.getPassword())){
                Map<String, String> tokenMap = new HashMap<>();
                tokenMap.put("error_code","500");
                return CommonResult.failed(tokenMap);
                //return CommonResult.validateFailed("登录失败，密码错误！");
            }else{//密码验证成功
                String token= tokenService.getToken(user1.getId().toString(),user1.getPassword());
                //得到token码 和用户的json 并且存储id 方便进行其他的登录
                Map<String, String> tokenMap = new HashMap<>();
                tokenMap.put("token", token);
                tokenMap.put("error_code","200");
                tokenMap.put("tokenHead", tokenHead);
                return CommonResult.success(tokenMap);

            }
        }


    }


    @ApiOperation(value = "获取当前登录用户信息")
    @GetMapping(value = "/info")
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "获取当前登录用户信息", type = LogType.USER_INFO)
    public CommonResult getAdminInfo(HttpServletRequest request) {

        String token = request.getHeader(tokenHeader);
        //得到token后 由于有Bearer@XXXX，因此要去掉头部信息

        UmsAdmin umsAdmin = service.findByUmsAdmin(token.split("@")[1]);
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsAdmin.getUsername());
        data.put("roles", new String[]{"TEST"});
        data.put("icon", umsAdmin.getIcon());
        return CommonResult.success(data);
    }


    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    @UserLoginToken
    @SystemLog(description = "登出", type = LogType.USER_LOGIN_OUT)
    public CommonResult logout() {
        return CommonResult.success(null);
    }




}
