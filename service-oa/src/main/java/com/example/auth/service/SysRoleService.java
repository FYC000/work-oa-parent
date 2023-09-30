package com.example.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysRole;
import com.example.vo.system.AssginRoleVo;
import org.springframework.stereotype.Repository;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    // 1、查询所有角色 和 当前用户所属角色
    public Map<String, Object> findRoleDataByUserId(Long userId);

    // 2、为用户分配角色
    public void doAssign(AssginRoleVo assginRoleVo);
}