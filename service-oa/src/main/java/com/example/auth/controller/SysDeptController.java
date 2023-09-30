package com.example.auth.controller;


import com.example.auth.service.SysDeptService;
import com.example.commonutil.result.Result;
import com.example.model.system.SysDept;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "部门管理")
@RequestMapping("/admin/system/sysDept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    @ApiOperation("获取部门数据")
    @GetMapping("/findDepts")
    public Result findDepts(){
        List<SysDept> lists=sysDeptService.findDepts();
        return Result.ok(lists);
    }

    @ApiOperation("添加部门")
    @PostMapping("/save")
    public Result save(@RequestBody SysDept sysDept){
        sysDeptService.save(sysDept);
        return Result.ok();
    }

    @ApiOperation("根据id查询")
    @GetMapping("/find/{id}")
    public Result findById(@PathVariable("id") Integer id){
        SysDept dept=sysDeptService.findById(id);
        return Result.ok(dept);
    }

    @ApiOperation("更新数据")
    @PostMapping("update")
    public Result update(@RequestBody SysDept sysDept){
        sysDeptService.updateData(sysDept);
        return Result.ok();
    }

    @ApiOperation("删除数据")
    @DeleteMapping("/remove/{id}")
    public Result delete(@PathVariable("id") Long id){
        boolean b = sysDeptService.removeById(id);
        if(b){
            return Result.ok();
        }
        else{
            return Result.fail();
        }
    }
}

