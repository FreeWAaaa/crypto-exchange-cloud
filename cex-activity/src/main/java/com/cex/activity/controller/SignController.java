package com.cex.activity.controller;

import com.cex.activity.domain.entity.SignActivity;
import com.cex.activity.domain.entity.SignRecord;
import com.cex.activity.service.SignService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/activity/sign")
@RequiredArgsConstructor
public class SignController {
    
    private final SignService signService;
    
    /**
     * 用户签到
     */
    @PostMapping("/in")
    public Result<Void> signIn(@RequestParam Long userId) {
        signService.signIn(userId);
        return Result.success();
    }
    
    /**
     * 查询当前签到活动
     */
    @GetMapping("/current")
    public Result<SignActivity> getCurrentActivity() {
        SignActivity activity = signService.getCurrentActivity();
        return Result.success(activity);
    }
    
    /**
     * 查询用户签到记录
     */
    @GetMapping("/records")
    public Result<List<SignRecord>> getUserSignRecords(@RequestParam Long userId) {
        List<SignRecord> records = signService.getUserSignRecords(userId);
        return Result.success(records);
    }
    
    /**
     * 查询签到状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getSignStatus(@RequestParam Long userId) {
        Map<String, Object> status = new HashMap<>();
        status.put("isTodaySigned", signService.isTodaySigned(userId));
        status.put("consecutiveDays", signService.getConsecutiveDays(userId));
        
        return Result.success(status);
    }
}

