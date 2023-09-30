package com.example.auth.utils;


import com.example.model.system.SysDept;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FuYiCheng
 * @date 2023-03-09 23:42
 * @description:实现部门构建器
 * @version:
 */
public class DeptHelper {

    public static List<SysDept>buildTrees(List<SysDept>depts){
        //创建树形结构
        List<SysDept> list=new ArrayList<>();
        //遍历部门集合
        for (SysDept dept : depts) {
            //找到递归入口
            if(dept.getParentId()==0){
                list.add(findChildren(dept,depts));
            }
        }
        return list;
    }

    private static SysDept findChildren(SysDept dept, List<SysDept> depts) {
        for (SysDept sysDept : depts) {
            if(dept.getChildren()==null){
                dept.setChildren(new ArrayList<>());
            }
            if(dept.getId()==sysDept.getParentId()){
                dept.getChildren().add(findChildren(sysDept,depts));
            }
        }
        return dept;
    }
}
