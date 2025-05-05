package com.itsu.jllama.service.impl;

import cn.hutool.cache.impl.TimedCache;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.Settings;
import com.itsu.jllama.mapper.SettingsMapper;
import com.itsu.jllama.service.SettingsService;
import org.springframework.stereotype.Service;

/**
 * @author jerry.su
 * @date 2025/4/5 15:48
 */
@Service
public class SettingsServiceImpl extends ServiceImpl<SettingsMapper, Settings> implements SettingsService {

    private final static TimedCache<String, Settings> cache = new TimedCache<>(1000 * 60 * 15);

    private static final String CACHE_KEY = "settings";

    static {
        cache.schedulePrune(5000);
    }

    public static void setCache(Settings settings) {
        cache.put(CACHE_KEY, settings);
    }

    public static Settings getCache() {
        return cache.get(CACHE_KEY);
    }

    @Override
    public Settings getCachedSettings() {
        if (cache.containsKey(CACHE_KEY)) {
            return cache.get(CACHE_KEY);
        }
        Settings settings = this.getById(Settings.DEFAULT_ID);
        setCache(settings);
        return settings;
    }

    @Override
    public void updateCachedSettings(Settings settings) {
        cache.remove(CACHE_KEY);
        this.updateById(settings);
    }
}
