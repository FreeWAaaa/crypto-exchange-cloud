package com.cex.matching.controller;

import com.cex.common.core.domain.Result;
import com.cex.common.dto.OrderDTO;
import com.cex.matching.domain.entity.OrderBook;
import com.cex.matching.domain.entity.TradeRecord;
import com.cex.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 撮合引擎控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@Validated
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 添加订单到撮合引擎
     */
    @PostMapping("/order/add")
    public Result<Void> addOrder(@Valid @RequestBody OrderDTO order) {
        matchingService.addOrder(order);
        return Result.success();
    }

    /**
     * 撤销订单
     */
    @PostMapping("/order/cancel")
    public Result<Void> cancelOrder(@RequestParam String orderNo) {
        matchingService.cancelOrder(orderNo);
        return Result.success();
    }

    /**
     * 获取订单簿
     */
    @GetMapping("/orderbook")
    public Result<Map<String, List<OrderBook>>> getOrderBook(@RequestParam String symbol) {
        Map<String, List<OrderBook>> orderBook = matchingService.getOrderBook(symbol);
        return Result.success(orderBook);
    }

    /**
     * 获取最新成交记录
     */
    @GetMapping("/trades")
    public Result<List<TradeRecord>> getLatestTrades(@RequestParam String symbol,
                                                    @RequestParam(defaultValue = "50") Integer limit) {
        List<TradeRecord> trades = matchingService.getLatestTrades(symbol, limit);
        return Result.success(trades);
    }

    /**
     * 获取K线数据
     */
    @GetMapping("/kline")
    public Result<List<Object>> getKlineData(@RequestParam String symbol,
                                            @RequestParam(defaultValue = "1m") String period,
                                            @RequestParam(defaultValue = "100") Integer limit) {
        List<Object> klineData = matchingService.getKlineData(symbol, period, limit);
        return Result.success(klineData);
    }

    /**
     * 启动撮合引擎
     */
    @PostMapping("/engine/start")
    public Result<Void> startMatchingEngine() {
        matchingService.startMatchingEngine();
        return Result.success();
    }

    /**
     * 停止撮合引擎
     */
    @PostMapping("/engine/stop")
    public Result<Void> stopMatchingEngine() {
        matchingService.stopMatchingEngine();
        return Result.success();
    }

    /**
     * 获取撮合引擎状态
     */
    @GetMapping("/engine/status")
    public Result<Boolean> getMatchingEngineStatus() {
        boolean isRunning = matchingService.isMatchingEngineRunning();
        return Result.success(isRunning);
    }
}
