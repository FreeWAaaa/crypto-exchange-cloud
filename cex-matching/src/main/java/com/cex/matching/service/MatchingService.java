package com.cex.matching.service;

import com.cex.common.dto.OrderDTO;
import com.cex.matching.domain.entity.OrderBook;
import com.cex.matching.domain.entity.TradeRecord;

import java.util.List;
import java.util.Map;

/**
 * 撮合引擎服务接口
 * 
 * @author cex
 */
public interface MatchingService {

    /**
     * 添加订单到撮合引擎
     */
    void addOrder(OrderDTO order);

    /**
     * 撤销订单
     */
    void cancelOrder(String orderNo);

    /**
     * 获取订单簿
     */
    Map<String, List<OrderBook>> getOrderBook(String symbol);

    /**
     * 获取最新成交记录
     */
    List<TradeRecord> getLatestTrades(String symbol, Integer limit);

    /**
     * 获取K线数据
     */
    List<Object> getKlineData(String symbol, String period, Integer limit);

    /**
     * 启动撮合引擎
     */
    void startMatchingEngine();

    /**
     * 停止撮合引擎
     */
    void stopMatchingEngine();

    /**
     * 获取撮合引擎状态
     */
    boolean isMatchingEngineRunning();
}
