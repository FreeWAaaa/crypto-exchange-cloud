package com.cex.trade.controller;

import com.cex.common.core.domain.Result;
import com.cex.trade.domain.dto.PlaceOrderDTO;
import com.cex.trade.domain.entity.TradeOrder;
import com.cex.trade.domain.entity.TradeSymbol;
import com.cex.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 交易控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
@Validated
public class TradeController {

    private final TradeService tradeService;

    /**
     * 下单
     */
    @PostMapping("/order/place")
    public Result<String> placeOrder(@RequestParam Long userId, @Valid @RequestBody PlaceOrderDTO placeOrderDTO) {
        String orderNo = tradeService.placeOrder(userId, placeOrderDTO);
        return Result.success("下单成功", orderNo);
    }

    /**
     * 撤销订单
     */
    @PostMapping("/order/cancel")
    public Result<Void> cancelOrder(@RequestParam Long userId, @RequestParam String orderNo) {
        tradeService.cancelOrder(userId, orderNo);
        return Result.success();
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/order/list")
    public Result<List<TradeOrder>> getUserOrders(@RequestParam Long userId,
                                                  @RequestParam(required = false) String symbol,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        List<TradeOrder> orders = tradeService.getUserOrders(userId, symbol, status, page, size);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/order/detail")
    public Result<TradeOrder> getOrderDetail(@RequestParam Long userId, @RequestParam String orderNo) {
        TradeOrder order = tradeService.getOrderDetail(userId, orderNo);
        return Result.success(order);
    }

    /**
     * 获取交易对列表
     */
    @GetMapping("/symbol/list")
    public Result<List<TradeSymbol>> getSymbolList() {
        List<TradeSymbol> symbols = tradeService.getSymbolList();
        return Result.success(symbols);
    }

    /**
     * 获取交易对详情
     */
    @GetMapping("/symbol/detail")
    public Result<TradeSymbol> getSymbolDetail(@RequestParam String symbol) {
        TradeSymbol symbolDetail = tradeService.getSymbolDetail(symbol);
        return Result.success(symbolDetail);
    }

    /**
     * 获取K线数据
     */
    @GetMapping("/kline")
    public Result<List<Object>> getKlineData(@RequestParam String symbol,
                                            @RequestParam(defaultValue = "1m") String period,
                                            @RequestParam(defaultValue = "100") Integer limit) {
        List<Object> klineData = tradeService.getKlineData(symbol, period, limit);
        return Result.success(klineData);
    }

    /**
     * 获取深度数据
     */
    @GetMapping("/depth")
    public Result<Object> getDepthData(@RequestParam String symbol,
                                      @RequestParam(defaultValue = "20") Integer limit) {
        Object depthData = tradeService.getDepthData(symbol, limit);
        return Result.success(depthData);
    }

    /**
     * 获取最新成交记录
     */
    @GetMapping("/records")
    public Result<List<Object>> getTradeRecords(@RequestParam String symbol,
                                               @RequestParam(defaultValue = "50") Integer limit) {
        List<Object> records = tradeService.getTradeRecords(symbol, limit);
        return Result.success(records);
    }
}
