package com.cex.admin.service;

import com.cex.admin.domain.entity.SysConfig;
import com.cex.common.core.domain.Result;

import java.util.List;

/**
 * 系统管理服务接口
 * 
 * @author cex
 */
public interface AdminService {

    /**
     * 获取系统配置列表
     */
    Result<List<SysConfig>> getConfigList(Integer configType);

    /**
     * 更新系统配置
     */
    Result<Void> updateConfig(String configKey, String configValue);

    /**
     * 获取系统统计信息
     */
    Result<Object> getSystemStats();

    /**
     * 获取用户统计信息
     */
    Result<Object> getUserStats();

    /**
     * 获取交易统计信息
     */
    Result<Object> getTradeStats();

    /**
     * 获取钱包统计信息
     */
    Result<Object> getWalletStats();
}
