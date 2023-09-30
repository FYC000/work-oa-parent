package com.example.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.service.SysLoginLogService;
import com.example.commonutil.result.Result;
import com.example.model.system.SysLoginLog;
import com.example.vo.system.SysLoginLogQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags="登录记录管理")
@RequestMapping("/admin/system/sysLoginLog")
public class SysLoginLogController {

    @Autowired
    private SysLoginLogService sysLoginLogService;

    @ApiOperation("获取登录日志列表")
    @GetMapping("/find/{page}/{limit}")
    public Result list(@PathVariable("page")Integer page, @PathVariable("limit")Integer limit, SysLoginLogQueryVo vo){
        Page<SysLoginLog> page1 = new Page<>(page, limit);
        IPage<SysLoginLog> iPage= sysLoginLogService.selectPage(page1,vo);
        return Result.ok(iPage);
    }

    @ApiOperation("通过id获取数据")
    @GetMapping("/get/{id}")
    public Result selectById(@PathVariable("id")Integer id){
        SysLoginLog sysLoginLog = sysLoginLogService.getById(id);
        return Result.ok(sysLoginLog);
    }
}
