package com.cex.admin.client;

import com.cex.common.core.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交易服务 Feign 客户端
 * 
 * @author cex
 */
@FeignClient(name = "cex-trade", path = "/api/trade")
public interface TradeFeignClient {
    
    /**
     * 查询订单列表
     */
    @GetMapping("/admin/order/list")
    Result<Object> getOrderList(@RequestParam Map<String, Object> params);
    
    /**
     * 查询订单详情
     */
    @GetMapping("/admin/order/detail/{orderNo}")
    Result<Object> getOrderDetail(@PathVariable("orderNo") String orderNo);
    
    /**
     * 查询交易对列表
     */
    @GetMapping("/admin/symbol/list")
    Result<Object> getSymbolList();
    
    /**
     * 更新交易对配置
     */
    @PostMapping("/admin/symbol/update")
    Result<Void> updateSymbol(@RequestBody Map<String, Object> symbolData);
    
    /**
     * 查询成交记录
     */
    @GetMapping("/admin/trade/list")
    Result<Object> getTradeList(@RequestParam Map<String, Object> params);
}

