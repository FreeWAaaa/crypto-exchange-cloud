package com.cex.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.cex.common.dto.OrderDTO;
import com.cex.common.dto.TradeRecordDTO;
import com.cex.trade.domain.entity.TradeOrder;
import com.cex.trade.domain.entity.TradeRecord;
import com.cex.trade.domain.entity.TradeSymbol;
import com.cex.trade.mapper.TradeOrderMapper;
import com.cex.trade.mapper.TradeRecordMapper;
import com.cex.trade.mapper.TradeSymbolMapper;
import com.cex.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 交易订单服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    @Autowired
    private TradeOrderMapper orderMapper;

    @Autowired
    private TradeSymbolMapper symbolMapper;
    
    @Autowired
    private TradeRecordMapper tradeRecordMapper;
    
    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private com.cex.trade.client.WalletFeignClient walletClient;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String placeOrder(Long userId, String symbol, Integer orderType, Integer side, 
                            BigDecimal price, BigDecimal amount, String clientOrderId) {
        
        log.info("用户下单：userId={}, symbol={}, type={}, side={}, price={}, amount={}", 
                 userId, symbol, orderType, side, price, amount);

        // 1. 参数校验
        validateOrderParams(orderType, side, price, amount);

        // 2. 查询交易对配置
        TradeSymbol tradeSymbol = symbolMapper.selectBySymbol(symbol);
        if (tradeSymbol == null) {
            throw new RuntimeException("交易对不存在：" + symbol);
        }
        if (tradeSymbol.getStatus() != 0) {
            throw new RuntimeException("交易对已停用");
        }
        if (tradeSymbol.getTradeable() == 0) {
            throw new RuntimeException("该交易对暂停交易");
        }

        // 3. 校验交易对配置
        validateSymbolConfig(tradeSymbol, orderType, side, price, amount);

        // 4. 校验用户当前委托数量
        Integer currentCount = orderMapper.countCurrentOrders(userId, symbol);
        if (tradeSymbol.getMaxTradingOrder() > 0 && currentCount >= tradeSymbol.getMaxTradingOrder()) {
            throw new RuntimeException("超过最大委托数量限制：" + tradeSymbol.getMaxTradingOrder());
        }

        // 5. 计算需要冻结的金额
        BigDecimal freezeAmount = calculateFreezeAmount(orderType, side, price, amount);
        String freezeCoin = getFreezeCoin(side, tradeSymbol);

        // 6. 冻结余额（调用Wallet服务，在分布式事务中）
        walletClient.freezeBalance(userId, freezeCoin, freezeAmount, null, "下单");
        log.info("冻结余额：userId={}, coin={}, amount={}", userId, freezeCoin, freezeAmount);

        // 7. 创建订单
        TradeOrder order = buildOrder(userId, tradeSymbol, orderType, side, price, amount, clientOrderId);
        orderMapper.insert(order);

        log.info("订单创建成功：orderNo={}", order.getOrderNo());

        // 8. 发送到撮合引擎
        sendToMatchingEngine(order);
        
        return order.getOrderNo();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        log.info("用户撤单：userId={}, orderNo={}", userId, orderNo);

        // 1. 查询订单
        TradeOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 校验权限
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 3. 校验订单状态
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("订单无法撤销，当前状态：" + getStatusName(order.getStatus()));
        }

        // 4. 发送撤单消息到撮合引擎
        sendCancelToMatchingEngine(order);
        
        // 5. 计算需要解冻的金额
        BigDecimal unfreezeAmount = calculateUnfreezeAmount(order);
        TradeSymbol symbol = symbolMapper.selectBySymbol(order.getSymbol());
        String unfreezeCoin = getFreezeCoin(order.getSide(), symbol);

        // 6. 解冻余额（调用Wallet服务，在分布式事务中）
        walletClient.unfreezeBalance(userId, unfreezeCoin, unfreezeAmount, orderNo, "撤单");
        log.info("解冻余额：userId={}, coin={}, amount={}", userId, unfreezeCoin, unfreezeAmount);

        // 7. 更新订单状态
        order.setStatus(3); // 已撤销
        order.setCancelTime(new Date());
        order.setCancelReason("用户撤单");
        orderMapper.updateById(order);

        log.info("撤单成功：orderNo={}", orderNo);
    }

    @Override
    public List<TradeOrder> getCurrentOrders(Long userId) {
        return orderMapper.selectCurrentOrders(userId);
    }

    @Override
    public List<TradeOrder> getCurrentOrdersBySymbol(Long userId, String symbol) {
        return orderMapper.selectCurrentOrdersBySymbol(userId, symbol);
    }

    @Override
    public List<TradeOrder> getHistoryOrders(Long userId) {
        return orderMapper.selectHistoryOrders(userId);
    }

    @Override
    public List<TradeOrder> getHistoryOrdersBySymbol(Long userId, String symbol) {
        return orderMapper.selectHistoryOrdersBySymbol(userId, symbol);
    }

    @Override
    public TradeOrder getByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderFilled(String orderNo, BigDecimal filledAmount, BigDecimal filledMoney, 
                                 BigDecimal fee, Integer status) {
        TradeOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.warn("订单不存在：{}", orderNo);
            return;
        }

        order.setFilledAmount(filledAmount);
        order.setFilledMoney(filledMoney);
        order.setFee(fee);
        order.setStatus(status);
        
        // 计算平均成交价
        order.calculateAvgPrice();
        
        // 如果完全成交，记录完成时间
        if (status == 2) {
            order.setCompleteTime(new Date());
        }

        orderMapper.updateById(order);
        log.info("订单成交信息已更新：orderNo={}, filledAmount={}, status={}", orderNo, filledAmount, status);
    }

    /**
     * 处理订单完成（分布式事务）
     * 
     * 【作用】
     * 用于 MQ 消费者调用，处理订单完成通知，包含：
     * 1. 解冻余额（远程调用 Wallet 服务）
     * 2. 更新订单状态（本地数据库操作）
     * 
     * 【事务处理】
     * - 使用 @GlobalTransactional 保证分布式事务一致性
     * - 如果解冻余额失败，订单状态不会更新，整个事务回滚
     * 
     * @param orderDTO 订单DTO
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCompleted(com.cex.common.dto.OrderDTO orderDTO) {
        log.info("处理订单完成（分布式事务）：orderNo={}, status={}", orderDTO.getOrderNo(), orderDTO.getStatus());
        
        // 1. 获取原始订单信息
        TradeOrder order = getByOrderNo(orderDTO.getOrderNo());
        if (order == null) {
            log.warn("订单不存在，无法处理：orderNo={}", orderDTO.getOrderNo());
            throw new RuntimeException("订单不存在：" + orderDTO.getOrderNo());
        }
        
        // 2. 根据订单状态处理余额（先解冻余额）
        if (orderDTO.getStatus() == 3 || orderDTO.getStatus() == 2) {
            // 计算需要解冻的金额
            BigDecimal unfilledAmount = order.getAmount().subtract(order.getFilledAmount());
            
            if (unfilledAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal unfreezeAmount;
                String unfreezeCoin;
                
                if (order.getOrderType() == 1) {  // 限价单
                    if (order.getSide() == 1) {  // 限价买单
                        unfreezeAmount = order.getPrice().multiply(unfilledAmount);
                        unfreezeCoin = order.getQuoteCoin();
                    } else {  // 限价卖单
                        unfreezeAmount = unfilledAmount;
                        unfreezeCoin = order.getBaseCoin();
                    }
                } else {  // 市价单
                    if (order.getSide() == 1) {  // 市价买单
                        unfreezeAmount = order.getAmount().subtract(order.getFilledMoney());
                        unfreezeCoin = order.getQuoteCoin();
                    } else {  // 市价卖单
                        unfreezeAmount = unfilledAmount;
                        unfreezeCoin = order.getBaseCoin();
                    }
                }
                
                if (unfreezeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // 调用 Wallet 服务解冻余额（在分布式事务中）
                    walletClient.unfreezeBalance(
                        order.getUserId(),
                        unfreezeCoin,
                        unfreezeAmount,
                        order.getOrderNo(),
                        orderDTO.getStatus() == 3 ? "订单取消" : "订单完成"
                    );
                    log.info("解冻余额成功：orderNo={}, coin={}, amount={}", 
                            orderDTO.getOrderNo(), unfreezeCoin, unfreezeAmount);
                }
            }
        }
        
        // 3. 更新订单状态（在分布式事务中）
        updateOrderFilled(
            orderDTO.getOrderNo(),
            orderDTO.getFilledAmount(),
            orderDTO.getFilledMoney(),
            BigDecimal.ZERO,  // TODO: 从配置获取手续费
            orderDTO.getStatus()
        );
        
        log.info("订单完成处理成功（分布式事务）：orderNo={}, status={}", 
                orderDTO.getOrderNo(), orderDTO.getStatus());
    }

    /**
     * 处理成交记录（分布式事务）
     * 
     * 【作用】
     * 用于 MQ 消费者调用，处理成交记录，包含：
     * 1. 更新买方订单成交信息
     * 2. 更新卖方订单成交信息
     * 3. 处理买方资产变更（扣减冻结计价币，增加基础币）
     * 4. 处理卖方资产变更（扣减冻结基础币，增加计价币）
     * 5. 保存成交记录到数据库
     * 
     * 【事务处理】
     * - 使用 @GlobalTransactional 保证分布式事务一致性
     * - 如果任何一步失败，整个事务回滚
     * 
     * @param tradeRecordDTO 成交记录DTO
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void processTradeRecord(TradeRecordDTO tradeRecordDTO) {
        log.info("处理成交记录（分布式事务）：tradeId={}, symbol={}, price={}, amount={}", 
                tradeRecordDTO.getTradeId(), tradeRecordDTO.getSymbol(), 
                tradeRecordDTO.getPrice(), tradeRecordDTO.getAmount());
        
        // 1. 获取交易对信息（用于获取基础币和计价币）
        TradeSymbol symbol = symbolMapper.selectBySymbol(tradeRecordDTO.getSymbol());
        if (symbol == null) {
            log.error("交易对不存在：{}", tradeRecordDTO.getSymbol());
            throw new RuntimeException("交易对不存在：" + tradeRecordDTO.getSymbol());
        }
        
        // 2. 更新买方订单成交信息
        updateOrderFromTrade(tradeRecordDTO.getBuyOrderNo(), tradeRecordDTO.getPrice(), 
                          tradeRecordDTO.getAmount(), tradeRecordDTO.getMoney(), tradeRecordDTO.getBuyFee());
        
        // 3. 更新卖方订单成交信息
        updateOrderFromTrade(tradeRecordDTO.getSellOrderNo(), tradeRecordDTO.getPrice(), 
                          tradeRecordDTO.getAmount(), tradeRecordDTO.getMoney(), tradeRecordDTO.getSellFee());
        
        // 4. 处理买方资产变更
        processBuyerBalanceChange(tradeRecordDTO, symbol);
        
        // 5. 处理卖方资产变更
        processSellerBalanceChange(tradeRecordDTO, symbol);
        
        // 6. 保存成交记录到数据库
        saveTradeRecord(tradeRecordDTO, symbol);
        
        log.info("成交记录处理完成（分布式事务）：tradeId={}", tradeRecordDTO.getTradeId());
    }
    
    /**
     * 从成交记录更新订单
     */
    private void updateOrderFromTrade(String orderNo, BigDecimal price, BigDecimal amount, 
                                     BigDecimal money, BigDecimal fee) {
        TradeOrder order = getByOrderNo(orderNo);
        if (order == null) {
            log.warn("订单不存在：{}", orderNo);
            return;
        }
        
        // 计算新的成交信息
        BigDecimal newFilledAmount = order.getFilledAmount().add(amount);
        BigDecimal newFilledMoney = order.getFilledMoney().add(money);
        BigDecimal newFee = order.getFee().add(fee != null ? fee : BigDecimal.ZERO);
        
        // 判断订单状态
        Integer status;
        if (newFilledAmount.compareTo(order.getAmount()) >= 0) {
            status = 2; // 完全成交
        } else if (newFilledAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = 1; // 部分成交
        } else {
            status = 0; // 待成交
        }
        
        // 更新订单
        updateOrderFilled(orderNo, newFilledAmount, newFilledMoney, newFee, status);
    }
    
    /**
     * 处理买方资产变更
     */
    private void processBuyerBalanceChange(TradeRecordDTO tradeRecord, TradeSymbol symbol) {
        // 计算需要扣减的冻结计价币（成交金额 + 手续费）
        BigDecimal frozenAmount = tradeRecord.getMoney().add(
            tradeRecord.getBuyFee() != null ? tradeRecord.getBuyFee() : BigDecimal.ZERO
        );
        
        // 扣减冻结的计价币（USDT）
        walletClient.decreaseFrozen(
            tradeRecord.getBuyUserId(),
            symbol.getQuoteCoin(),  // 计价币（USDT）
            frozenAmount,
            tradeRecord.getBuyOrderNo(),
            "买入成交扣减"
        );
        
        // 增加基础币（BTC）
        walletClient.increaseBalance(
            tradeRecord.getBuyUserId(),
            symbol.getBaseCoin(),  // 基础币（BTC）
            tradeRecord.getAmount(),
            tradeRecord.getBuyOrderNo(),
            "买入成交到账"
        );
        
        log.info("买方资产变更完成：userId={}, 扣减冻结{}={}, 增加{}={}", 
                tradeRecord.getBuyUserId(), 
                symbol.getQuoteCoin(), frozenAmount,
                symbol.getBaseCoin(), tradeRecord.getAmount());
    }
    
    /**
     * 处理卖方资产变更
     */
    private void processSellerBalanceChange(TradeRecordDTO tradeRecord, TradeSymbol symbol) {
        // 扣减冻结的基础币（BTC）
        walletClient.decreaseFrozen(
            tradeRecord.getSellUserId(),
            symbol.getBaseCoin(),  // 基础币（BTC）
            tradeRecord.getAmount(),
            tradeRecord.getSellOrderNo(),
            "卖出成交扣减"
        );
        
        // 计算实际到账金额（成交金额 - 手续费）
        BigDecimal receivedAmount = tradeRecord.getMoney().subtract(
            tradeRecord.getSellFee() != null ? tradeRecord.getSellFee() : BigDecimal.ZERO
        );
        
        // 增加计价币（USDT）
        walletClient.increaseBalance(
            tradeRecord.getSellUserId(),
            symbol.getQuoteCoin(),  // 计价币（USDT）
            receivedAmount,
            tradeRecord.getSellOrderNo(),
            "卖出成交到账"
        );
        
        log.info("卖方资产变更完成：userId={}, 扣减冻结{}={}, 增加{}={}", 
                tradeRecord.getSellUserId(), 
                symbol.getBaseCoin(), tradeRecord.getAmount(),
                symbol.getQuoteCoin(), receivedAmount);
    }
    
    /**
     * 保存成交记录到数据库
     */
    private void saveTradeRecord(TradeRecordDTO tradeRecordDTO, TradeSymbol symbol) {
        TradeRecord tradeRecord = new TradeRecord();
        tradeRecord.setTradeId(tradeRecordDTO.getTradeId());
        tradeRecord.setSymbol(tradeRecordDTO.getSymbol());
        
        // 获取订单ID（通过订单号查询）
        TradeOrder buyOrder = getByOrderNo(tradeRecordDTO.getBuyOrderNo());
        TradeOrder sellOrder = getByOrderNo(tradeRecordDTO.getSellOrderNo());
        
        if (buyOrder != null) {
            tradeRecord.setBuyOrderId(buyOrder.getId());
        }
        if (sellOrder != null) {
            tradeRecord.setSellOrderId(sellOrder.getId());
        }
        
        tradeRecord.setBuyUserId(tradeRecordDTO.getBuyUserId());
        tradeRecord.setSellUserId(tradeRecordDTO.getSellUserId());
        tradeRecord.setPrice(tradeRecordDTO.getPrice());
        tradeRecord.setAmount(tradeRecordDTO.getAmount());
        tradeRecord.setMoney(tradeRecordDTO.getMoney());
        tradeRecord.setBuyFee(tradeRecordDTO.getBuyFee());
        tradeRecord.setSellFee(tradeRecordDTO.getSellFee());
        tradeRecord.setFeeCoin(symbol.getQuoteCoin());  // 手续费币种通常是计价币
        
        // 格式化交易时间
        if (tradeRecordDTO.getTradeTime() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tradeRecord.setTradeTime(sdf.format(tradeRecordDTO.getTradeTime()));
        } else {
            tradeRecord.setTradeTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        
        tradeRecordMapper.insert(tradeRecord);
        log.info("成交记录已保存到数据库：tradeId={}", tradeRecordDTO.getTradeId());
    }

    /**
     * 参数校验
     */
    private void validateOrderParams(Integer orderType, Integer side, BigDecimal price, BigDecimal amount) {
        if (orderType == null || (orderType != 1 && orderType != 2)) {
            throw new RuntimeException("订单类型错误");
        }
        if (side == null || (side != 1 && side != 2)) {
            throw new RuntimeException("买卖方向错误");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        // 限价单价格必须大于0
        if (orderType == 1 && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("限价单价格必须大于0");
        }
    }

    /**
     * 校验交易对配置
     */
    private void validateSymbolConfig(TradeSymbol symbol, Integer orderType, Integer side, 
                                      BigDecimal price, BigDecimal amount) {
        // 检查市价买卖是否启用
        if (orderType == 2) {
            if (side == 1 && symbol.getEnableMarketBuy() == 0) {
                throw new RuntimeException("该交易对不支持市价买入");
            }
            if (side == 2 && symbol.getEnableMarketSell() == 0) {
                throw new RuntimeException("该交易对不支持市价卖出");
            }
        }

        // 检查数量范围
        if (amount.compareTo(symbol.getMinTradeAmount()) < 0) {
            throw new RuntimeException("数量小于最小交易量：" + symbol.getMinTradeAmount());
        }
        if (symbol.getMaxTradeAmount() != null && symbol.getMaxTradeAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(symbol.getMaxTradeAmount()) > 0) {
                throw new RuntimeException("数量超过最大交易量：" + symbol.getMaxTradeAmount());
            }
        }

        // 限价单检查价格范围
        if (orderType == 1) {
            if (symbol.getMinSellPrice() != null && side == 2 && price.compareTo(symbol.getMinSellPrice()) < 0) {
                throw new RuntimeException("卖出价格低于最低限价：" + symbol.getMinSellPrice());
            }
            if (symbol.getMaxBuyPrice() != null && side == 1 && price.compareTo(symbol.getMaxBuyPrice()) > 0) {
                throw new RuntimeException("买入价格高于最高限价：" + symbol.getMaxBuyPrice());
            }
        }

        // 检查最小成交额
        if (orderType == 1 && symbol.getMinTurnover() != null) {
            BigDecimal turnover = price.multiply(amount);
            if (turnover.compareTo(symbol.getMinTurnover()) < 0) {
                throw new RuntimeException("成交额小于最小限额：" + symbol.getMinTurnover());
            }
        }
    }

    /**
     * 计算需要冻结的金额
     */
    private BigDecimal calculateFreezeAmount(Integer orderType, Integer side, BigDecimal price, BigDecimal amount) {
        if (orderType == 1) {  // 限价单
            if (side == 1) {  // 买入：冻结 价格×数量 的计价币
                return price.multiply(amount);
            } else {  // 卖出：冻结 数量 的基础币
                return amount;
            }
        } else {  // 市价单
            if (side == 1) {  // 市价买入：冻结 金额 的计价币
                return amount;
            } else {  // 市价卖出：冻结 数量 的基础币
                return amount;
            }
        }
    }

    /**
     * 获取需要冻结的币种
     */
    private String getFreezeCoin(Integer side, TradeSymbol symbol) {
        if (side == 1) {  // 买入：冻结计价币（USDT）
            return symbol.getQuoteCoin();
        } else {  // 卖出：冻结基础币（BTC）
            return symbol.getBaseCoin();
        }
    }

    /**
     * 计算撤单需要解冻的金额
     */
    private BigDecimal calculateUnfreezeAmount(TradeOrder order) {
        BigDecimal unfilledAmount = order.getAmount().subtract(order.getFilledAmount());
        
        if (order.getOrderType() == 1) {  // 限价单
            if (order.getSide() == 1) {  // 限价买单：未成交金额
                return order.getPrice().multiply(unfilledAmount);
            } else {  // 限价卖单：未成交数量
                return unfilledAmount;
            }
        } else {  // 市价单
            if (order.getSide() == 1) {  // 市价买单：未成交金额
                return order.getAmount().subtract(order.getFilledMoney());
            } else {  // 市价卖单：未成交数量
                return unfilledAmount;
            }
        }
    }

    /**
     * 构建订单对象
     */
    private TradeOrder buildOrder(Long userId, TradeSymbol symbol, Integer orderType, Integer side, 
                                 BigDecimal price, BigDecimal amount, String clientOrderId) {
        TradeOrder order = new TradeOrder();
        
        // 生成订单号：E + 时间戳 + 随机6位
        String orderNo = "E" + System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setSymbol(symbol.getSymbol());
        order.setBaseCoin(symbol.getBaseCoin());
        order.setQuoteCoin(symbol.getQuoteCoin());
        order.setOrderType(orderType);
        order.setSide(side);
        order.setPrice(orderType == 2 ? BigDecimal.ZERO : price); // 市价单价格为0
        order.setAmount(amount);
        order.setFilledAmount(BigDecimal.ZERO);
        order.setFilledMoney(BigDecimal.ZERO);
        order.setAvgPrice(BigDecimal.ZERO);
        order.setStatus(0); // 待成交
        order.setFee(BigDecimal.ZERO);
        order.setFeeCoin(side == 1 ? symbol.getBaseCoin() : symbol.getQuoteCoin());
        order.setSource(1); // Web
        order.setClientOrderId(clientOrderId);
        order.setUseDiscount(0);
        
        return order;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待成交";
            case 1: return "部分成交";
            case 2: return "完全成交";
            case 3: return "已撤销";
            case 4: return "超时";
            default: return "未知";
        }
    }
    
    /**
     * 发送订单到撮合引擎
     */
    private void sendToMatchingEngine(TradeOrder order) {
        try {
            // 转换为OrderDTO
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderNo(order.getOrderNo());
            orderDTO.setUserId(order.getUserId());
            orderDTO.setSymbol(order.getSymbol());
            orderDTO.setOrderType(order.getOrderType());
            orderDTO.setSide(order.getSide());
            orderDTO.setPrice(order.getPrice());
            orderDTO.setAmount(order.getAmount());
            orderDTO.setFilledAmount(order.getFilledAmount());
            orderDTO.setFilledMoney(order.getFilledMoney());
            orderDTO.setFeeRate(BigDecimal.ZERO);  // TODO: 从配置获取
            orderDTO.setFeeCoin(order.getFeeCoin());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setCreateTime(System.currentTimeMillis());
            
            String json = JSON.toJSONString(orderDTO);
            streamBridge.send("order-input", MessageBuilder.withPayload(json).build());
            log.info("订单已发送到撮合引擎：orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("发送订单到撮合引擎失败：orderNo={}", order.getOrderNo(), e);
        }
    }
    
    /**
     * 发送撤单请求到撮合引擎
     */
    private void sendCancelToMatchingEngine(TradeOrder order) {
        try {
            // 转换为OrderDTO
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderNo(order.getOrderNo());
            orderDTO.setUserId(order.getUserId());
            orderDTO.setSymbol(order.getSymbol());
            orderDTO.setOrderType(order.getOrderType());
            orderDTO.setSide(order.getSide());
            orderDTO.setPrice(order.getPrice());
            orderDTO.setAmount(order.getAmount());
            orderDTO.setFilledAmount(order.getFilledAmount());
            orderDTO.setFilledMoney(order.getFilledMoney());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setCreateTime(order.getCreateTime() != null ? 
                    order.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis());
            
            String json = JSON.toJSONString(orderDTO);
            streamBridge.send("order-cancel-input", MessageBuilder.withPayload(json).build());
            log.info("撤单请求已发送到撮合引擎：orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("发送撤单请求到撮合引擎失败：orderNo={}", order.getOrderNo(), e);
        }
    }
}

