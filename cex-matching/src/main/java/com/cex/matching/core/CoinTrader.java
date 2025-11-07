package com.cex.matching.core;

import com.alibaba.fastjson.JSON;
import com.cex.common.dto.OrderDTO;
import com.cex.common.dto.TradeRecordDTO;
import com.cex.common.enums.OrderDirection;
import com.cex.common.enums.OrderStatus;
import com.cex.common.enums.OrderType;
import com.cex.common.enums.PublishType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 币种交易器（核心撮合引擎）
 * 
 * 基于老项目CoinTrader的重构版
 * 
 * @author cex
 */
@Slf4j
@Component
public class CoinTrader {
    /** 交易对 */
    private final String symbol;
    
    /** 交易币种精度 */
    private int coinScale = 8;
    
    /** 计价币种精度 */
    private int baseCoinScale = 8;
    
    /** 买入限价订单队列（价格从高到低） */
    private final TreeMap<BigDecimal, MergeOrder> buyLimitPriceQueue;
    
    /** 卖出限价订单队列（价格从低到高） */
    private final TreeMap<BigDecimal, MergeOrder> sellLimitPriceQueue;
    
    /** 买入市价订单队列（按时间排序） */
    private final LinkedList<OrderDTO> buyMarketQueue;
    
    /** 卖出市价订单队列（按时间排序） */
    private final LinkedList<OrderDTO> sellMarketQueue;
    
    /** 卖盘盘口信息 */
    private final TradePlate sellTradePlate;
    
    /** 买盘盘口信息 */
    private final TradePlate buyTradePlate;
    
    /** 是否暂停交易 */
    private boolean tradingHalt = false;
    
    /** 是否就绪 */
    private boolean ready = false;
    
    /** 发行类型 */
    private PublishType publishType = PublishType.NONE;
    
    /** 清盘时间 */
    private String clearTime;
    
    /** 日期格式化器 */
    private final SimpleDateFormat dateTimeFormat;
    
    /** RocketMQ Stream Binding Source */
    private org.springframework.cloud.stream.function.StreamBridge streamBridge;
    
    public CoinTrader(String symbol) {
        this.symbol = symbol;
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 买单队列价格降序排列
        this.buyLimitPriceQueue = new TreeMap<>(Comparator.reverseOrder());
        // 卖单队列价格升序排列
        this.sellLimitPriceQueue = new TreeMap<>(Comparator.naturalOrder());
        this.buyMarketQueue = new LinkedList<>();
        this.sellMarketQueue = new LinkedList<>();
        this.sellTradePlate = new TradePlate(symbol, OrderDirection.SELL);
        this.buyTradePlate = new TradePlate(symbol, OrderDirection.BUY);
        
        log.info("初始化交易器: {}", symbol);
    }
    
    public void setStreamBridge(org.springframework.cloud.stream.function.StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    
    public void setCoinScale(int coinScale) {
        this.coinScale = coinScale;
    }
    
    public void setBaseCoinScale(int baseCoinScale) {
        this.baseCoinScale = baseCoinScale;
    }
    
    public void setPublishType(PublishType publishType) {
        this.publishType = publishType;
    }
    
    public void setClearTime(String clearTime) {
        this.clearTime = clearTime;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public boolean isTradingHalt() {
        return tradingHalt;
    }
    
    public void haltTrading() {
        this.tradingHalt = true;
    }
    
    public void resumeTrading() {
        this.tradingHalt = false;
    }
    
    /**
     * 添加限价订单到队列
     */
    public void addLimitPriceOrder(OrderDTO exchangeOrder) {
        if (exchangeOrder.getOrderType() != 1) {  // LIMIT_PRICE
            return;
        }
        
        TreeMap<BigDecimal, MergeOrder> list;
        if (exchangeOrder.getSide() == 1) {  // BUY
            list = buyLimitPriceQueue;
            buyTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(buyTradePlate);
            }
        } else {
            list = sellLimitPriceQueue;
            sellTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(sellTradePlate);
            }
        }
        
        synchronized (list) {
            MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
            if (mergeOrder == null) {
                mergeOrder = new MergeOrder();
                mergeOrder.add(exchangeOrder);
                list.put(exchangeOrder.getPrice(), mergeOrder);
            } else {
                mergeOrder.add(exchangeOrder);
            }
        }
    }
    
