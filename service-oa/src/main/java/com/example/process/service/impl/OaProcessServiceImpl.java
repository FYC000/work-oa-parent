package com.example.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.service.SysUserService;
import com.example.model.process.Process;
import com.example.model.process.ProcessRecord;
import com.example.model.process.ProcessTemplate;
import com.example.model.system.SysUser;
import com.example.process.mapper.OaProcessMapper;
import com.example.process.service.OaProcessRecordService;
import com.example.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.process.service.OaProcessTemplateService;
import com.example.security.custome.LoginUserInfoHelper;
import com.example.vo.process.ApprovalVo;
import com.example.vo.process.ProcessFormVo;
import com.example.vo.process.ProcessQueryVo;
import com.example.vo.process.ProcessVo;
import com.example.wechat.service.MessageService;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService processRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private MessageService messageService;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    @Override
    public void deployByZip(String deployPath) {
        //定义zip输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //流程部署
        repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1.根据用户id获取用户信息
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        //2.根据模板id获取模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //3.新增process数据
        Process process = new Process();
        //将processFormVo复制到process对象里面
        BeanUtils.copyProperties(processFormVo,process);
        //其他值
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        //添加新数据
        baseMapper.insert(process);
        //4.启动流程实例
        //4.1 流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //4.2 业务key processId
        Long processId = process.getId();
        //4.3 表单数据转换为map记录
        String formValues = process.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        Map<String, Object> map = new HashMap<>();
        for(Map.Entry<String, Object>entry:formData.entrySet()){
            map.put(entry.getKey(),entry.getValue());
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, String.valueOf(processId), map);
        //4.4 业务表关联当前流程实例id
        String processInstanceId = processInstance.getId();
        process.setProcessInstanceId(processInstanceId);
        List<String> nameList = new ArrayList<>();
        //5.计算下一个审批人，可能有多个（并行审批）
        List<Task> taskList=this.getCurrentTaskList(processInstanceId);
        for (Task task : taskList) {
            SysUser user = sysUserService.getUserByUserName(task.getAssignee());
            String name = user.getName();
            nameList.add(name);
            //推送消息给下一个审批人
            messageService.pushPendingMessage(processId,user.getId(),task.getId());
            process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        }
        baseMapper.updateById(process);

        //记录操作行为
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        //1.封装查询条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        //2.调用方法分页条件查询,返回List集合，代办任务集合
        //list有两个参数,第一个参数->开始位置，第二个参数->每页显示记录数
        List<Task> list = query.listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        long totalCount = query.count();
        //3.将List<task>->List<ProcessVo>
        List<ProcessVo> processList = new ArrayList<>();
        // 根据流程的业务ID查询实体并关联
        for (Task item : list) {
            //1.获取流程实例id
            String processInstanceId = item.getProcessInstanceId();
            //2.获取流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                continue;
            }
            //3.获取流程id
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            //4.获取流程
            Process process = this.getById(Long.parseLong(businessKey));
            if(process==null)continue;;
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(item.getId());
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }
    //获取审批详情信息
    @Override
    public Map<String, Object> show(Long id) {
        //1.通过流程id获取process
        Process process = baseMapper.selectById(id);
        //2.通过流程id获取流程记录
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        //3.获取模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //4.判断当前用户是否可以审批任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        boolean isApprove=false;
        for (Task task : taskList) {
            if(task.getAssignee().equals(LoginUserInfoHelper.getUsername()))isApprove=true;
        }
        //5.将查询数据封装到map集合中
        Map<String,Object>map=new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //1.通过taskId获取流程变量
        Map<String, Object> variables = taskService.getVariables(approvalVo.getTaskId());
        for(Map.Entry<String,Object>entry:variables.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //2.判断审批状态值
        Integer status = approvalVo.getStatus();
        if(status==1){
            //状态值为1，表示审批通过
            approvalVo.setDescription("已通过");
            taskService.complete(approvalVo.getTaskId());
        }else{
            //状态值为2，表示审批未通过
            approvalVo.setDescription("未通过");
            this.endTask(approvalVo.getTaskId());
        }
        //3.记录审批过程的相关信息
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),approvalVo.getDescription());
        //4.查询下一个审批人，更新流程数据表记录
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String>assigneeList=new ArrayList<>();
            for (Task task : taskList) {
                SysUser sysUser = sysUserService.getUserByUserName(task.getAssignee());
                String name = sysUser.getName();
                assigneeList.add(name);
                //推送消息给下一个审批人
                messageService.pushProcessedMessage(process.getId(),sysUser.getId(),status);
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(status==1){
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            }else{
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        //推送消息给申请人
        messageService.pushProcessedMessage(process.getId(), process.getUserId(), approvalVo.getStatus());
        this.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().finished().orderByTaskCreateTime().desc();
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size =(int)pageParam.getSize();
        List<HistoricTaskInstance> historicTaskInstanceList = query.listPage(begin, size);
        int count= query().count();
        List<ProcessVo>processVoList=new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
            String processInstanceId = historicTaskInstance.getProcessInstanceId();
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            ProcessVo processVo = new ProcessVo();
            if(process==null)continue;
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId("0");
            processVoList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), count);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }

    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List<EndEvent> eventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(eventList)) {
            return;
        }
        FlowNode endEvent = eventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //清理活动方向
        currentFlowNode.getOutgoingFlows().clear();
        //建立新方向
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("newSequenceFlowId");
        sequenceFlow.setSourceFlowElement(currentFlowNode);
        sequenceFlow.setTargetFlowElement(endEvent);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(sequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //  完成当前任务
        taskService.complete(task.getId());
    }

    private List<Task> getCurrentTaskList(String processInstanceId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        return taskList;
    }
}
