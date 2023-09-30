package com.example.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
