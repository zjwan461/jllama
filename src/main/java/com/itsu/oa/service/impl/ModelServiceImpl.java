package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.oa.entity.Model;
import com.itsu.oa.mapper.ModelMapper;
import com.itsu.oa.service.ModelService;
import org.springframework.stereotype.Service;

@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {
}
