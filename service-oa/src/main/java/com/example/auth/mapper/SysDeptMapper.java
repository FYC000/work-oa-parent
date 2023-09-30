package com.example.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.model.system.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysDeptMapper extends BaseMapper<SysDept> {
}
