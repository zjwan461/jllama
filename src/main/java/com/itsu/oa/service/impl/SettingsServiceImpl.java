package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.oa.entity.Settings;
import com.itsu.oa.mapper.SettingsMapper;
import com.itsu.oa.service.SettingsService;
import org.springframework.stereotype.Service;

/**
 * @author jerry.su
 * @date 2025/4/5 15:48
 */
@Service
public class SettingsServiceImpl extends ServiceImpl<SettingsMapper, Settings> implements SettingsService {
}
