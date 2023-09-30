package com.example.auth.utils;

import com.example.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FuYiCheng
 * @date 2023-08-20 15:03
 * @description:
 * @version:
 */
public class MenuHelper {
    /*
     *
     * 使用递归方法构建菜单
     * @param null
     * @return
     * @create
     **/
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        List<SysMenu>trees=new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            //找到递归入口
            if(sysMenu.getParentId().longValue()==0){
                trees.add(getChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }
    /*
     *
     * 递归查找子节点
     * @param null
     * @return
     * @create
     **/
    public static SysMenu getChildren(SysMenu sysMenu,List<SysMenu> sysMenuList){
        sysMenu.setChildren(new ArrayList<>());
        for (SysMenu menu : sysMenuList) {
            if(sysMenu.getId().longValue()==menu.getParentId().longValue()){
                sysMenu.getChildren().add(getChildren(menu,sysMenuList));
            }
        }

        return sysMenu;
    }
}
