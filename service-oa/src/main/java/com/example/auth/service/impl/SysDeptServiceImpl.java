package com.example.auth.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.SysDeptMapper;
import com.example.auth.service.SysDeptService;
import com.example.auth.utils.DeptHelper;
import com.example.model.system.SysDept;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> findDepts() {
        List<SysDept> sysDepts = baseMapper.selectList(null);
        return DeptHelper.buildTrees(sysDepts);
    }

    @Override
    public SysDept findById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void updateData(SysDept sysDept) {
        baseMapper.updateById(sysDept);
    }

    @Override
    public void deleteById(Integer id) {
        baseMapper.deleteById(id);
    }
}
