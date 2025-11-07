package com.cex.activity.service;

import com.cex.activity.domain.entity.Activity;
import com.cex.activity.domain.entity.RedPacket;
import com.cex.common.core.domain.Result;

import java.util.List;

/**
 * 活动服务接口
 * 
 * @author cex
 */
public interface ActivityService {

    /**
     * 获取活动列表
     */
    Result<List<Activity>> getActivityList(Integer type, Integer status);

    /**
     * 获取活动详情
     */
    Result<Activity> getActivityDetail(Long id);

    /**
     * 参与活动
     */
    Result<Void> participateActivity(Long userId, Long activityId, String amount);

    /**
     * 创建红包
     */
    Result<String> createRedPacket(Long userId, String coin, String totalAmount, 
                                  Integer totalCount, Integer type, String blessing);

    /**
     * 抢红包
     */
    Result<String> grabRedPacket(Long userId, String packetId);

    /**
     * 获取红包详情
     */
    Result<RedPacket> getRedPacketDetail(String packetId);

    /**
     * 获取用户红包记录
     */
    Result<List<RedPacket>> getUserRedPackets(Long userId);
}