    /**
     * 添加市价订单到队列
     */
    public void addMarketPriceOrder(OrderDTO exchangeOrder) {
        if (exchangeOrder.getOrderType() != 2) {  // MARKET_PRICE
            return;
        }
        log.info("添加市价订单: {}", exchangeOrder.getOrderNo());
        
        LinkedList<OrderDTO> list = exchangeOrder.getSide() == 1 ? buyMarketQueue : sellMarketQueue;
        synchronized (list) {
            list.addLast(exchangeOrder);
        }
    }
    
    /**
     * 处理订单（撮合）
     */
    public void trade(OrderDTO exchangeOrder) {
        if (tradingHalt) {
            return;
        }
        
        if (!symbol.equalsIgnoreCase(exchangeOrder.getSymbol())) {
            log.info("不支持的交易对: {}", exchangeOrder.getSymbol());
            return;
        }
        
        if (exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0 
                || exchangeOrder.getAmount().subtract(exchangeOrder.getFilledAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        TreeMap<BigDecimal, MergeOrder> limitPriceOrderList;
        LinkedList<OrderDTO> marketPriceOrderList;
        
        if (exchangeOrder.getSide() == 1) {  // BUY
            limitPriceOrderList = sellLimitPriceQueue;
            marketPriceOrderList = sellMarketQueue;
        } else {  // SELL
            limitPriceOrderList = buyLimitPriceQueue;
            marketPriceOrderList = buyMarketQueue;
        }
        
        if (exchangeOrder.getOrderType() == 2) {  // MARKET_PRICE
            // 市价单与限价单撮合
            matchMarketPriceWithLPList(limitPriceOrderList, exchangeOrder);
        } else if (exchangeOrder.getOrderType() == 1) {  // LIMIT_PRICE
            // 限价单价格必须大于0
            if (exchangeOrder.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            
            // 分摊模式特殊处理
            if (publishType == PublishType.FENTAN && exchangeOrder.getSide() == 2) {  // SELL
                log.info("分摊卖单处理");
                try {
                    if (exchangeOrder.getCreateTime() != null 
                            && clearTime != null 
                            && exchangeOrder.getCreateTime() < dateTimeFormat.parse(clearTime).getTime()) {
                        log.info("分摊卖单处在结束时间与清盘时间内");
                        matchLimitPriceWithLPListByFENTAN(limitPriceOrderList, exchangeOrder, false);
                        return;
                    }
                } catch (ParseException e) {
                    log.error("解析清盘时间失败", e);
                }
            }
            
            // 先与限价单撮合
            matchLimitPriceWithLPList(limitPriceOrderList, exchangeOrder, false);
            
            // 如果还没交易完，与市价单撮合
            if (exchangeOrder.getAmount().compareTo(exchangeOrder.getFilledAmount()) > 0) {
                matchLimitPriceWithMPList(marketPriceOrderList, exchangeOrder);
            }
        }
    }
    
    /**
     * 限价单与限价单撮合
     */
    private void matchLimitPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, OrderDTO focusedOrder, boolean canEnterList) {
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();
        List<OrderDTO> completedOrders = new ArrayList<>();
        
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<OrderDTO> orderIterator = mergeOrder.iterator();
                
                // 买单需要匹配的价格不大于委托价，否则退出
                if (focusedOrder.getSide() == 1 && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) > 0) {
                    break;
                }
                // 卖单需要匹配的价格不小于委托价，否则退出
                if (focusedOrder.getSide() == 2 && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) < 0) {
                    break;
                }
                
                while (orderIterator.hasNext()) {
                    OrderDTO matchOrder = orderIterator.next();
                    // 处理撮合
                    TradeRecordDTO trade = processMatch(focusedOrder, matchOrder);
                    if (trade != null) {
                        exchangeTrades.add(trade);
                    }
                    
                    // 判断匹配单是否完成
                    if (matchOrder.getStatus() == 2) {  // COMPLETED
                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                    }
                    
                    // 判断交易单是否完成
                    if (focusedOrder.getStatus() == 2) {  // COMPLETED
                        completedOrders.add(focusedOrder);
                        exitLoop = true;
                        break;
                    }
                }
                
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }
        
        // 如果还没交易完，订单压入列表
        if (focusedOrder.getAmount().compareTo(focusedOrder.getFilledAmount()) > 0 && canEnterList) {
            addLimitPriceOrder(focusedOrder);
        }
        
