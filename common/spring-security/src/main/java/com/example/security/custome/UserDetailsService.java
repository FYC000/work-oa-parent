package com.example.security.custome;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    //根据用户名获取用户对象
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
