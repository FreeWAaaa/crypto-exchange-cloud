package com.cex.admin.controller;

import com.cex.admin.client.TradeFeignClient;
import com.cex.admin.client.UserFeignClient;
import com.cex.admin.client.WalletFeignClient;
import com.cex.admin.service.StatisticsService;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@Api(tags = "数据统计")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final UserFeignClient userFeignClient;
    private final WalletFeignClient walletFeignClient;
    private final TradeFeignClient tradeFeignClient;
    
    /**
     * 获取首页统计数据
     */
    @GetMapping("/dashboard")
    @ApiOperation("获取首页统计数据")
    public Result<Map<String, Object>> getDashboardStats() {
        log.info("管理员查询首页统计数据");
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }
    
    /**
     * 获取用户统计
     */
    @GetMapping("/user")
    @ApiOperation("获取用户统计")
    public Result<Map<String, Object>> getUserStats(@RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        log.info("管理员查询用户统计，startDate：{}，endDate：{}", startDate, endDate);
        Map<String, Object> stats = statisticsService.getUserStats(startDate, endDate);
        return Result.success(stats);
    }
    
    /**
     * 获取交易统计
     */
    @GetMapping("/trade")
    @ApiOperation("获取交易统计")
    public Result<Map<String, Object>> getTradeStats(@RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        log.info("管理员查询交易统计，startDate：{}，endDate：{}", startDate, endDate);
        Map<String, Object> stats = statisticsService.getTradeStats(startDate, endDate);
        return Result.success(stats);
    }
    
    /**
     * 获取财务统计
     */
    @GetMapping("/finance")
    @ApiOperation("获取财务统计")
    public Result<Map<String, Object>> getFinanceStats(@RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        log.info("管理员查询财务统计，startDate：{}，endDate：{}", startDate, endDate);
        Map<String, Object> stats = statisticsService.getFinanceStats(startDate, endDate);
        return Result.success(stats);
    }
}

