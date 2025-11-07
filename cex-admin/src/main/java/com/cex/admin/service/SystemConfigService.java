package com.cex.admin.service;

import com.cex.admin.domain.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 * 
 * @author cex
 */
public interface SystemConfigService {
    
    /**
     * 查询所有配置
     */
    List<SysConfig> getAllConfigs();
    
    /**
     * 根据配置键查询
     */
    SysConfig getByKey(String configKey);
    
    /**
     * 更新配置
     */
    void updateConfig(SysConfig config);
    
    /**
     * 添加配置
     */
    void addConfig(SysConfig config);
    
    /**
     * 删除配置
     */
    void deleteConfig(Long id);
}

