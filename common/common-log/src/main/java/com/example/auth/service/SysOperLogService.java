package com.example.auth.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysOperLog;
import com.example.vo.system.SysOperLogQueryVo;
import org.apache.ibatis.annotations.Param;

public interface SysOperLogService extends IService<SysOperLog> {
    public void saveSysLog(SysOperLog sysOperLog);


    IPage<SysOperLog> selectPage(Page<SysOperLog> pageParam,SysOperLogQueryVo sysOperLogQueryVo);

    SysOperLog selectById(Integer id);
}
