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
 * 【作用】
 * 这是撮合引擎的入口，负责接收来自 trade 模块的订单消息，然后交给对应的 CoinTrader 进行撮合处理。
 * 
 * 【工作流程】
 * 1. 从 RocketMQ 接收订单消息（JSON 格式）
 * 2. 解析消息为 OrderDTO 对象
 * 3. 根据交易对（symbol）获取对应的 CoinTrader（撮合引擎）
 * 4. 调用 CoinTrader.trade() 执行撮合算法
 * 5. 撮合结果由 CoinTrader 通过 MQ 发送出去
 * 
 * 【关键点】
 * - 每个交易对（BTC/USDT、ETH/USDT 等）都有独立的 CoinTrader 实例
 * - 撮合算法在 CoinTrader 中实现（价格优先、时间优先）
 * - 所有撮合都在内存中进行，处理完成后通过 MQ 发送结果
 * 
 * @author cex
 */
@Slf4j
@Component
public class MatchingOrderConsumer {
    
    /**
     * 交易器工厂
     * 
     * 【作用】
     * CoinTraderFactory 是一个工厂类，负责管理所有交易对的 CoinTrader 实例。
     * 
     * 【设计模式】
     * - Factory 模式：统一管理所有交易对的撮合引擎
     * - 每个交易对（symbol）对应一个 CoinTrader 实例
     * 
     * 【例子】
     * - traderFactory.getTrader("BTC/USDT") → 返回 BTC/USDT 的 CoinTrader
     * - traderFactory.getTrader("ETH/USDT") → 返回 ETH/USDT 的 CoinTrader
     * 
     * 【为什么需要 Factory？】
     * 1. 隔离不同交易对：每个交易对有自己的订单簿，互不干扰
     * 2. 按需创建：只有有订单的交易对才会创建 CoinTrader
     * 3. 统一管理：方便管理所有交易对的状态（暂停、恢复等）
     */
    private final CoinTraderFactory traderFactory;
    
    /**
     * 消息发送桥接器
     * 
     * 【作用】
     * 用于发送撮合结果到 MQ（成交记录、订单完成通知等）
     * 
     * 【注意】
     * 这里虽然注入了 StreamBridge，但实际发送消息是在 CoinTrader 中完成的
     * CoinTrader 通过 setStreamBridge() 方法接收这个 StreamBridge
     */
    private final StreamBridge streamBridge;
    
    /**
     * 构造函数注入
     * 
     * @param traderFactory 交易器工厂（管理所有交易对的撮合引擎）
     * @param streamBridge 消息发送桥接器（用于发送撮合结果）
     */
    public MatchingOrderConsumer(CoinTraderFactory traderFactory, StreamBridge streamBridge) {
        this.traderFactory = traderFactory;
        this.streamBridge = streamBridge;
    }
    
