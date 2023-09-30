package com.example.wechat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.auth.service.SysUserService;
import com.example.model.process.Process;
import com.example.model.process.ProcessTemplate;
import com.example.model.system.SysUser;
import com.example.process.service.OaProcessService;
import com.example.process.service.OaProcessTemplateService;
import com.example.security.custome.LoginUserInfoHelper;
import com.example.wechat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {
    @Autowired
    private OaProcessService processService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService templateService;
    @Autowired
    private WxMpService wxMpService;
    //推送待审批人员
    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //获取流程信息
        Process process = processService.getById(processId);
        //获取审批人员信息
        SysUser sysUser = sysUserService.getById(userId);
        //获取模板信息
        ProcessTemplate template = templateService.getById(process.getProcessTemplateId());
        //获取申请人员
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        //获取审批人员openId
        String openId = sysUser.getOpenId();
        if(openId==null){
            //为了方便测试，使用自己的openId值
            openId="obAyN6DPirrpbSu5rNjijCFfIzF4";
        }
        //设置消息发送信息
        WxMpTemplateMessage message = WxMpTemplateMessage.builder()
                //审核人员的openId
                .toUser(openId)
                //模板消息的Id值
                .templateId("m3EQfuzhbMzQOenX2xK8q2SXV47qU8etzexTp3-Ovkg")
                //点击消息，跳转的网址
                .url("http://qianduan.v5.idcfengye.com/#/show/" + processId + "+taskId")
                .build();
        //获取审批模板的消息
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer stringBuffer = new StringBuffer();
        for(Map.Entry entry:formShowData.entrySet()){
            stringBuffer.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        //设置模板里面的参数值
        message.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+template.getName()+"审批申请，请注意查看。", "#272727"));
        message.addData(new WxMpTemplateData("keyword1",process.getProcessCode(),"#272727"));
        message.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        message.addData(new WxMpTemplateData("content", stringBuffer.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(message);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        log.info("推送消息返回：{}", msg);
    }
    //审批后推送提交审批人员
    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = templateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "obAyN6DPirrpbSu5rNjijCFfIzF4";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("pRjyz1Cu0y9xuR63DQDe2yTTMrSD6LMLnhar3Yw9TZk")//模板id
                .url("http://qianduan.v5.idcfengye.com/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        log.info("推送消息返回：{}", msg);
    }
}
