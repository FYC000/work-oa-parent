package com.example.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysMenu;
import com.example.vo.system.AssginMenuVo;
import com.example.vo.system.RouterVo;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findMenuByRoleId(Long roleId);

    void doAssign(AssginMenuVo assginMenuVo);
    //根据id获取用户可以操作的菜单列表
    List<RouterVo> findUserMenuListByUserId(Long userId);
    //根据id获取用户可以操作的按钮列表
    List<String> findPermsMenuListByUserId(Long userId);
}
