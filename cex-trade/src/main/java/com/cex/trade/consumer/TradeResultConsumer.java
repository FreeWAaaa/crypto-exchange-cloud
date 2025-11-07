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

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * 交易结果消费者
 * 
 * 接收撮合引擎的成交结果，更新订单状态
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
                    
                    // 更新买方订单
                    updateOrderFromTrade(tradeRecord.getBuyOrderNo(), tradeRecord.getPrice(), 
                                      tradeRecord.getAmount(), tradeRecord.getMoney(), tradeRecord.getBuyFee());
                    
                    // 更新卖方订单
                    updateOrderFromTrade(tradeRecord.getSellOrderNo(), tradeRecord.getPrice(), 
                                      tradeRecord.getAmount(), tradeRecord.getMoney(), tradeRecord.getSellFee());
                }
                
            } catch (Exception e) {
                log.error("处理撮合结果失败", e);
            }
        };
    }
    
    /**
     * 接收订单完成通知
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
                    
                    // 更新订单状态
                    tradeOrderService.updateOrderFilled(
                        orderDTO.getOrderNo(),
                        orderDTO.getFilledAmount(),
                        orderDTO.getFilledMoney(),
                        BigDecimal.ZERO,  // TODO: 从配置获取手续费
                        orderDTO.getStatus()
                    );
                }
                
            } catch (Exception e) {
                log.error("处理订单完成通知失败", e);
            }
        };
    }
    
    /**
     * 从成交记录更新订单
     */
    private void updateOrderFromTrade(String orderNo, java.math.BigDecimal price, java.math.BigDecimal amount, 
                                     java.math.BigDecimal money, java.math.BigDecimal fee) {
        try {
            // 获取订单当前状态
            var order = tradeOrderService.getByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在：{}", orderNo);
                return;
            }
            
            // 计算新的成交信息
            java.math.BigDecimal newFilledAmount = order.getFilledAmount().add(amount);
            java.math.BigDecimal newFilledMoney = order.getFilledMoney().add(money);
            java.math.BigDecimal newFee = order.getFee().add(fee);
            
            // 判断订单状态
            Integer status;
            if (newFilledAmount.compareTo(order.getAmount()) >= 0) {
                status = 2; // 完全成交
            } else if (newFilledAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                status = 1; // 部分成交
            } else {
                status = 0; // 待成交
            }
            
            // 更新订单
            tradeOrderService.updateOrderFilled(orderNo, newFilledAmount, newFilledMoney, newFee, status);
            
            log.info("订单成交更新：orderNo={}, filledAmount={}, status={}", orderNo, newFilledAmount, status);
            
        } catch (Exception e) {
            log.error("更新订单失败：orderNo={}", orderNo, e);
        }
    }
}

