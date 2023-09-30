package com.example.auth.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysPost;
import com.example.vo.system.SysPostQueryVo;

public interface SysPostService extends IService<SysPost> {


    void switchStatus(String id, Integer status);

    IPage<SysPost> selectPage(Page<SysPost> postPage, SysPostQueryVo postQueryVo);
}
