package com.cex.trade.service;

import com.cex.trade.domain.entity.TradeOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易订单服务接口
 * 
 * @author cex
 */
public interface TradeOrderService {

    /**
     * 下单
     * 
     * @param userId 用户ID
     * @param symbol 交易对
     * @param orderType 订单类型（1限价 2市价）
     * @param side 方向（1买入 2卖出）
     * @param price 价格（限价单必填）
     * @param amount 数量
     * @param clientOrderId 客户端订单ID
     * @return 订单号
     */
    String placeOrder(Long userId, String symbol, Integer orderType, Integer side, 
                     BigDecimal price, BigDecimal amount, String clientOrderId);

    /**
     * 撤单
     * 
     * @param userId 用户ID
     * @param orderNo 订单号
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 查询用户当前委托
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<TradeOrder> getCurrentOrders(Long userId);

    /**
     * 查询用户指定交易对的当前委托
     * 
     * @param userId 用户ID
     * @param symbol 交易对
     * @return 订单列表
     */
    List<TradeOrder> getCurrentOrdersBySymbol(Long userId, String symbol);

    /**
     * 查询用户历史委托
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<TradeOrder> getHistoryOrders(Long userId);

    /**
     * 查询用户指定交易对的历史委托
     * 
     * @param userId 用户ID
     * @param symbol 交易对
     * @return 订单列表
     */
    List<TradeOrder> getHistoryOrdersBySymbol(Long userId, String symbol);

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 订单对象
     */
    TradeOrder getByOrderNo(String orderNo);

    /**
     * 更新订单成交信息（撮合引擎回调）
     * 
     * @param orderNo 订单号
     * @param filledAmount 成交数量
     * @param filledMoney 成交金额
     * @param fee 手续费
     * @param status 订单状态
     */
    void updateOrderFilled(String orderNo, BigDecimal filledAmount, BigDecimal filledMoney, 
                          BigDecimal fee, Integer status);
}

