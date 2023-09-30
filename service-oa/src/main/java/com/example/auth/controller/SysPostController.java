package com.example.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.service.SysPostService;
import com.example.commonutil.result.Result;
import com.example.model.system.SysPost;
import com.example.vo.system.SysPostQueryVo;
import com.example.auth.annotation.Log;
import com.example.auth.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "岗位管理")
@RequestMapping("/admin/system/sysPost")
public class SysPostController {

    @Autowired
    private SysPostService sysPostService;

    @Log(title = "岗位管理", businessType = BusinessType.OTHER)
    @ApiOperation("获取岗位数据")
    @GetMapping("/findPosts/{page}/{limit}")
    public Result findPosts(@PathVariable("page")Integer page,
                            @PathVariable("limit")Integer limit,
                            SysPostQueryVo postQueryVo){
        Page<SysPost> postPage = new Page<>(page, limit);
        IPage<SysPost> postIPage=sysPostService.selectPage(postPage,postQueryVo);
        return Result.ok(postIPage);
    }

    @ApiOperation("添加岗位")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('bnt.sysPost.add')")
    @PostMapping("/save")
    public Result save(@RequestBody SysPost sysPost){
        sysPostService.save(sysPost);
        return Result.ok();
    }

    @ApiOperation("通过id查询岗位数据")
    @Log(title = "岗位管理", businessType = BusinessType.OTHER)
    @GetMapping("/query/{id}")
    public Result selectById(@PathVariable("id")Long id){
        SysPost post = sysPostService.getById(id);
        return Result.ok(post);
    }

    @ApiOperation("修改选定的岗位数据")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('bnt.sysPost.update')")
    @PostMapping("/update")
    public Result update(@RequestBody SysPost sysPost){
        sysPostService.updateById(sysPost);
        return Result.ok();
    }

    @ApiOperation("通过id删除指定数据")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysPost.remove')")
    @DeleteMapping("/remove/{id}")
    public Result removeById(@PathVariable("id") Integer id){
        boolean b = sysPostService.removeById(id);
        if(b){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation("改变状态")
    @Log(title = "岗位管理", businessType = BusinessType.STATUS)
    @GetMapping("/change/{id}/{status}")
    public Result changeStatus(@PathVariable("id")String id,@PathVariable("status")Integer status){
        sysPostService.switchStatus(id,status);
        return Result.ok();
    }
}
