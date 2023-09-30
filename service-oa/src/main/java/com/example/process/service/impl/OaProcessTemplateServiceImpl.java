package com.example.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.model.process.ProcessTemplate;
import com.example.model.process.ProcessType;
import com.example.process.mapper.OaProcessTemplateMapper;
import com.example.process.service.OaProcessService;
import com.example.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {
    @Autowired
    private OaProcessTypeService service;
    @Autowired
    private OaProcessService processService;
    //分页查询审批模板，把审批类型对应名称查询
    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        //1.调用mapper的方法实现分页查询
        Page<ProcessTemplate> page = baseMapper.selectPage(pageParam, null);
        //2.获取分页数据
        List<ProcessTemplate> records = page.getRecords();
        //3.遍历list集合，得到每个对象的审批类型id
        for (ProcessTemplate record : records) {
            Long processTypeId = record.getProcessTypeId();
            //4.根据审批类型id，查询获取对应名称
            LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            ProcessType processType = service.getOne(wrapper);
            if(processType==null)continue;
            record.setProcessTypeName(processType.getName());
        }
        return page;
    }

    @Override
    public void publish(Long id) {
        //修改状态为1进行发布
        ProcessTemplate template = baseMapper.selectById(id);
        template.setStatus(1);
        baseMapper.updateById(template);
        //流程定义部署
        if(!StringUtils.isEmpty(template.getProcessDefinitionPath())){
            processService.deployByZip(template.getProcessDefinitionPath());
        }
    }
}
