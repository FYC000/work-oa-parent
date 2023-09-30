package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.model.system.SysOperLog;
import com.example.vo.system.SysOperLogQueryVo;
import com.example.auth.mapper.SysOperLogMapper;
import com.example.auth.service.SysOperLogService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>implements SysOperLogService {

    @Resource
    private SysOperLogMapper sysOperLogMapper;

    @Override
    public void saveSysLog(SysOperLog sysOperLog) {
        sysOperLogMapper.insert(sysOperLog);
    }

    @Override
    public IPage<SysOperLog> selectPage(Page<SysOperLog> page1, @Param("vo") SysOperLogQueryVo vo) {
        return sysOperLogMapper.selectPage(page1,vo,null);
    }

    @Override
    public SysOperLog selectById(Integer id) {
        return baseMapper.selectById(id);
    }

}
