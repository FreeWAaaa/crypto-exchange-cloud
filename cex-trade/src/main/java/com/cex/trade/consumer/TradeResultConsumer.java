package com.cex.trade.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cex.common.dto.OrderDTO;
import com.cex.common.dto.TradeRecordDTO;
import com.cex.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 交易结果消费者
 * 
 * 接收撮合引擎的成交结果，更新订单状态、处理资产变更、保存成交记录
 * 
 * @author cex
 */
@Slf4j
@Component
public class TradeResultConsumer {
    
    private final TradeOrderService tradeOrderService;
    
    public TradeResultConsumer(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }
    
    /**
     * 接收撮合结果
     * 
     * 【处理流程】
     * 1. 解析成交记录列表
     * 2. 对每条成交记录：
     *    - 更新买方订单成交信息
     *    - 更新卖方订单成交信息
     *    - 处理买方资产变更（扣减冻结计价币，增加基础币）
     *    - 处理卖方资产变更（扣减冻结基础币，增加计价币）
     *    - 保存成交记录到数据库
     */
    @Bean
    public Consumer<Message<String>> tradeResultInput() {
        return message -> {
            try {
                String payload = message.getPayload();
                log.info("接收到撮合结果：{}", payload);
                
                // 解析成交记录列表
                JSONArray tradeArray = JSONArray.parseArray(payload);
                
                for (int i = 0; i < tradeArray.size(); i++) {
                    JSONObject tradeJson = tradeArray.getJSONObject(i);
                    TradeRecordDTO tradeRecord = JSON.parseObject(tradeJson.toJSONString(), TradeRecordDTO.class);
                    
                    // 使用 Service 方法处理成交记录（包含分布式事务）
                    // 这个方法内部会：
                    // 1. 更新买方和卖方订单成交信息
                    // 2. 处理买方资产变更（扣减冻结计价币，增加基础币）
                    // 3. 处理卖方资产变更（扣减冻结基础币，增加计价币）
                    // 4. 保存成交记录到数据库
                    // 都在同一个分布式事务中
                    tradeOrderService.processTradeRecord(tradeRecord);
                }
                
            } catch (Exception e) {
                log.error("处理撮合结果失败", e);
                // 抛出异常，让 MQ 重试
                throw e;
            }
        };
    }
    
    /**
     * 接收订单完成通知
     * 
     * 【作用】
     * 接收撮合引擎发送的订单完成通知，根据订单状态做不同处理：
     * - status = 2 (COMPLETED)：订单完全成交
     * - status = 3 (CANCELED)：订单被取消
     * 
     * 【处理逻辑】
     * 1. 先解冻余额（远程调用 Wallet 服务）
     * 2. 解冻成功后，更新订单状态到数据库（本地事务）
     * 
     * 【事务处理】
     * - 先解冻余额：确保用户资金安全，如果失败不更新订单状态
     * - 再更新订单状态：解冻成功后，使用本地事务更新
     * - 失败处理：记录日志，可以重试（MQ 的 retry 机制）
     * 
     * 【为什么先解冻余额？】
     * - 保证用户资金安全：先解冻，确保用户可以使用资金
     * - 如果解冻失败，订单状态不更新，可以重试
     * - 避免数据不一致：如果先更新订单状态，解冻失败会导致数据不一致
     */
    @Bean
    public Consumer<Message<String>> orderCompletedInput() {
        return message -> {
            try {
                String payload = message.getPayload();
                log.info("接收到订单完成通知：{}", payload);
                
                // 解析订单列表
                JSONArray orderArray = JSONArray.parseArray(payload);
                
                for (int i = 0; i < orderArray.size(); i++) {
                    JSONObject orderJson = orderArray.getJSONObject(i);
                    OrderDTO orderDTO = JSON.parseObject(orderJson.toJSONString(), OrderDTO.class);
                    
                    // 使用 Service 方法处理订单完成（包含分布式事务）
                    // 这个方法内部会：
                    // 1. 解冻余额（远程调用 Wallet 服务）
                    // 2. 更新订单状态（本地数据库操作）
                    // 都在同一个分布式事务中
                    tradeOrderService.handleOrderCompleted(orderDTO);
                }
                
            } catch (Exception e) {
                log.error("处理订单完成通知失败", e);
                // 抛出异常，让 MQ 重试
                throw e;
            }
        };
    }
    
}

