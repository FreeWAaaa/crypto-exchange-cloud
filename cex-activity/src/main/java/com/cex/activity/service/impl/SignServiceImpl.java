package com.cex.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.activity.domain.entity.SignActivity;
import com.cex.activity.domain.entity.SignRecord;
import com.cex.activity.mapper.SignActivityMapper;
import com.cex.activity.mapper.SignRecordMapper;
import com.cex.activity.service.SignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 签到服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class SignServiceImpl implements SignService {
    
    @Autowired
    private SignActivityMapper activityMapper;
    
    @Autowired
    private SignRecordMapper recordMapper;
    
    // TODO: 注入 Wallet Feign Client 发放奖励
    // @Autowired
    // private WalletFeignClient walletClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signIn(Long userId) {
        log.info("用户签到：userId={}", userId);
        
        // 1. 检查今日是否已签到
        if (isTodaySigned(userId)) {
            throw new RuntimeException("今日已签到");
        }
        
        // 2. 获取当前活动
        SignActivity activity = getCurrentActivity();
        if (activity == null) {
            throw new RuntimeException("当前无签到活动");
        }
        
        // 3. 计算连续签到天数
        int consecutiveDays = getConsecutiveDays(userId) + 1;
        
        // 4. 创建签到记录
        SignRecord record = new SignRecord();
        record.setUserId(userId);
        record.setActivityId(activity.getId());
        record.setCoin(activity.getCoin());
        record.setAmount(activity.getAmount());
        record.setSignDate(new Date());
        record.setConsecutiveDays(consecutiveDays);
        recordMapper.insert(record);
        
        // 5. 发放奖励到钱包
        // TODO: 调用 Wallet 服务增加余额
        // walletClient.increaseBalance(userId, activity.getCoin(), activity.getAmount(), "签到奖励");
        log.info("签到成功，奖励：{} {}", activity.getAmount(), activity.getCoin());
    }
    
    @Override
    public SignActivity getCurrentActivity() {
        return activityMapper.selectCurrentActivity();
    }
    
    @Override
    public List<SignRecord> getUserSignRecords(Long userId) {
        LambdaQueryWrapper<SignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignRecord::getUserId, userId);
        wrapper.orderByDesc(SignRecord::getSignDate);
        
        return recordMapper.selectList(wrapper);
    }
    
    @Override
    public boolean isTodaySigned(Long userId) {
        return recordMapper.countTodaySign(userId) > 0;
    }
    
    @Override
    public int getConsecutiveDays(Long userId) {
        return recordMapper.getConsecutiveDays(userId);
    }
}

