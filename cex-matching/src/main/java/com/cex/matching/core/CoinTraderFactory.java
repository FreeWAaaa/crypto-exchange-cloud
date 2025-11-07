package com.cex.matching.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 币种交易器工厂
 * 
 * @author cex
 */
@Slf4j
@Component
public class CoinTraderFactory {
    
    private final ConcurrentMap<String, CoinTrader> traders = new ConcurrentHashMap<>();
    
    private StreamBridge streamBridge;
    
    public void setStreamBridge(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }
    
    /**
     * 获取或创建交易器
     */
    public CoinTrader getTrader(String symbol) {
        return traders.computeIfAbsent(symbol, sym -> {
            CoinTrader trader = new CoinTrader(sym);
            if (streamBridge != null) {
                trader.setStreamBridge(streamBridge);
            }
            trader.setReady(true);
            log.info("创建交易器: {}", symbol);
            return trader;
        });
    }
    
    /**
     * 移除交易器
     */
    public void removeTrader(String symbol) {
        traders.remove(symbol);
        log.info("移除交易器: {}", symbol);
    }
    
    /**
     * 获取所有交易器
     */
    public ConcurrentMap<String, CoinTrader> getAllTraders() {
        return traders;
    }
}

