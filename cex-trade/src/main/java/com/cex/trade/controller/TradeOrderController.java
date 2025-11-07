package com.cex.trade.controller;

import com.cex.common.core.domain.Result;
import com.cex.trade.domain.dto.PlaceOrderDTO;
import com.cex.trade.domain.entity.TradeOrder;
import com.cex.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易订单Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/trade/order")
public class TradeOrderController {

    @Autowired
    private TradeOrderService orderService;

    /**
     * 下单
     */
    @PostMapping("/place")
    public Result<String> placeOrder(
            @RequestParam Long userId,
            @Validated @RequestBody PlaceOrderDTO dto) {
        
        log.info("下单请求：userId={}, dto={}", userId, dto);
        
        String orderNo = orderService.placeOrder(
            userId, 
            dto.getSymbol(), 
            dto.getOrderType(), 
            dto.getSide(), 
            dto.getPrice(), 
            dto.getAmount(), 
            dto.getClientOrderId()
        );
        
        return Result.success(orderNo, "下单成功");
    }

    /**
     * 撤单
     */
    @PostMapping("/cancel")
    public Result<Void> cancelOrder(
            @RequestParam Long userId,
            @RequestParam String orderNo) {
        
        log.info("撤单请求：userId={}, orderNo={}", userId, orderNo);
        
        orderService.cancelOrder(userId, orderNo);
        
        return Result.success("撤单成功");
    }

    /**
     * 查询当前委托
     */
    @GetMapping("/current")
    public Result<List<TradeOrder>> getCurrentOrders(@RequestParam Long userId) {
        List<TradeOrder> orders = orderService.getCurrentOrders(userId);
        return Result.success(orders, "查询成功");
    }

    /**
     * 查询指定交易对的当前委托
     */
    @GetMapping("/current/{symbol}")
    public Result<List<TradeOrder>> getCurrentOrdersBySymbol(
            @RequestParam Long userId,
            @PathVariable String symbol) {
        
        List<TradeOrder> orders = orderService.getCurrentOrdersBySymbol(userId, symbol);
        return Result.success(orders, "查询成功");
    }

    /**
     * 查询历史委托
     */
    @GetMapping("/history")
    public Result<List<TradeOrder>> getHistoryOrders(@RequestParam Long userId) {
        List<TradeOrder> orders = orderService.getHistoryOrders(userId);
        return Result.success(orders, "查询成功");
    }

    /**
     * 查询指定交易对的历史委托
     */
    @GetMapping("/history/{symbol}")
    public Result<List<TradeOrder>> getHistoryOrdersBySymbol(
            @RequestParam Long userId,
            @PathVariable String symbol) {
        
        List<TradeOrder> orders = orderService.getHistoryOrdersBySymbol(userId, symbol);
        return Result.success(orders, "查询成功");
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<TradeOrder> getOrderDetail(@PathVariable String orderNo) {
        TradeOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.success(order, "查询成功");
    }
}