    /**
     * 接收订单并撮合
     * 
     * 【这是 Spring Cloud Stream 的消费者方法】
     * - 方法名 orderInput 必须在配置的 function.definition 中声明
     * - 绑定名称：orderInput-in-0 → exchange-order-topic
     * 
     * 【处理流程】
     * 1. 接收消息：从 RocketMQ Topic (exchange-order-topic) 接收订单消息
     * 2. 解析消息：将 JSON 字符串解析为 OrderDTO 对象
     * 3. 获取交易器：根据交易对（symbol）从 Factory 获取对应的 CoinTrader
     * 4. 检查状态：如果交易器未就绪或暂停，直接取消订单
     * 5. 执行撮合：调用 CoinTrader.trade() 执行撮合算法
     * 6. 记录日志：记录撮合耗时
     * 
     * 【撮合算法说明】
     * 撮合算法在 CoinTrader.trade() 中实现，主要包括：
     * - 限价单与限价单撮合：价格优先、时间优先
     * - 限价单与市价单撮合：市价单按对手盘最优价格成交
     * - 市价单与限价单撮合：市价单按对手盘最优价格成交
     * 
     * 【内存处理】
     * - 所有订单簿（买单队列、卖单队列）都在内存中（TreeMap、LinkedList）
     * - 撮合过程完全在内存中进行，速度极快
     * - 撮合完成后，结果通过 MQ 发送，由其他服务负责落库
     * 
     * @return Consumer<Message<String>> Spring Cloud Stream 消费者函数
     */
    @Bean
    public Consumer<Message<String>> orderInput() {
        return message -> {
            try {
                // ========== 第一步：解析消息 ==========
                // 从 MQ 消息中获取 JSON 字符串
                String payload = message.getPayload();
                log.info("接收到订单消息: {}", payload);
                
                // 将 JSON 字符串解析为 OrderDTO 对象
                // OrderDTO 包含：订单号、用户ID、交易对、订单类型、方向、价格、数量等
                OrderDTO order = JSON.parseObject(payload, OrderDTO.class);
                if (order == null) {
                    log.warn("订单消息解析失败");
                    return;
                }
                
                // ========== 第二步：获取对应的交易器 ==========
                // 根据交易对（symbol）从 Factory 获取对应的 CoinTrader
                // 例如：BTC/USDT → CoinTrader("BTC/USDT")
                //       ETH/USDT → CoinTrader("ETH/USDT")
                // 
                // 【重要】每个交易对都有独立的 CoinTrader 实例
                // - 每个 CoinTrader 有自己的订单簿（买单队列、卖单队列）
                // - 不同交易对的订单完全隔离，互不干扰
                CoinTrader trader = traderFactory.getTrader(order.getSymbol());
                
                // ========== 第三步：检查交易器状态 ==========
                // 如果交易器未就绪或暂停交易，直接取消订单
                // 这种情况通常发生在：
                // - 交易对刚创建，还未初始化完成
                // - 交易对被管理员暂停（维护、异常等）
                if (trader.isTradingHalt() || !trader.isReady()) {
                    log.info("交易器未就绪或暂停，取消订单: {}", order.getOrderNo());
                    
                    // 构造取消订单的响应
                    OrderDTO canceledOrder = new OrderDTO();
                    canceledOrder.setOrderNo(order.getOrderNo());
                    canceledOrder.setStatus(3);  // 3 = CANCELED（已取消）
                    
                    // 发送订单完成通知到 MQ
                    // 接收方：cex-trade 模块的 TradeResultConsumer
                    streamBridge.send("order-completed-out", 
                            MessageBuilder.withPayload(JSON.toJSONString(canceledOrder)).build());
                    return;
                }
                
                // ========== 第四步：执行撮合 ==========
                // 调用 CoinTrader.trade() 执行撮合算法
                // 
                // 【撮合算法核心逻辑】
                // 1. 判断订单类型（限价单/市价单）
                // 2. 判断订单方向（买入/卖出）
                // 3. 从对手盘队列中查找可匹配的订单
                // 4. 按价格优先、时间优先的原则进行撮合
                // 5. 更新订单状态（部分成交/完全成交）
                // 6. 生成成交记录（TradeRecord）
                // 7. 发送成交结果到 MQ
                // 
                // 【内存处理】
                // - 所有订单簿都在内存中（TreeMap、LinkedList）
                // - 撮合过程完全在内存中进行，速度极快（微秒级）
                // - 撮合完成后，结果通过 MQ 发送，由其他服务负责落库
                long startTick = System.currentTimeMillis();
                trader.trade(order);
                
                // 记录撮合耗时（通常几毫秒到几十毫秒）
                log.info("订单撮合完成: orderNo={}, usedTime={}ms", 
                        order.getOrderNo(), System.currentTimeMillis() - startTick);
                
            } catch (Exception e) {
                log.error("处理订单消息失败", e);
            }
        };
    }
    
    /**
     * 接收取消订单请求
     * 
     * 【这是 Spring Cloud Stream 的消费者方法】
     * - 方法名 orderCancelInput 必须在配置的 function.definition 中声明
     * - 绑定名称：orderCancelInput-in-0 → exchange-order-cancel-topic
     * 
     * 【处理流程】
     * 1. 接收消息：从 RocketMQ Topic (exchange-order-cancel-topic) 接收取消订单请求
     * 2. 解析消息：将 JSON 字符串解析为 OrderDTO 对象
     * 3. 获取交易器：根据交易对获取对应的 CoinTrader
     * 4. 取消订单：从订单簿中移除订单
     * 5. 发送通知：发送订单完成通知到 MQ
     * 
     * 【取消订单的逻辑】
     * - 限价单：从对应价格的 MergeOrder 中移除
     * - 市价单：从市价单队列中移除
     * - 如果订单已部分成交，取消的是剩余未成交部分
     * 
     * @return Consumer<Message<String>> Spring Cloud Stream 消费者函数
     */
    @Bean
    public Consumer<Message<String>> orderCancelInput() {
        return message -> {
            try {
                // ========== 第一步：解析消息 ==========
                String payload = message.getPayload();
                log.info("接收到取消订单消息: {}", payload);
                
                // 解析为 OrderDTO 对象
                OrderDTO order = JSON.parseObject(payload, OrderDTO.class);
                if (order == null) {
                    log.warn("取消订单消息解析失败");
                    return;
                }
                
                // ========== 第二步：获取对应的交易器 ==========
                // 根据交易对获取对应的 CoinTrader
                CoinTrader trader = traderFactory.getTrader(order.getSymbol());
                
                // ========== 第三步：取消订单 ==========
                // 只有交易器就绪时才处理取消请求
                if (trader.isReady()) {
                    // 调用 CoinTrader.cancelOrder() 从订单簿中移除订单
                    // 返回值：被取消的订单对象（如果找到的话）
                    OrderDTO canceledOrder = trader.cancelOrder(order);
                    
                    if (canceledOrder != null) {
                        // 设置订单状态为已取消
                        canceledOrder.setStatus(3);  // 3 = CANCELED
                        
                        // 发送订单完成通知到 MQ
                        // 接收方：cex-trade 模块的 TradeResultConsumer
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

