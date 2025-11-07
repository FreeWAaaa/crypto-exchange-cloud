package com.cex.matching.consumer;

import com.alibaba.fastjson.JSON;
import com.cex.common.dto.OrderDTO;
import com.cex.matching.core.CoinTrader;
import com.cex.matching.core.CoinTraderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 撮合订单消费者
 * 
 * 接收来自trade模块的订单，交给CoinTrader进行撮合
 * 
 * @author cex
 */
@Slf4j
@Component
public class MatchingOrderConsumer {
    
    private final CoinTraderFactory traderFactory;
    private final StreamBridge streamBridge;
    
    public MatchingOrderConsumer(CoinTraderFactory traderFactory, StreamBridge streamBridge) {
        this.traderFactory = traderFactory;
        this.streamBridge = streamBridge;
    }
    
    /**
     * 接收订单并撮合
     */
    @Bean
    public Consumer<Message<String>> orderInput() {
        return message -> {
            try {
                String payload = message.getPayload();
                log.info("接收到订单消息: {}", payload);
                
                OrderDTO order = JSON.parseObject(payload, OrderDTO.class);
                if (order == null) {
                    log.warn("订单消息解析失败");
                    return;
                }
                
                CoinTrader trader = traderFactory.getTrader(order.getSymbol());
                
                // 如果交易暂停或未就绪，取消订单
                if (trader.isTradingHalt() || !trader.isReady()) {
                    log.info("交易器未就绪或暂停，取消订单: {}", order.getOrderNo());
                    OrderDTO canceledOrder = new OrderDTO();
                    canceledOrder.setOrderNo(order.getOrderNo());
                    canceledOrder.setStatus(3);  // CANCELED
                    streamBridge.send("order-completed-out", 
                            MessageBuilder.withPayload(JSON.toJSONString(canceledOrder)).build());
                    return;
                }
                
                // 执行撮合
                long startTick = System.currentTimeMillis();
                trader.trade(order);
                log.info("订单撮合完成: orderNo={}, usedTime={}ms", 
                        order.getOrderNo(), System.currentTimeMillis() - startTick);
                
            } catch (Exception e) {
                log.error("处理订单消息失败", e);
            }
        };
    }
    
    /**
     * 接收取消订单请求
     */
    @Bean
    public Consumer<Message<String>> orderCancelInput() {
        return message -> {
            try {
                String payload = message.getPayload();
                log.info("接收到取消订单消息: {}", payload);
                
                OrderDTO order = JSON.parseObject(payload, OrderDTO.class);
                if (order == null) {
                    log.warn("取消订单消息解析失败");
                    return;
                }
                
                CoinTrader trader = traderFactory.getTrader(order.getSymbol());
                
                if (trader.isReady()) {
                    OrderDTO canceledOrder = trader.cancelOrder(order);
                    if (canceledOrder != null) {
                        canceledOrder.setStatus(3);  // CANCELED
                        streamBridge.send("order-completed-out", 
                                MessageBuilder.withPayload(JSON.toJSONString(canceledOrder)).build());
                    }
                }
                
            } catch (Exception e) {
                log.error("处理取消订单消息失败", e);
            }
        };
    }
}

