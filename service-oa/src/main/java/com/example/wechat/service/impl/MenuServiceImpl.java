package com.example.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.model.wechat.Menu;
import com.example.vo.wechat.MenuVo;
import com.example.wechat.mapper.MenuMapper;
import com.example.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Autowired
    private WxMpService wxMpService;
    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo>orderList=new ArrayList<>();
        //获取所有菜单
        List<Menu> menuList = baseMapper.selectList(null);
        //获取所有一级菜单，prarentId=0
        List<Menu> oneList = menuList.stream().filter(item -> item.getParentId().intValue() == 0).collect(Collectors.toList());
        //遍历一级菜单
        for (Menu menu : oneList) {
            MenuVo menuVo = new MenuVo();
            BeanUtils.copyProperties(menu,menuVo);
            List<Menu> towList = menuList.stream().filter(item -> item.getParentId().intValue() == menu.getId().intValue())
                    .sorted(Comparator.comparing(Menu::getSort)).collect(Collectors.toList());
            List<MenuVo> children = new ArrayList<>();
            for (Menu menu1 : towList) {
                MenuVo menuVo1 = new MenuVo();
                BeanUtils.copyProperties(menu1,menuVo1);
                children.add(menuVo1);
            }
            menuVo.setChildren(children);
            orderList.add(menuVo);
        }
        return orderList;
    }

    @Override
    public void syncMenu() {
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://qianduan.v5.idcfengye.com/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://qianduan.v5.idcfengye.com#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }
}
