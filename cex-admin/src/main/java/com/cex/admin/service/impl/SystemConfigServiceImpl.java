package com.cex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.admin.domain.entity.SysConfig;
import com.cex.admin.mapper.SysConfigMapper;
import com.cex.admin.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统配置服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    
    @Autowired
    private SysConfigMapper configMapper;
    
    @Override
    public List<SysConfig> getAllConfigs() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysConfig::getConfigType, SysConfig::getConfigKey);
        return configMapper.selectList(wrapper);
    }
    
    @Override
    public SysConfig getByKey(String configKey) {
        return configMapper.selectByKey(configKey);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(SysConfig config) {
        if (config.getId() == null) {
            throw new RuntimeException("配置ID不能为空");
        }
        configMapper.updateById(config);
        log.info("系统配置已更新：key={}", config.getConfigKey());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(SysConfig config) {
        // 检查键是否已存在
        SysConfig existingConfig = configMapper.selectByKey(config.getConfigKey());
        if (existingConfig != null) {
            throw new RuntimeException("配置键已存在：" + config.getConfigKey());
        }
        
        configMapper.insert(config);
        log.info("系统配置已添加：key={}", config.getConfigKey());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        configMapper.deleteById(id);
        log.info("系统配置已删除：id={}", id);
    }
}

