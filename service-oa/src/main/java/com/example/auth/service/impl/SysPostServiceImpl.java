package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.SysPostMapper;
import com.example.auth.service.SysPostService;
import com.example.model.system.SysPost;
import com.example.vo.system.SysPostQueryVo;
import org.springframework.stereotype.Service;

@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    @Override
    public void switchStatus(String id, Integer status) {
        SysPost sysPost = baseMapper.selectById(id);
        sysPost.setStatus(status);
        baseMapper.updateById(sysPost);
    }

    @Override
    public IPage<SysPost> selectPage(Page<SysPost> postPage, SysPostQueryVo postQueryVo) {
        return baseMapper.selectPage(postPage,postQueryVo);
    }

}
