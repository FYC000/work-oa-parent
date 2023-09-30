package com.example.security.filter;

import com.alibaba.fastjson.JSON;
import com.example.commonutil.jwt.JwtHelper;
import com.example.commonutil.result.ResponseUtil;
import com.example.commonutil.result.Result;
import com.example.commonutil.result.ResultCodeEnum;
import com.example.security.custome.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;
    public TokenAuthenticationFilter(RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        logger.info("uri:"+request.getRequestURI());
        if("/admin/system/index/login".equals(request.getRequestURI())){
            chain.doFilter(request,response);
            return ;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("token");
        if(!StringUtils.isEmpty(token)){
            String username = JwtHelper.getUsername(token);
            if(!StringUtils.isEmpty(username)){
                String authString = (String) redisTemplate.opsForValue().get(username);
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(username);
                if(!StringUtils.isEmpty(authString)){
                    List<Map> maps = JSON.parseArray(authString, Map.class);
                    List<SimpleGrantedAuthority>authorities=new ArrayList<>();
                    for (Map map : maps) {
                        authorities.add(new SimpleGrantedAuthority((String)map.get("authority")));
                    }
                    return new UsernamePasswordAuthenticationToken(username,null, authorities);
                }else return new UsernamePasswordAuthenticationToken(username,null, Collections.emptyList());
            }
        }
        return null;
    }

}
