package com.example.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysLoginLog;
import com.example.vo.system.SysLoginLogQueryVo;

public interface SysLoginLogService extends IService<SysLoginLog> {
    public void recordLoginLog(String username,Integer status,String IPadd,String mes);

    IPage<SysLoginLog> selectPage(Page<SysLoginLog> page1, SysLoginLogQueryVo vo);
}