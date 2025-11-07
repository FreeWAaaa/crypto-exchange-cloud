package com.cex.admin.service;

import java.util.Map;

/**
 * 数据统计服务接口
 * 
 * @author cex
 */
public interface StatisticsService {
    
    /**
     * 获取首页统计数据
     */
    Map<String, Object> getDashboardStats();
    
    /**
     * 获取用户统计
     */
    Map<String, Object> getUserStats(String startDate, String endDate);
    
    /**
     * 获取交易统计
     */
    Map<String, Object> getTradeStats(String startDate, String endDate);
    
    /**
     * 获取财务统计
     */
    Map<String, Object> getFinanceStats(String startDate, String endDate);
}

