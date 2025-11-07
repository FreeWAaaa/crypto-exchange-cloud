package com.cex.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.activity.domain.entity.Activity;
import com.cex.activity.domain.entity.RedPacket;
import com.cex.activity.mapper.ActivityMapper;
import com.cex.activity.mapper.RedPacketMapper;
import com.cex.activity.service.ActivityService;
import com.cex.common.core.domain.Result;
import com.cex.common.core.util.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 活动服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final RedPacketMapper redPacketMapper;
    private final RedisLockUtil redisLockUtil;
    
    @Autowired
    private ActivityServiceImpl self;  // 自注入，解决事务问题

    @Override
    public Result<List<Activity>> getActivityList(Integer type, Integer status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getEnabled, 1);
        if (type != null) {
            wrapper.eq(Activity::getType, type);
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        
        List<Activity> activities = activityMapper.selectList(wrapper);
        return Result.success(activities);
    }

    @Override
    public Result<Activity> getActivityDetail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    @Override
    public Result<Void> participateActivity(Long userId, Long activityId, String amount) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        
        if (activity.getStatus() != 1) {
            return Result.error("活动未开始或已结束");
        }
        
        // TODO: 实现具体的参与逻辑
        log.info("用户{}参与活动{}，金额{}", userId, activityId, amount);
        
        return Result.success();
    }

    @Override
    public Result<String> createRedPacket(Long userId, String coin, String totalAmount, 
                                         Integer totalCount, Integer type, String blessing) {
        RedPacket redPacket = new RedPacket();
        redPacket.setPacketId(UUID.randomUUID().toString().replace("-", ""));
        redPacket.setSenderId(userId);
        redPacket.setType(type);
        redPacket.setCoin(coin);
        redPacket.setTotalAmount(new BigDecimal(totalAmount));
        redPacket.setTotalCount(totalCount);
        redPacket.setBlessing(blessing);
        redPacket.setStatus(0);
        
        redPacketMapper.insert(redPacket);
        
        log.info("用户{}创建红包{}，币种{}，总金额{}", userId, redPacket.getPacketId(), coin, totalAmount);
        
        return Result.success(redPacket.getPacketId());
    }

    /**
     * 抢红包（带锁和事务）
     * 
     * 使用自注入解决本地调用事务问题
     * 使用分布式锁保证并发安全
     * 使用事务确保数据一致性
     */
    @Override
    public Result<String> grabRedPacket(Long userId, String packetId) {
        String lockKey = "redpacket:grab:" + packetId;
        
        // 自注入调用，确保事务生效 ✅
        return redisLockUtil.executeWithLock(lockKey, () -> {
            return self.doGrabRedPacket(userId, packetId);
        }, 10, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    /**
     * 执行抢红包业务逻辑（带事务）
     * 
     * 使用自注入调用，确保@Transactional生效
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> doGrabRedPacket(Long userId, String packetId) {
        // 使用数据库行锁，确保并发安全
        RedPacket redPacket = redPacketMapper.selectOne(
            new LambdaQueryWrapper<RedPacket>()
                .eq(RedPacket::getPacketId, packetId)
                .last("FOR UPDATE")  // 行锁
        );
        
        if (redPacket == null) {
            return Result.error("红包不存在");
        }
        
        if (redPacket.getStatus() != 1) {
            return Result.error("红包未发送或已领完");
        }
        
        // 检查是否还有剩余
        if (redPacket.getReceivedCount() >= redPacket.getTotalCount()) {
            return Result.error("红包已被抢完");
        }
        
        // TODO: 实现抢红包具体逻辑
        // 1. 计算分配给用户的金额
        // 2. 更新红包信息
        // 3. 更新用户余额
        
        log.info("用户{}成功抢到红包{}", userId, packetId);
        
        return Result.success("抢红包成功");
    }

    @Override
    public Result<RedPacket> getRedPacketDetail(String packetId) {
        RedPacket redPacket = redPacketMapper.selectOne(
            new LambdaQueryWrapper<RedPacket>().eq(RedPacket::getPacketId, packetId)
        );
        
        if (redPacket == null) {
            return Result.error("红包不存在");
        }
        
        return Result.success(redPacket);
    }

    @Override
    public Result<List<RedPacket>> getUserRedPackets(Long userId) {
        LambdaQueryWrapper<RedPacket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RedPacket::getSenderId, userId);
        wrapper.orderByDesc(RedPacket::getCreateTime);
        
        List<RedPacket> redPackets = redPacketMapper.selectList(wrapper);
        return Result.success(redPackets);
    }
}
