package com.cex.matching.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 币种交易器工厂（CoinTrader Factory）
 * 
 * 【作用】
 * 这是一个工厂类，负责管理所有交易对的 CoinTrader 实例。
 * 
 * 【设计模式】
 * - Factory 模式：统一管理所有交易对的撮合引擎
 * - 单例模式：每个交易对只有一个 CoinTrader 实例
 * 
 * 【核心概念】
 * - Factory（工厂）：管理所有交易对的 CoinTrader
 * - Trader（交易器）：每个交易对（如 BTC/USDT）对应一个 CoinTrader 实例
 * 
 * 【例子】
 * ```
 * traderFactory.getTrader("BTC/USDT")  → CoinTrader("BTC/USDT")
 *   - 管理 BTC/USDT 的订单簿
 *   - 处理 BTC/USDT 的撮合
 * 
 * traderFactory.getTrader("ETH/USDT") → CoinTrader("ETH/USDT")
 *   - 管理 ETH/USDT 的订单簿
 *   - 处理 ETH/USDT 的撮合
 * ```
 * 
 * 【为什么需要 Factory？】
 * 1. 隔离不同交易对：每个交易对有自己的订单簿，互不干扰
 * 2. 按需创建：只有有订单的交易对才会创建 CoinTrader（懒加载）
 * 3. 统一管理：方便管理所有交易对的状态（暂停、恢复等）
 * 4. 线程安全：使用 ConcurrentHashMap 保证多线程安全
 * 
 * 【数据结构】
 * - ConcurrentMap<String, CoinTrader>
 *   - Key: 交易对名称（如 "BTC/USDT"）
 *   - Value: 对应的 CoinTrader 实例
 * 
 * @author cex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinTraderFactory {
    
    /**
     * 所有交易器的缓存
     * 
     * 【数据结构】
     * - Key: 交易对名称（如 "BTC/USDT"、"ETH/USDT"）
     * - Value: 对应的 CoinTrader 实例
     * 
     * 【为什么用 ConcurrentHashMap？】
     * - 线程安全：多个线程可能同时调用 getTrader()
     * - 高性能：读操作不需要加锁
     * - 支持并发：多个交易对可以同时进行撮合
     * 
     * 【注意】
     * 虽然用了 ConcurrentHashMap，但每个 CoinTrader 内部的订单簿操作还需要 synchronized
     * 因为 ConcurrentHashMap 只保证 Map 本身的线程安全，不保证 Value 对象的线程安全
     */
    private final ConcurrentMap<String, CoinTrader> traders = new ConcurrentHashMap<>();
    
    /**
     * 消息发送桥接器
     * 
     * 【作用】
     * 用于发送撮合结果到 MQ（成交记录、订单完成通知等）
     * 
     * 【传递方式】
     * 通过 setStreamBridge() 方法传递给每个 CoinTrader 实例
     * 这样 CoinTrader 就可以在撮合完成后发送结果
     */
    private final StreamBridge streamBridge;
    
    /**
     * 获取或创建交易器
     * 
     * 【方法说明】
     * 根据交易对名称获取对应的 CoinTrader 实例。
     * 如果不存在，则创建新的实例（懒加载）。
     * 
     * 【执行流程】
     * 1. 检查 traders Map 中是否已有该交易对的 CoinTrader
     * 2. 如果存在，直接返回
     * 3. 如果不存在，创建新的 CoinTrader 实例
     * 4. 设置 StreamBridge（用于发送撮合结果）
     * 5. 设置交易器为就绪状态
     * 6. 放入 Map 并返回
     * 
     * 【线程安全】
     * - computeIfAbsent() 是原子操作，保证线程安全
     * - 即使多个线程同时调用，也只会创建一个实例
     * 
     * 【例子】
     * ```java
     * // 第一次调用：创建新的 CoinTrader
     * CoinTrader btcTrader = traderFactory.getTrader("BTC/USDT");
     * 
     * // 第二次调用：返回已存在的 CoinTrader（不会重复创建）
     * CoinTrader btcTrader2 = traderFactory.getTrader("BTC/USDT");
     * // btcTrader == btcTrader2 (同一个实例)
     * ```
     * 
     * @param symbol 交易对名称（如 "BTC/USDT"、"ETH/USDT"）
     * @return CoinTrader 对应的交易器实例
     */
    public CoinTrader getTrader(String symbol) {
        // 使用 computeIfAbsent 实现懒加载
        // - 如果 symbol 已存在，直接返回对应的 CoinTrader
        // - 如果 symbol 不存在，执行 lambda 创建新的 CoinTrader
        return traders.computeIfAbsent(symbol, sym -> {
            // 创建新的 CoinTrader 实例
            // 每个 CoinTrader 管理一个交易对的所有订单簿
            CoinTrader trader = new CoinTrader(sym);
            
            // 设置 StreamBridge，用于发送撮合结果
            // CoinTrader 在撮合完成后会通过 StreamBridge 发送成交记录、订单完成通知等
            if (streamBridge != null) {
                trader.setStreamBridge(streamBridge);
            }
            
            // 设置交易器为就绪状态
            // 只有就绪的交易器才能处理订单
            trader.setReady(true);
            
            log.info("创建交易器: {}", symbol);
            return trader;
        });
    }
    
    /**
     * 移除交易器
     * 
     * 【使用场景】
     * - 交易对下架时移除
     * - 系统维护时清理
     * 
     * 【注意】
     * 移除后，该交易对的订单簿数据会丢失（因为都在内存中）
     * 通常需要先处理完所有订单再移除
     * 
     * @param symbol 交易对名称
     */
    public void removeTrader(String symbol) {
        traders.remove(symbol);
        log.info("移除交易器: {}", symbol);
    }
    
    /**
     * 获取所有交易器
     * 
     * 【使用场景】
     * - 监控所有交易对的状态
     * - 批量操作（如暂停所有交易）
     * 
     * @return 所有交易器的 Map（只读，不要直接修改）
     */
    public ConcurrentMap<String, CoinTrader> getAllTraders() {
        return traders;
    }
}

