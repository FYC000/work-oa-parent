package com.example.auth.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.commonutil.result.Result;
import com.example.model.system.SysOperLog;
import com.example.vo.system.SysOperLogQueryVo;
import com.example.auth.service.SysOperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api("操作日志接口")
@RestController
@RequestMapping("/admin/system/sysOperLog")
public class SysOperLogController {

    @Resource
    private SysOperLogService sysOperLogService;

    @ApiOperation("获取操作日志数据")
    @GetMapping("/find/{page}/{limit}")
    public Result findList(@PathVariable("page")Integer page, @PathVariable("limit")Integer limit, SysOperLogQueryVo vo){
        Page<SysOperLog> page1=new Page<>(page,limit);
        IPage<SysOperLog> iPage= sysOperLogService.selectPage(page1,vo);
        return Result.ok(iPage);
    }

    @ApiOperation("通过id获取指定数据")
    @GetMapping("/select/{id}")
    public Result selectById(@PathVariable("id")Integer id){
        SysOperLog log=sysOperLogService.selectById(id);
        return Result.ok(log);
    }
}
