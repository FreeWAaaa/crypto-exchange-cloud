package com.cex.matching.core;

import com.cex.common.dto.OrderDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 合并订单（同一价格的所有订单）
 *
 * @author cex
 */
public class MergeOrder {
    private final List<OrderDTO> orders = new ArrayList<>();

    /**
     * 添加订单
     */
    public void add(OrderDTO order) {
        orders.add(order);
    }

    /**
     * 获取第一个订单
     */
    public OrderDTO get() {
        return orders.get(0);
    }

    /**
     * 获取订单数量
     */
    public int size() {
        return orders.size();
    }

    /**
     * 获取价格（第一笔订单的价格）
     */
    public BigDecimal getPrice() {
        return orders.get(0).getPrice();
    }

    /**
     * 获取迭代器
     */
    public Iterator<OrderDTO> iterator() {
        return orders.iterator();
    }

    /**
     * 获取总委托量
     */
    public BigDecimal getTotalAmount() {
        return orders.stream()
                .map(OrderDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

