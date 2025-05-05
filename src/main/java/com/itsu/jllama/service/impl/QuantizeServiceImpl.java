package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.Quantize;
import com.itsu.jllama.mapper.QuantizeMapper;
import com.itsu.jllama.service.QuantizeService;
import org.springframework.stereotype.Service;

@Service
public class QuantizeServiceImpl extends ServiceImpl<QuantizeMapper, Quantize> implements QuantizeService {
}
