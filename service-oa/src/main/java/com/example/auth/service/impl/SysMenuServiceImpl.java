package com.example.auth.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.SysMenuMapper;
import com.example.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.service.SysRoleMenuService;
import com.example.auth.utils.MenuHelper;
import com.example.model.system.SysMenu;
import com.example.model.system.SysRoleMenu;
import com.example.serviceutil.exception.GuiguException;
import com.example.vo.system.AssginMenuVo;
import com.example.vo.system.MetaVo;
import com.example.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        //1.查询菜单所有数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        //2.构建树形结构
        List<SysMenu> list = MenuHelper.buildTree(sysMenuList);
        return list;
    }

    // 删除菜单
    @Override
    public void removeMenuById(Long id) {
        // 判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysMenu::getParentId,id);

        Integer count = baseMapper.selectCount(lambdaQueryWrapper);

        if (count>0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }
    //查询所有菜单和角色分配菜单
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //1.查询所有菜单
        LambdaQueryWrapper<SysMenu> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(SysMenu::getStatus,1);
        List<SysMenu> sysMenuList = baseMapper.selectList(wrapper1);

        //2.获取角色的菜单
        LambdaQueryWrapper<SysRoleMenu> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> roleMenuList = sysRoleMenuService.list(wrapper2);
        //3.根据角色菜单id，获取角色菜单信息
        List<Long> roleMenuIdList = roleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        for (SysMenu sysMenu : sysMenuList) {
            if(roleMenuIdList.contains(sysMenu.getId())){
                sysMenu.setSelect(true);
            }else sysMenu.setSelect(false);
        }
        List<SysMenu> list = MenuHelper.buildTree(sysMenuList);
        return list;
    }
    //角色分配菜单
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //1.删除原有的角色菜单信息
        LambdaQueryWrapper<SysRoleMenu> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper1);
        //2.分配角色菜单信息
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long aLong : menuIdList) {
            if(aLong==null)continue;
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenu.setMenuId(aLong);
            sysRoleMenuService.save(sysRoleMenu);
        }
    }
    //根据id获取用户可以操作的菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu>sysMenuList=new ArrayList<>();
        //1.判断用户是否为管理员,userId=1就是管理员,并且获取菜单列表
        if(userId==1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList= baseMapper.selectList(wrapper);
        }else{
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }
        //2.对菜单列表构建树形结构
        List<SysMenu> buildTree = MenuHelper.buildTree(sysMenuList);
        //3.构建框架要求的路由结构
        List<RouterVo>routers=this.buildRouter(buildTree);
        return routers;
    }

    private List<RouterVo> buildRouter(List<SysMenu> buildTree) {
        List<RouterVo>routers=new ArrayList<>();
        for (SysMenu sysMenu : buildTree) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(sysMenu));
            router.setComponent(sysMenu.getComponent());
            router.setMeta(new MetaVo(sysMenu.getName(), sysMenu.getIcon()));
            List<SysMenu> children = sysMenu.getChildren();
            if(sysMenu.getType()==1){
                List<SysMenu> hiddenMenuList  = children.stream().filter(c -> !StringUtils.isEmpty(c.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList ) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else{
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    // 递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    //根据id获取用户可以操作的按钮列表
    @Override
    public List<String> findPermsMenuListByUserId(Long userId) {
        List<SysMenu>sysMenuList=new ArrayList<>();
        //1.判断用户是否为管理员,userId=1就是管理员,并且获取菜单列表
        if(userId==1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList= baseMapper.selectList(wrapper);
        }else{
            sysMenuList=baseMapper.findMenuListByUserId(userId);
        }
        //2.从查询出来的数据里面，获取可以操作按钮的List集合
        List<String> permsList = sysMenuList.stream().filter(item -> item.getType() == 2).map(item -> item.getPerms()).collect(Collectors.toList());
        return permsList;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if (menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
}
