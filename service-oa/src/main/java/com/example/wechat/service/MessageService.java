package com.example.wechat.service;

public interface MessageService {
    //推送待审批人员
    void pushPendingMessage(Long processId, Long userId, String taskId);

    //审批后推送提交审批人员
    void pushProcessedMessage(Long processId, Long userId, Integer status);
}
