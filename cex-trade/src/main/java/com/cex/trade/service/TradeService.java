package com.cex.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cex.trade.domain.dto.PlaceOrderDTO;
import com.cex.trade.domain.entity.TradeOrder;
import com.cex.trade.domain.entity.TradeSymbol;

import java.util.List;

/**
 * 交易服务接口
 * 
 * @author cex
 */
public interface TradeService extends IService<TradeOrder> {

    /**
     * 下单
     */
    String placeOrder(Long userId, PlaceOrderDTO placeOrderDTO);

    /**
     * 撤销订单
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 获取用户订单列表
     */
    List<TradeOrder> getUserOrders(Long userId, String symbol, Integer status, Integer page, Integer size);

    /**
     * 获取订单详情
     */
    TradeOrder getOrderDetail(Long userId, String orderNo);

    /**
     * 获取交易对列表
     */
    List<TradeSymbol> getSymbolList();

    /**
     * 获取交易对详情
     */
    TradeSymbol getSymbolDetail(String symbol);

    /**
     * 获取K线数据
     */
    List<Object> getKlineData(String symbol, String period, Integer limit);

    /**
     * 获取深度数据
     */
    Object getDepthData(String symbol, Integer limit);

    /**
     * 获取最新成交记录
     */
    List<Object> getTradeRecords(String symbol, Integer limit);
}
