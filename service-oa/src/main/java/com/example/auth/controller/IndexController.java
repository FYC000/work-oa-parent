package com.example.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.auth.service.SysMenuService;
import com.example.auth.service.SysUserService;
import com.example.commonutil.jwt.JwtHelper;
import com.example.commonutil.result.Result;
import com.example.commonutil.utils.MD5;
import com.example.model.system.SysUser;
import com.example.serviceutil.exception.GuiguException;
import com.example.vo.system.LoginVo;
import com.example.vo.system.RouterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysMenuService menuService;

    //登录
    @ApiOperation("登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        /*Map<String, Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);*/
        //1.通过用户名获取用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,loginVo.getUsername());
        SysUser user = userService.getOne(wrapper);
        //2.判断用户是否存在
        if(user==null)throw new GuiguException(201,"用户不存在");
        //3.判断密码是否一致
        String password = MD5.encrypt(loginVo.getPassword());
        if(!user.getPassword().equals(password))throw new GuiguException(201,"密码不一致");
        //4.判断状态是否可用
        if(user.getStatus().intValue()!=1)throw new GuiguException(201,"用户被禁用");
        //5.通过jwt根据用户名和id生成token
        String token = JwtHelper.createToken(user.getId(), user.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }

    //获取用户信息
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        //1.通过请求头获取token
        String token = request.getHeader("token");
        //2.获取用户id
        Long userId = JwtHelper.getUserId(token);
        //3.获取用户信息
        SysUser user = userService.getById(userId);
        //4.根据id获取用户可以操作的菜单列表
        List<RouterVo> routerList=menuService.findUserMenuListByUserId(userId);
        //5.根据id获取用户可以操作的按钮列表
        List<String>permsList=menuService.findPermsMenuListByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",user.getUsername());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        // 返回用户可以操作的菜单
        map.put("routers", routerList);
        // 返回用户可以操作的按钮
        map.put("buttons", permsList);
        return Result.ok(map);
    }

    //退出
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
