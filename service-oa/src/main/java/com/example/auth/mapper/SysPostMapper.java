package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.model.system.SysPost;
import com.example.vo.system.SysPostQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    IPage<SysPost> selectPage(Page<SysPost> postPage, @Param("vo") SysPostQueryVo postQueryVo);
}
