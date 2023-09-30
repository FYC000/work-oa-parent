package com.example.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.system.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {
    List<SysDept> findDepts();

    SysDept findById(Integer id);

    void updateData(SysDept sysDept);

    void deleteById(Integer id);
}
