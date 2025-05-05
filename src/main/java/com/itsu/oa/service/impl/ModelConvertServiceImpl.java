package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.oa.entity.ModelConvert;
import com.itsu.oa.mapper.ModelConvertMapper;
import com.itsu.oa.service.ModelConvertService;
import org.springframework.stereotype.Service;

@Service
public class ModelConvertServiceImpl extends ServiceImpl<ModelConvertMapper, ModelConvert> implements ModelConvertService {
}
