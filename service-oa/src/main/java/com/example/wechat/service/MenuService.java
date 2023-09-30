package com.example.wechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.wechat.Menu;
import com.example.vo.wechat.MenuVo;

import java.util.List;

public interface MenuService extends IService<Menu> {

    List<MenuVo> findMenuInfo();

    void syncMenu();

    void removeMenu();
}
