package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.ModelConvert;
import com.itsu.jllama.mapper.ModelConvertMapper;
import com.itsu.jllama.service.ModelConvertService;
import org.springframework.stereotype.Service;

@Service
public class ModelConvertServiceImpl extends ServiceImpl<ModelConvertMapper, ModelConvert> implements ModelConvertService {
}
