package com.cex.matching.service.impl;

import cn.hutool.core.util.IdUtil;
import com.cex.common.dto.OrderDTO;
import com.cex.matching.domain.entity.OrderBook;
import com.cex.matching.domain.entity.TradeRecord;
import com.cex.matching.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.ZoneId;

/**
 * 撮合引擎服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
public class MatchingServiceImpl implements MatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 撮合引擎运行状态
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // 订单簿：symbol -> side -> price -> orders
    private final Map<String, Map<Integer, ConcurrentSkipListMap<BigDecimal, List<OrderDTO>>>> orderBooks = new ConcurrentHashMap<>();
    
    // 成交记录缓存
    private final Map<String, List<TradeRecord>> tradeRecords = new ConcurrentHashMap<>();

    public MatchingServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addOrder(OrderDTO order) {
        String symbol = order.getSymbol();
        
        // 初始化订单簿
        orderBooks.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(order.getSide(), k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(order.getPrice(), k -> new ArrayList<>())
                .add(order);

        log.info("订单添加到撮合引擎: orderNo={}, symbol={}, side={}, price={}, amount={}", 
                order.getOrderNo(), symbol, order.getSide(), order.getPrice(), order.getAmount());

        // 执行撮合
        matchOrders(symbol);
    }

    @Override
    public void cancelOrder(String orderNo) {
        // 从所有订单簿中查找并移除订单
        for (Map.Entry<String, Map<Integer, ConcurrentSkipListMap<BigDecimal, List<OrderDTO>>>> symbolEntry : orderBooks.entrySet()) {
            for (Map.Entry<Integer, ConcurrentSkipListMap<BigDecimal, List<OrderDTO>>> sideEntry : symbolEntry.getValue().entrySet()) {
                for (Map.Entry<BigDecimal, List<OrderDTO>> priceEntry : sideEntry.getValue().entrySet()) {
                    List<OrderDTO> orders = priceEntry.getValue();
                    orders.removeIf(order -> order.getOrderNo().equals(orderNo));
                    
                    if (orders.isEmpty()) {
                        sideEntry.getValue().remove(priceEntry.getKey());
                    }
                    
                    if (!orders.isEmpty()) {
                        log.info("订单撤销成功: orderNo={}", orderNo);
                        return;
                    }
                }
            }
        }
        
        log.warn("未找到要撤销的订单: orderNo={}", orderNo);
    }

    @Override
    public Map<String, List<OrderBook>> getOrderBook(String symbol) {
        Map<String, List<OrderBook>> result = new HashMap<>();
        
        Map<Integer, ConcurrentSkipListMap<BigDecimal, List<OrderDTO>>> symbolOrderBook = orderBooks.get(symbol);
        if (symbolOrderBook == null) {
            return result;
        }

        // 买单（按价格降序）
        List<OrderBook> buyOrders = new ArrayList<>();
        ConcurrentSkipListMap<BigDecimal, List<OrderDTO>> buySide = symbolOrderBook.get(1);
        if (buySide != null) {
            buySide.descendingMap().forEach((price, orders) -> {
                BigDecimal totalAmount = orders.stream()
                        .map(OrderDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                OrderBook orderBook = new OrderBook();
                orderBook.setSymbol(symbol);
                orderBook.setSide(1);
                orderBook.setPrice(price);
                orderBook.setAmount(totalAmount);
                orderBook.setOrderCount(orders.size());
                orderBook.setUpdateTime(new java.util.Date());
                
                buyOrders.add(orderBook);
            });
        }

        // 卖单（按价格升序）
        List<OrderBook> sellOrders = new ArrayList<>();
        ConcurrentSkipListMap<BigDecimal, List<OrderDTO>> sellSide = symbolOrderBook.get(2);
        if (sellSide != null) {
            sellSide.forEach((price, orders) -> {
                BigDecimal totalAmount = orders.stream()
                        .map(OrderDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                OrderBook orderBook = new OrderBook();
                orderBook.setSymbol(symbol);
                orderBook.setSide(2);
                orderBook.setPrice(price);
                orderBook.setAmount(totalAmount);
                orderBook.setOrderCount(orders.size());
                orderBook.setUpdateTime(new java.util.Date());
                
                sellOrders.add(orderBook);
            });
        }

        result.put("bids", buyOrders);
        result.put("asks", sellOrders);
        
        return result;
    }

    @Override
    public List<TradeRecord> getLatestTrades(String symbol, Integer limit) {
        List<TradeRecord> records = tradeRecords.getOrDefault(symbol, new ArrayList<>());
        return records.stream()
                .sorted((a, b) -> b.getTradeTime().compareTo(a.getTradeTime()))
                .limit(limit)
                .toList();
    }

    @Override
    public List<Object> getKlineData(String symbol, String period, Integer limit) {
        // TODO: 实现K线数据计算
        return new ArrayList<>();
    }

    @Override
    public void startMatchingEngine() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("撮合引擎启动成功");
        } else {
            log.warn("撮合引擎已在运行中");
        }
    }

    @Override
    public void stopMatchingEngine() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("撮合引擎停止成功");
        } else {
            log.warn("撮合引擎未在运行");
        }
    }

    @Override
    public boolean isMatchingEngineRunning() {
        return isRunning.get();
    }

    /**
     * 执行撮合
     */
    private void matchOrders(String symbol) {
        Map<Integer, ConcurrentSkipListMap<BigDecimal, List<OrderDTO>>> symbolOrderBook = orderBooks.get(symbol);
        if (symbolOrderBook == null) {
            return;
        }

        ConcurrentSkipListMap<BigDecimal, List<OrderDTO>> buySide = symbolOrderBook.get(1);
        ConcurrentSkipListMap<BigDecimal, List<OrderDTO>> sellSide = symbolOrderBook.get(2);

        if (buySide == null || sellSide == null || buySide.isEmpty() || sellSide.isEmpty()) {
            return;
        }

        // 获取最高买价和最低卖价
        BigDecimal highestBuyPrice = buySide.lastKey();
        BigDecimal lowestSellPrice = sellSide.firstKey();

        // 检查是否可以撮合
        if (highestBuyPrice.compareTo(lowestSellPrice) >= 0) {
            // 执行撮合
            List<OrderDTO> buyOrders = buySide.get(highestBuyPrice);
            List<OrderDTO> sellOrders = sellSide.get(lowestSellPrice);

            if (buyOrders != null && !buyOrders.isEmpty() && sellOrders != null && !sellOrders.isEmpty()) {
                executeMatch(buyOrders, sellOrders, symbol);
            }
        }
    }

    /**
     * 执行撮合交易
     */
    private void executeMatch(List<OrderDTO> buyOrders, List<OrderDTO> sellOrders, String symbol) {
        OrderDTO buyOrder = buyOrders.get(0);
        OrderDTO sellOrder = sellOrders.get(0);

        // 计算成交数量
        BigDecimal buyRemaining = buyOrder.getAmount().subtract(buyOrder.getFilledAmount());
        BigDecimal sellRemaining = sellOrder.getAmount().subtract(sellOrder.getFilledAmount());
        BigDecimal tradeAmount = buyRemaining.min(sellRemaining);

        // 计算成交价格（使用卖单价格）
        BigDecimal tradePrice = sellOrder.getPrice();
        BigDecimal tradeMoney = tradeAmount.multiply(tradePrice);

        // 创建成交记录
        TradeRecord tradeRecord = new TradeRecord();
        tradeRecord.setTradeId(IdUtil.getSnowflakeNextIdStr());
        tradeRecord.setSymbol(symbol);
        tradeRecord.setBuyOrderNo(buyOrder.getOrderNo());
        tradeRecord.setSellOrderNo(sellOrder.getOrderNo());
        tradeRecord.setBuyUserId(buyOrder.getUserId());
        tradeRecord.setSellUserId(sellOrder.getUserId());
        tradeRecord.setPrice(tradePrice);
        tradeRecord.setAmount(tradeAmount);
        tradeRecord.setMoney(tradeMoney);
        tradeRecord.setBuyFee(tradeMoney.multiply(buyOrder.getFeeRate() != null ? buyOrder.getFeeRate() : BigDecimal.ZERO));
        tradeRecord.setSellFee(tradeAmount.multiply(sellOrder.getFeeRate() != null ? sellOrder.getFeeRate() : BigDecimal.ZERO));
        tradeRecord.setTradeTime(new java.util.Date());

        // 更新订单状态
        buyOrder.setFilledAmount(buyOrder.getFilledAmount().add(tradeAmount));
        buyOrder.setFilledMoney(buyOrder.getFilledMoney().add(tradeMoney));
        if (buyOrder.getFilledAmount().compareTo(buyOrder.getAmount()) >= 0) {
            buyOrder.setStatus(3); // 完全成交
            buyOrders.remove(buyOrder);
        } else {
            buyOrder.setStatus(2); // 部分成交
        }

        sellOrder.setFilledAmount(sellOrder.getFilledAmount().add(tradeAmount));
        sellOrder.setFilledMoney(sellOrder.getFilledMoney().add(tradeMoney));
        if (sellOrder.getFilledAmount().compareTo(sellOrder.getAmount()) >= 0) {
            sellOrder.setStatus(3); // 完全成交
            sellOrders.remove(sellOrder);
        } else {
            sellOrder.setStatus(2); // 部分成交
        }

        // 添加到成交记录
        tradeRecords.computeIfAbsent(symbol, k -> new ArrayList<>()).add(tradeRecord);

        // 缓存到Redis
        String key = "trade:records:" + symbol;
        redisTemplate.opsForList().leftPush(key, tradeRecord);
        redisTemplate.opsForList().trim(key, 0, 999); // 保留最近1000条

        log.info("撮合交易成功: tradeId={}, symbol={}, price={}, amount={}, buyOrder={}, sellOrder={}", 
                tradeRecord.getTradeId(), symbol, tradePrice, tradeAmount, buyOrder.getOrderNo(), sellOrder.getOrderNo());

        // 如果还有剩余订单，继续撮合
        if (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            matchOrders(symbol);
        }
    }
}
