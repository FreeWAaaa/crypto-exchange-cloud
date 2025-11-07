package com.cex.matching.core;

import com.cex.common.dto.OrderDTO;
import com.cex.common.enums.OrderDirection;
import com.cex.common.enums.OrderType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * 盘口信息
 *
 * @author cex
 */
@Data
@Slf4j
public class TradePlate {
    /** 盘口数据 */
    private LinkedList<TradePlateItem> items;
    /** 最大深度 */
    private int maxDepth = 100;
    /** 方向 */
    private OrderDirection direction;
    /** 交易对 */
    private String symbol;

    public TradePlate() {
        this.items = new LinkedList<>();
    }

    public TradePlate(String symbol, OrderDirection direction) {
        this.symbol = symbol;
        this.direction = direction;
        this.items = new LinkedList<>();
    }

    /**
     * 添加订单到盘口
     */
    public boolean add(OrderDTO exchangeOrder) {
        synchronized (items) {
            int index = 0;
            
            // 市价单不加入盘口
            if (exchangeOrder.getOrderType() == 2) {  // MARKET_PRICE
                return false;
            }
            
            // 方向不一致不加入
            if (!directionMatches(exchangeOrder.getSide())) {
                return false;
            }

            if (items.size() > 0) {
                for (index = 0; index < items.size(); index++) {
                    TradePlateItem item = items.get(index);
                    
                    // 买单：价格高的在前面
                    // 卖单：价格低的在前面
                    if (direction == OrderDirection.BUY && item.getPrice().compareTo(exchangeOrder.getPrice()) > 0
                            || direction == OrderDirection.SELL && item.getPrice().compareTo(exchangeOrder.getPrice()) < 0) {
                        continue;
                    } else if (item.getPrice().compareTo(exchangeOrder.getPrice()) == 0) {
                        // 相同价格，合并数量
                        BigDecimal deltaAmount = exchangeOrder.getAmount().subtract(exchangeOrder.getFilledAmount());
                        item.setAmount(item.getAmount().add(deltaAmount));
                        return true;
                    } else {
                        break;
                    }
                }
            }
            
            // 添加到合适位置
            if (index < maxDepth) {
                TradePlateItem newItem = new TradePlateItem();
                newItem.setAmount(exchangeOrder.getAmount().subtract(exchangeOrder.getFilledAmount()));
                newItem.setPrice(exchangeOrder.getPrice());
                items.add(index, newItem);
            }
        }
        return true;
    }

    /**
     * 从盘口移除订单
     */
    public void remove(OrderDTO order, BigDecimal amount) {
        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {
                TradePlateItem item = items.get(index);
                if (item.getPrice().compareTo(order.getPrice()) == 0) {
                    item.setAmount(item.getAmount().subtract(amount));
                    if (item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        items.remove(index);
                    }
                    return;
                }
            }
        }
    }

    /**
     * 从盘口移除订单
     */
    public void remove(OrderDTO order) {
        remove(order, order.getAmount().subtract(order.getFilledAmount()));
    }

    /**
     * 判断订单方向是否匹配
     */
    private boolean directionMatches(Integer side) {
        if (direction == OrderDirection.BUY && side == 1) {
            return true;
        }
        if (direction == OrderDirection.SELL && side == 2) {
            return true;
        }
        return false;
    }

    /**
     * 获取最高价
     */
    public BigDecimal getHighestPrice() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == OrderDirection.BUY) {
            return items.getFirst().getPrice();
        } else {
            return items.getLast().getPrice();
        }
    }

    /**
     * 获取最低价
     */
    public BigDecimal getLowestPrice() {
        if (items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == OrderDirection.BUY) {
            return items.getLast().getPrice();
        } else {
            return items.getFirst().getPrice();
        }
    }

    /**
     * 获取深度
     */
    public int getDepth() {
        return items.size();
    }
}

