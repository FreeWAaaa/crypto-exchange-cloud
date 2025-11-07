package com.cex.admin.controller;

import com.cex.admin.client.TradeFeignClient;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单管理控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Api(tags = "订单管理")
public class OrderManageController {
    
    private final TradeFeignClient tradeFeignClient;
    
    /**
     * 订单列表
     */
    @GetMapping("/list")
    @ApiOperation("查询订单列表")
    public Result<Object> getOrderList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询订单列表，参数：{}", params);
        return tradeFeignClient.getOrderList(params);
    }
    
    /**
     * 订单详情
     */
    @GetMapping("/detail/{orderNo}")
    @ApiOperation("查询订单详情")
    public Result<Object> getOrderDetail(@PathVariable String orderNo) {
        log.info("管理员查询订单详情，orderNo：{}", orderNo);
        return tradeFeignClient.getOrderDetail(orderNo);
    }
    
    /**
     * 成交记录列表
     */
    @GetMapping("/trade/list")
    @ApiOperation("查询成交记录列表")
    public Result<Object> getTradeList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询成交记录列表，参数：{}", params);
        return tradeFeignClient.getTradeList(params);
    }
    
    /**
     * 交易对列表
     */
    @GetMapping("/symbol/list")
    @ApiOperation("查询交易对列表")
    public Result<Object> getSymbolList() {
        log.info("管理员查询交易对列表");
        return tradeFeignClient.getSymbolList();
    }
    
    /**
     * 更新交易对配置
     */
    @PostMapping("/symbol/update")
    @ApiOperation("更新交易对配置")
    public Result<Void> updateSymbol(@RequestBody Map<String, Object> symbolData) {
        log.info("管理员更新交易对配置，数据：{}", symbolData);
        return tradeFeignClient.updateSymbol(symbolData);
    }
}

