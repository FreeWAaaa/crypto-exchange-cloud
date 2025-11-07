package com.cex.activity.service;

import com.cex.activity.domain.entity.SignActivity;
import com.cex.activity.domain.entity.SignRecord;

import java.util.List;

/**
 * 签到服务接口
 * 
 * @author cex
 */
public interface SignService {
    
    /**
     * 用户签到
     */
    void signIn(Long userId);
    
    /**
     * 查询当前签到活动
     */
    SignActivity getCurrentActivity();
    
    /**
     * 查询用户签到记录
     */
    List<SignRecord> getUserSignRecords(Long userId);
    
    /**
     * 查询用户今日是否已签到
     */
    boolean isTodaySigned(Long userId);
    
    /**
     * 查询用户连续签到天数
     */
    int getConsecutiveDays(Long userId);
}

