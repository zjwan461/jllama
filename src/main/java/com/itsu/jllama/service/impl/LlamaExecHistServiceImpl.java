package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.LlamaExecHis;
import com.itsu.jllama.mapper.LlamaExecHistMapper;
import com.itsu.jllama.service.LlamaExecHistService;
import org.springframework.stereotype.Service;

@Service
public class LlamaExecHistServiceImpl extends ServiceImpl<LlamaExecHistMapper, LlamaExecHis> implements LlamaExecHistService {
}
