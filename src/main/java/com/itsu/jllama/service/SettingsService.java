package com.itsu.jllama.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itsu.jllama.entity.Settings;

/**
 * @author jerry.su
 * @date 2025/4/5 15:47
 */
public interface SettingsService extends IService<Settings> {

    Settings getCachedSettings();

    void updateCachedSettings(Settings settings);
}