        // 推送撮合结果
        handleExchangeTrade(exchangeTrades);
        
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getSide() == 1 ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }
    
    /**
     * 处理两个匹配的订单
     */
    private TradeRecordDTO processMatch(OrderDTO focusedOrder, OrderDTO matchOrder) {
        BigDecimal needAmount, dealPrice, availAmount;
        
        // 如果匹配单是限价单，则以其价格为成交价
        if (matchOrder.getOrderType() == 1) {  // LIMIT_PRICE
            dealPrice = matchOrder.getPrice();
        } else {
            dealPrice = focusedOrder.getPrice();
        }
        
        // 成交价必须大于0
        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        // 计算需要交易的数量
        BigDecimal focusedRemaining = focusedOrder.getAmount().subtract(focusedOrder.getFilledAmount());
        BigDecimal matchRemaining = matchOrder.getAmount().subtract(matchOrder.getFilledAmount());
        
        if (focusedOrder.getOrderType() == 2 && focusedOrder.getSide() == 1) {  // MARKET_PRICE BUY
            needAmount = matchRemaining;
        } else {
            needAmount = focusedRemaining;
        }
        
        // 可用数量（对手单剩余）
        availAmount = matchRemaining;
        
        // 计算成交量
        BigDecimal tradedAmount = (availAmount.compareTo(needAmount) >= 0 ? needAmount : availAmount);
        
        // 如果成交量为0，退出
        if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        // 计算成交额
        BigDecimal turnover = tradedAmount.multiply(dealPrice);
        
        // 更新订单状态
        matchOrder.setFilledAmount(matchOrder.getFilledAmount().add(tradedAmount));
        matchOrder.setFilledMoney(matchOrder.getFilledMoney().add(turnover));
        focusedOrder.setFilledAmount(focusedOrder.getFilledAmount().add(tradedAmount));
        focusedOrder.setFilledMoney(focusedOrder.getFilledMoney().add(turnover));
        
        // 判断是否完成
        if (matchOrder.getFilledAmount().compareTo(matchOrder.getAmount()) >= 0) {
            matchOrder.setStatus(2);  // COMPLETED
        } else {
            matchOrder.setStatus(1);  // PARTIAL
        }
        
        if (focusedOrder.getFilledAmount().compareTo(focusedOrder.getAmount()) >= 0) {
            focusedOrder.setStatus(2);  // COMPLETED
        } else {
            focusedOrder.setStatus(1);  // PARTIAL
        }
        
        // 创建成交记录
        TradeRecordDTO tradeRecord = new TradeRecordDTO();
        tradeRecord.setSymbol(symbol);
        tradeRecord.setPrice(dealPrice);
        tradeRecord.setAmount(tradedAmount);
        
        if (focusedOrder.getSide() == 1) {  // BUY
            tradeRecord.setBuyOrderNo(focusedOrder.getOrderNo());
            tradeRecord.setSellOrderNo(matchOrder.getOrderNo());
            tradeRecord.setBuyUserId(focusedOrder.getUserId());
            tradeRecord.setSellUserId(matchOrder.getUserId());
        } else {  // SELL
            tradeRecord.setBuyOrderNo(matchOrder.getOrderNo());
            tradeRecord.setSellOrderNo(focusedOrder.getOrderNo());
            tradeRecord.setBuyUserId(matchOrder.getUserId());
            tradeRecord.setSellUserId(focusedOrder.getUserId());
        }
        
        tradeRecord.setMoney(turnover);
        tradeRecord.setTradeTime(new java.util.Date());
        
        log.info("撮合成功: price={}, amount={}", dealPrice, tradedAmount);
        
        return tradeRecord;
    }
    
    /**
     * 市价单与限价单撮合
     */
    private void matchMarketPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, OrderDTO focusedOrder) {
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();
        List<OrderDTO> completedOrders = new ArrayList<>();
        
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<OrderDTO> orderIterator = mergeOrder.iterator();
                
                while (orderIterator.hasNext()) {
                    OrderDTO matchOrder = orderIterator.next();
                    TradeRecordDTO trade = processMatch(focusedOrder, matchOrder);
                    if (trade != null) {
                        exchangeTrades.add(trade);
                    }
                    
                    if (matchOrder.getStatus() == 2) {  // COMPLETED
                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                    }
                    
                    if (focusedOrder.getStatus() == 2) {  // COMPLETED
                        completedOrders.add(focusedOrder);
                        exitLoop = true;
                        break;
                    }
                }
                
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }
        
        // 如果还没交易完，订单压入列表
        BigDecimal remainingAmount = focusedOrder.getAmount().subtract(focusedOrder.getFilledAmount());
        if ((focusedOrder.getSide() == 2 && remainingAmount.compareTo(BigDecimal.ZERO) > 0)
                || (focusedOrder.getSide() == 1 && focusedOrder.getFilledMoney().compareTo(focusedOrder.getAmount()) < 0)) {
            addMarketPriceOrder(focusedOrder);
        }
        
        handleExchangeTrade(exchangeTrades);
        
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getSide() == 1 ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }
    
    /**
     * 限价单与市价单撮合
     */
    private void matchLimitPriceWithMPList(LinkedList<OrderDTO> mpList, OrderDTO focusedOrder) {
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();
        List<OrderDTO> completedOrders = new ArrayList<>();
        
        synchronized (mpList) {
            Iterator<OrderDTO> iterator = mpList.iterator();
            while (iterator.hasNext()) {
                OrderDTO matchOrder = iterator.next();
                TradeRecordDTO trade = processMatch(focusedOrder, matchOrder);
                if (trade != null) {
                    exchangeTrades.add(trade);
                }
                
                if (matchOrder.getStatus() == 2) {  // COMPLETED
                    iterator.remove();
                    completedOrders.add(matchOrder);
                }
                
                if (focusedOrder.getStatus() == 2) {  // COMPLETED
                    completedOrders.add(focusedOrder);
                    break;
                }
            }
        }
        
        // 如果还没交易完，订单压入列表
        if (focusedOrder.getAmount().compareTo(focusedOrder.getFilledAmount()) > 0) {
            addLimitPriceOrder(focusedOrder);
        }
        
        handleExchangeTrade(exchangeTrades);
        orderCompleted(completedOrders);
    }
    
    /**
     * 分摊模式撮合
     * 用于抢购/分摊活动，按比例分配成交量
     */
    private void matchLimitPriceWithLPListByFENTAN(TreeMap<BigDecimal, MergeOrder> lpList, OrderDTO focusedOrder, boolean canEnterList) {
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();
        List<OrderDTO> completedOrders = new ArrayList<>();
        
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            
            // 计算总量（用于分摊比例）
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Map.Entry<BigDecimal, MergeOrder> entry : lpList.entrySet()) {
                totalAmount = totalAmount.add(entry.getValue().getTotalAmount());
            }
            
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<OrderDTO> orderIterator = mergeOrder.iterator();
                
                // 买入单需要匹配的价格不大于委托价
                if (focusedOrder.getSide() == 1 && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) > 0) {
                    break;
                }
                // 卖出单需要匹配的价格不小于委托价
                if (focusedOrder.getSide() == 2 && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) < 0) {
                    break;
                }
                
                while (orderIterator.hasNext()) {
                    OrderDTO matchOrder = orderIterator.next();
                    
                    // 计算分摊成交量 = 发行总量 * 匹配单数量占比
                    BigDecimal ratio = matchOrder.getAmount().divide(totalAmount, 8, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal tradedAmount = focusedOrder.getAmount().multiply(ratio).setScale(8, BigDecimal.ROUND_HALF_DOWN);
                    
                    if (tradedAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal dealPrice = matchOrder.getPrice();
                        BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(8, BigDecimal.ROUND_HALF_DOWN);
                        
                        // 更新订单
                        matchOrder.setFilledAmount(matchOrder.getFilledAmount().add(tradedAmount));
                        matchOrder.setFilledMoney(matchOrder.getFilledMoney().add(turnover));
                        focusedOrder.setFilledAmount(focusedOrder.getFilledAmount().add(tradedAmount));
                        focusedOrder.setFilledMoney(focusedOrder.getFilledMoney().add(turnover));
                        
                        // 创建成交记录
                        TradeRecordDTO tradeRecord = new TradeRecordDTO();
                        tradeRecord.setSymbol(symbol);
                        tradeRecord.setPrice(dealPrice);
                        tradeRecord.setAmount(tradedAmount);
                        tradeRecord.setMoney(turnover);
                        
                        if (focusedOrder.getSide() == 1) {
                            tradeRecord.setBuyOrderNo(focusedOrder.getOrderNo());
                            tradeRecord.setSellOrderNo(matchOrder.getOrderNo());
                            tradeRecord.setBuyUserId(focusedOrder.getUserId());
                            tradeRecord.setSellUserId(matchOrder.getUserId());
                        } else {
                            tradeRecord.setBuyOrderNo(matchOrder.getOrderNo());
                            tradeRecord.setSellOrderNo(focusedOrder.getOrderNo());
                            tradeRecord.setBuyUserId(matchOrder.getUserId());
                            tradeRecord.setSellUserId(focusedOrder.getUserId());
                        }
                        
                        tradeRecord.setTradeTime(new java.util.Date());
                        exchangeTrades.add(tradeRecord);
                        
                        // 判断是否完成
                        if (matchOrder.getFilledAmount().compareTo(matchOrder.getAmount()) >= 0) {
                            matchOrder.setStatus(2);
                            orderIterator.remove();
                            completedOrders.add(matchOrder);
                        } else {
                            matchOrder.setStatus(1);
                        }
                    }
                    
                    // 判断焦点订单是否完成
                    if (focusedOrder.getFilledAmount().compareTo(focusedOrder.getAmount()) >= 0) {
                        focusedOrder.setStatus(2);
                        completedOrders.add(focusedOrder);
                        exitLoop = true;
                        break;
                    }
                }
                
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }
        
        // 处理成交结果
        handleExchangeTrade(exchangeTrades);
        
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getSide() == 1 ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }
    
    /**
     * 处理撮合结果
     */
    private void handleExchangeTrade(List<TradeRecordDTO> trades) {
        if (trades.size() > 0) {
            int maxSize = 1000;
            if (trades.size() > maxSize) {
                int size = trades.size();
                for (int index = 0; index < size; index += maxSize) {
                    int length = (size - index) > maxSize ? maxSize : size - index;
                    java.util.List<TradeRecordDTO> subTrades = trades.subList(index, index + length);
                    // 发送到RocketMQ
                    if (streamBridge != null) {
                        streamBridge.send("trade-result-out", MessageBuilder.withPayload(JSON.toJSONString(subTrades)).build());
                    }
                }
            } else {
                if (streamBridge != null) {
                    streamBridge.send("trade-result-out", MessageBuilder.withPayload(JSON.toJSONString(trades)).build());
                }
            }
        }
    }
    
    /**
     * 订单完成
     */
    private void orderCompleted(List<OrderDTO> orders) {
        if (orders.size() > 0) {
            int maxSize = 1000;
            if (orders.size() > maxSize) {
                int size = orders.size();
                for (int index = 0; index < size; index += maxSize) {
                    int length = (size - index) > maxSize ? maxSize : size - index;
                    List<OrderDTO> subOrders = orders.subList(index, index + length);
                    if (streamBridge != null) {
                        streamBridge.send("order-completed-out", MessageBuilder.withPayload(JSON.toJSONString(subOrders)).build());
                    }
                }
            } else {
                if (streamBridge != null) {
                    streamBridge.send("order-completed-out", MessageBuilder.withPayload(JSON.toJSONString(orders)).build());
                }
            }
        }
    }
    
    /**
     * 发送盘口变化消息
     */
    private void sendTradePlateMessage(TradePlate plate) {
        synchronized (plate) {
            if (streamBridge != null) {
                streamBridge.send("trade-plate-out", MessageBuilder.withPayload(JSON.toJSONString(plate)).build());
            }
        }
    }
    
    /**
     * 取消订单
     */
    public OrderDTO cancelOrder(OrderDTO exchangeOrder) {
        log.info("取消订单: {}", exchangeOrder.getOrderNo());
        
        if (exchangeOrder.getOrderType() == 2) {  // MARKET_PRICE
            // 处理市价单
            List<OrderDTO> list = exchangeOrder.getSide() == 1 ? buyMarketQueue : sellMarketQueue;
            synchronized (list) {
                Iterator<OrderDTO> orderIterator = list.iterator();
                while (orderIterator.hasNext()) {
                    OrderDTO order = orderIterator.next();
                    if (order.getOrderNo().equals(exchangeOrder.getOrderNo())) {
                        orderIterator.remove();
                        onRemoveOrder(order);
                        return order;
                    }
                }
            }
        } else {
            // 处理限价单
            TreeMap<BigDecimal, MergeOrder> list = exchangeOrder.getSide() == 1 ? buyLimitPriceQueue : sellLimitPriceQueue;
            synchronized (list) {
                MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
                if (mergeOrder != null) {
                    Iterator<OrderDTO> orderIterator = mergeOrder.iterator();
                    while (orderIterator.hasNext()) {
                        OrderDTO order = orderIterator.next();
                        if (order.getOrderNo().equals(exchangeOrder.getOrderNo())) {
                            orderIterator.remove();
                            if (mergeOrder.size() == 0) {
                                list.remove(exchangeOrder.getPrice());
                            }
                            onRemoveOrder(order);
                            return order;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 从盘口移除订单
     */
    private void onRemoveOrder(OrderDTO order) {
        if (order.getOrderType() == 1) {  // LIMIT_PRICE
            if (order.getSide() == 1) {  // BUY
                buyTradePlate.remove(order);
                sendTradePlateMessage(buyTradePlate);
            } else {
                sellTradePlate.remove(order);
                sendTradePlateMessage(sellTradePlate);
            }
        }
    }
}

