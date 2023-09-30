package com.example.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    SysUser getUserByUserName(String username);

    Map<String, Object> getCurrentUser();
}
