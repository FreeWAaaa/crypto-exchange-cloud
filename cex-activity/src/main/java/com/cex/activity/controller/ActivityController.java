package com.cex.activity.controller;

import com.cex.activity.domain.entity.Activity;
import com.cex.activity.domain.entity.RedPacket;
import com.cex.activity.service.ActivityService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 * 
 * @author cex
 */
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 获取活动列表
     */
    @GetMapping("/list")
    public Result<List<Activity>> getActivityList(@RequestParam(required = false) Integer type,
                                                  @RequestParam(required = false) Integer status) {
        return activityService.getActivityList(type, status);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/detail/{id}")
    public Result<Activity> getActivityDetail(@PathVariable Long id) {
        return activityService.getActivityDetail(id);
    }

    /**
     * 参与活动
     */
    @PostMapping("/participate")
    public Result<Void> participateActivity(@RequestParam Long userId,
                                           @RequestParam Long activityId,
                                           @RequestParam String amount) {
        return activityService.participateActivity(userId, activityId, amount);
    }

    /**
     * 创建红包
     */
    @PostMapping("/redpacket/create")
    public Result<String> createRedPacket(@RequestParam Long userId,
                                         @RequestParam String coin,
                                         @RequestParam String totalAmount,
                                         @RequestParam Integer totalCount,
                                         @RequestParam Integer type,
                                         @RequestParam(required = false) String blessing) {
        return activityService.createRedPacket(userId, coin, totalAmount, totalCount, type, blessing);
    }

    /**
     * 抢红包
     */
    @PostMapping("/redpacket/grab")
    public Result<String> grabRedPacket(@RequestParam Long userId,
                                       @RequestParam String packetId) {
        return activityService.grabRedPacket(userId, packetId);
    }

    /**
     * 获取红包详情
     */
    @GetMapping("/redpacket/detail/{packetId}")
    public Result<RedPacket> getRedPacketDetail(@PathVariable String packetId) {
        return activityService.getRedPacketDetail(packetId);
    }

    /**
     * 获取用户红包记录
     */
    @GetMapping("/redpacket/user/{userId}")
    public Result<List<RedPacket>> getUserRedPackets(@PathVariable Long userId) {
        return activityService.getUserRedPackets(userId);
    }
}
