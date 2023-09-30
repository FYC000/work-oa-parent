package com.example.process.service;

import com.example.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OaProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId, Integer status, String description);
}
