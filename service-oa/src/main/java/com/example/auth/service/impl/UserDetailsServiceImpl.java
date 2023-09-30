package com.example.auth.service.impl;

import com.example.auth.service.SysMenuService;
import com.example.auth.service.SysUserService;
import com.example.model.system.SysUser;
import com.example.security.custome.CustomUser;
import com.example.security.custome.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 根据用户名查询
        SysUser sysUser = sysUserService.getUserByUserName(username);

        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }

        //通过userid获取权限数据
        List<String> permsList = sysMenuService.findPermsMenuListByUserId(sysUser.getId());
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String s : permsList) {
            authorities.add(new SimpleGrantedAuthority(s.trim()));
        }

        return new CustomUser(sysUser, authorities);
    }
}
