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
 * 【作用】
 * 这是撮合引擎的核心类，负责处理单个交易对（如 BTC/USDT）的所有订单撮合。
 * 
 * 【核心功能】
 * 1. 管理订单簿：维护买单队列、卖单队列（限价单、市价单）
 * 2. 执行撮合：按价格优先、时间优先的原则撮合订单
 * 3. 生成成交记录：创建成交记录并发送到 MQ
 * 4. 更新盘口：实时更新买卖盘口信息
 * 
 * 【撮合算法】
 * 这是标准的 CEX（中心化交易所）撮合算法：
 * 1. 价格优先：买单价格高的优先，卖单价格低的优先
 * 2. 时间优先：同价格的订单，先下单的优先
 * 3. 限价单撮合：限价单与限价单撮合，限价单与市价单撮合
 * 4. 市价单撮合：市价单与限价单撮合（按对手盘最优价格）
 * 
 * 【内存处理】
 * - 所有订单簿都在内存中（TreeMap、LinkedList）
 * - 撮合过程完全在内存中进行，速度极快（微秒级）
 * - 撮合完成后，结果通过 MQ 发送，由其他服务负责落库
 * 
 * 【线程安全】
 * - 虽然 CoinTraderFactory 用了 ConcurrentHashMap，但这里还需要 synchronized
 * - 原因：ConcurrentHashMap 只保证 Map 本身的线程安全，不保证 Value 对象的线程安全
 * - 每个 CoinTrader 的订单簿操作都需要 synchronized 保护
 * 
 * 【设计】
 * - 每个交易对（symbol）对应一个 CoinTrader 实例
 * - 不同交易对的订单完全隔离，互不干扰
 * - 通过 CoinTraderFactory 统一管理所有 CoinTrader
 * 
 * @author cex
 */
@Slf4j
@Component
public class CoinTrader {
    /** 交易对名称（如 "BTC/USDT"、"ETH/USDT"） */
    private final String symbol;
    
    /** 交易币种精度（如 BTC 的小数位数，默认 8 位） */
    private int coinScale = 8;
    
    /** 计价币种精度（如 USDT 的小数位数，默认 8 位） */
    private int baseCoinScale = 8;
    
    /**
     * 买入限价订单队列（价格从高到低排序）
     * 
     * 【数据结构】
     * - TreeMap<BigDecimal, MergeOrder>
     *   - Key: 价格（BigDecimal）
     *   - Value: 该价格下的所有订单（MergeOrder，按时间排序）
     * 
     * 【排序规则】
     * - 使用 Comparator.reverseOrder() 实现价格从高到低
     * - 这样撮合时，价格高的买单优先成交
     * 
     * 【例子】
     * ```
     * 50000 -> [订单1, 订单2]  // 价格 50000 的订单列表
     * 49000 -> [订单3]
     * 48000 -> [订单4, 订单5]
     * ```
     * 
     * 【线程安全】
     * - TreeMap 本身不是线程安全的
     * - 所有操作都需要用 synchronized (buyLimitPriceQueue) 保护
     */
    private final TreeMap<BigDecimal, MergeOrder> buyLimitPriceQueue;
    
    /**
     * 卖出限价订单队列（价格从低到高排序）
     * 
     * 【数据结构】
     * - TreeMap<BigDecimal, MergeOrder>
     *   - Key: 价格（BigDecimal）
     *   - Value: 该价格下的所有订单（MergeOrder，按时间排序）
     * 
     * 【排序规则】
     * - 使用 Comparator.naturalOrder() 实现价格从低到高
     * - 这样撮合时，价格低的卖单优先成交
     * 
     * 【例子】
     * ```
     * 51000 -> [订单1, 订单2]  // 价格 51000 的订单列表
     * 52000 -> [订单3]
     * 53000 -> [订单4, 订单5]
     * ```
     * 
     * 【线程安全】
     * - TreeMap 本身不是线程安全的
     * - 所有操作都需要用 synchronized (sellLimitPriceQueue) 保护
     */
    private final TreeMap<BigDecimal, MergeOrder> sellLimitPriceQueue;
    
    /**
     * 买入市价订单队列（按时间排序，FIFO）
     * 
     * 【数据结构】
     * - LinkedList<OrderDTO>
     *   - 按订单到达时间排序（先到先成交）
     * 
     * 【市价单特点】
     * - 没有指定价格，按对手盘最优价格成交
     * - 买单市价单：按卖盘最低价成交
     * - 卖单市价单：按买盘最高价成交
     * 
     * 【线程安全】
     * - LinkedList 本身不是线程安全的
     * - 所有操作都需要用 synchronized (buyMarketQueue) 保护
     */
    private final LinkedList<OrderDTO> buyMarketQueue;
    
    /**
     * 卖出市价订单队列（按时间排序，FIFO）
     * 
     * 【数据结构】
     * - LinkedList<OrderDTO>
     *   - 按订单到达时间排序（先到先成交）
     * 
     * 【线程安全】
     * - LinkedList 本身不是线程安全的
     * - 所有操作都需要用 synchronized (sellMarketQueue) 保护
     */
    private final LinkedList<OrderDTO> sellMarketQueue;
    
    /** 卖盘盘口信息（用于前端展示） */
    private final TradePlate sellTradePlate;
    
    /** 买盘盘口信息（用于前端展示） */
    private final TradePlate buyTradePlate;
    
    /** 是否暂停交易（true=暂停，false=正常） */
    private boolean tradingHalt = false;
    
    /** 是否就绪（true=可以处理订单，false=未初始化完成） */
    private boolean ready = false;
    
    /** 发行类型（用于特殊撮合逻辑，如分摊模式） */
    private PublishType publishType = PublishType.NONE;
    
    /** 清盘时间（用于分摊模式） */
    private String clearTime;
    
    /** 日期格式化器（用于解析清盘时间） */
    private final SimpleDateFormat dateTimeFormat;
    
    /**
     * RocketMQ 消息发送桥接器
     * 
     * 【作用】
     * 用于发送撮合结果到 MQ：
     * - 成交记录（TradeRecord）
     * - 订单完成通知（OrderDTO）
     * - 盘口更新（TradePlate）
     * 
     * 【设置方式】
     * 通过 CoinTraderFactory.getTrader() 创建时设置
     */
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
     * 
     * 【作用】
     * 将限价订单添加到对应的订单簿中。
     * 如果订单没有完全成交，剩余部分会进入订单簿等待撮合。
     * 
     * 【处理流程】
     * 1. 判断订单方向（买入/卖出），选择对应的订单队列
     * 2. 更新盘口信息（用于前端展示）
     * 3. 将订单添加到对应价格的 MergeOrder 中
     * 
     * 【数据结构】
     * - 相同价格的订单会合并到一个 MergeOrder 中
     * - MergeOrder 内部按时间排序（先到先成交）
     * 
     * 【为什么需要 synchronized？】
     * - TreeMap 本身不是线程安全的
     * - 即使 CoinTraderFactory 用了 ConcurrentHashMap，这里也需要同步
     * - 因为 ConcurrentHashMap 只保证 Map 本身的线程安全，不保证 Value 对象的线程安全
     * - 多个线程可能同时操作同一个 CoinTrader 的订单簿
     * 
     * @param exchangeOrder 限价订单
     */
    public void addLimitPriceOrder(OrderDTO exchangeOrder) {
        // 只处理限价单（订单类型 1 = LIMIT_PRICE）
        if (exchangeOrder.getOrderType() != 1) {  // LIMIT_PRICE
            return;
        }
        
        // 根据订单方向选择对应的订单队列和盘口
        TreeMap<BigDecimal, MergeOrder> list;
        if (exchangeOrder.getSide() == 1) {  // BUY（买入）
            list = buyLimitPriceQueue;  // 买入限价单队列
            buyTradePlate.add(exchangeOrder);  // 更新买盘盘口
            if (ready) {
                sendTradePlateMessage(buyTradePlate);  // 发送盘口更新到 MQ
            }
        } else {  // SELL（卖出）
            list = sellLimitPriceQueue;  // 卖出限价单队列
            sellTradePlate.add(exchangeOrder);  // 更新卖盘盘口
            if (ready) {
                sendTradePlateMessage(sellTradePlate);  // 发送盘口更新到 MQ
            }
        }
        
        // ========== 关键：为什么需要 synchronized？ ==========
        // 
        // 【问题】为什么用了 ConcurrentHashMap 还要用 synchronized？
        // 
        // 【答案】
        // 1. ConcurrentHashMap 只保证 Map 本身的线程安全
        //    - 例如：CoinTraderFactory.traders 的 get/put 操作是线程安全的
        //    - 但是：CoinTrader 内部的订单簿（TreeMap、LinkedList）不是线程安全的
        // 
        // 2. 多个线程可能同时操作同一个 CoinTrader
        //    - 虽然每个交易对只有一个 CoinTrader 实例
        //    - 但如果配置了 concurrency > 1，多个线程可能同时处理同一个交易对的订单
        //    - 例如：线程1 处理 BTC/USDT 订单1，线程2 处理 BTC/USDT 订单2（同时）
        // 
        // 3. TreeMap 和 LinkedList 不是线程安全的
        //    - TreeMap 的 put/get/remove 操作不是原子的
        //    - 多个线程同时操作会导致数据不一致
        // 
        // 4. 需要保护复合操作
        //    - 例如：get(price) → 判断是否为 null → put(price, new MergeOrder)
        //    - 这个操作序列需要原子性，否则可能重复创建 MergeOrder
        // 
        // 【例子】
        // ```
        // 线程1：get(50000) → null → put(50000, new MergeOrder)
        // 线程2：get(50000) → null → put(50000, new MergeOrder)  // 重复创建！
        // ```
        // 
        // 【解决方案】
        // 使用 synchronized (list) 保护整个操作序列
        // 这样同一时间只有一个线程能操作订单簿
        synchronized (list) {
            // 获取该价格下的订单列表（MergeOrder）
            MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
            
            if (mergeOrder == null) {
                // 如果该价格还没有订单，创建新的 MergeOrder
                mergeOrder = new MergeOrder();
                mergeOrder.add(exchangeOrder);  // 添加订单
                list.put(exchangeOrder.getPrice(), mergeOrder);  // 放入订单簿
            } else {
                // 如果该价格已有订单，直接添加到 MergeOrder
                mergeOrder.add(exchangeOrder);
            }
        }
    }
    
    /**
     * 添加市价订单到队列
     * 
     * 【作用】
     * 将市价订单添加到对应的市价单队列中。
     * 市价单通常会在撮合时立即与限价单成交，如果还有剩余，才会进入队列。
     * 
     * 【市价单特点】
     * - 没有指定价格，按对手盘最优价格成交
     * - 买单市价单：按卖盘最低价成交
     * - 卖单市价单：按买盘最高价成交
     * 
     * 【数据结构】
     * - LinkedList：按时间排序（FIFO，先到先成交）
     * 
     * 【为什么需要 synchronized？】
     * - LinkedList 本身不是线程安全的
     * - 多个线程可能同时添加市价单
     * - 需要保护 addLast() 操作的原子性
     * 
     * @param exchangeOrder 市价订单
     */
    public void addMarketPriceOrder(OrderDTO exchangeOrder) {
        // 只处理市价单（订单类型 2 = MARKET_PRICE）
        if (exchangeOrder.getOrderType() != 2) {  // MARKET_PRICE
            return;
        }
        log.info("添加市价订单: {}", exchangeOrder.getOrderNo());
        
        // 根据订单方向选择对应的市价单队列
        LinkedList<OrderDTO> list = exchangeOrder.getSide() == 1 ? buyMarketQueue : sellMarketQueue;
        
        // 使用 synchronized 保护 addLast() 操作
        // 原因：LinkedList 不是线程安全的，多个线程同时添加会导致数据不一致
        synchronized (list) {
            list.addLast(exchangeOrder);  // 添加到队列末尾（FIFO）
        }
    }
    
    /**
     * 处理订单（撮合）- 核心撮合算法入口
     * 
     * 【作用】
     * 这是撮合引擎的核心方法，负责处理新订单的撮合。
     * 
     * 【撮合算法流程】
     * 1. 判断订单类型（限价单/市价单）
     * 2. 判断订单方向（买入/卖出）
     * 3. 选择对手盘队列（买单对卖盘，卖单对买盘）
     * 4. 执行撮合：
     *    - 限价单：先与限价单撮合，再与市价单撮合
     *    - 市价单：直接与限价单撮合
     * 5. 如果未完全成交，剩余部分进入订单簿
     * 
     * 【撮合原则】
     * - 价格优先：买单价格高的优先，卖单价格低的优先
     * - 时间优先：同价格的订单，先下单的优先
     * 
     * 【内存处理】
     * - 所有撮合都在内存中进行（TreeMap、LinkedList）
     * - 速度极快（微秒级）
     * - 撮合完成后，结果通过 MQ 发送，由其他服务负责落库
     * 
     * 【例子】
     * ```
     * 新订单：买入 BTC/USDT，限价 50000，数量 1
     * 
     * 1. 选择对手盘：sellLimitPriceQueue（卖盘限价单队列）
     * 2. 查找可匹配订单：
     *    - 51000 -> [订单1]  // 价格太高，不匹配
     *    - 50000 -> [订单2]  // 价格匹配，成交！
     *    - 49000 -> [订单3]  // 价格更低，也可以成交
     * 3. 按价格优先：先与 49000 的订单成交（价格更优）
     * 4. 如果还有剩余，继续与 50000 的订单成交
     * ```
     * 
     * @param exchangeOrder 待撮合的订单
     */
    public void trade(OrderDTO exchangeOrder) {
        // ========== 第一步：检查交易状态 ==========
        // 如果交易暂停，直接返回
        if (tradingHalt) {
            return;
        }
        
        // 检查交易对是否匹配
        if (!symbol.equalsIgnoreCase(exchangeOrder.getSymbol())) {
            log.info("不支持的交易对: {}", exchangeOrder.getSymbol());
            return;
        }
        
        // 检查订单数量是否有效
        // 订单数量必须大于 0，且剩余未成交数量必须大于 0
        if (exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0 
                || exchangeOrder.getAmount().subtract(exchangeOrder.getFilledAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // ========== 第二步：选择对手盘队列 ==========
        // 买入订单 → 对卖盘撮合
        // 卖出订单 → 对买盘撮合
        TreeMap<BigDecimal, MergeOrder> limitPriceOrderList;  // 限价单队列
        LinkedList<OrderDTO> marketPriceOrderList;  // 市价单队列
        
        if (exchangeOrder.getSide() == 1) {  // BUY（买入）
            // 买入订单对卖盘撮合
            limitPriceOrderList = sellLimitPriceQueue;  // 卖盘限价单队列
            marketPriceOrderList = sellMarketQueue;  // 卖盘市价单队列
        } else {  // SELL（卖出）
            // 卖出订单对买盘撮合
            limitPriceOrderList = buyLimitPriceQueue;  // 买盘限价单队列
            marketPriceOrderList = buyMarketQueue;  // 买盘市价单队列
        }
        
        // ========== 第三步：根据订单类型执行撮合 ==========
        if (exchangeOrder.getOrderType() == 2) {  // MARKET_PRICE（市价单）
            // 市价单撮合：直接与限价单撮合
            // 市价单没有指定价格，按对手盘最优价格成交
            // - 买单市价单：按卖盘最低价成交
            // - 卖单市价单：按买盘最高价成交
            matchMarketPriceWithLPList(limitPriceOrderList, exchangeOrder);
            
        } else if (exchangeOrder.getOrderType() == 1) {  // LIMIT_PRICE（限价单）
            // 限价单价格必须大于 0
            if (exchangeOrder.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            
            // 分摊模式特殊处理（特殊业务逻辑，可以忽略）
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
            
            // 限价单撮合流程：
            // 1. 先与限价单撮合（价格优先、时间优先）
            matchLimitPriceWithLPList(limitPriceOrderList, exchangeOrder, false);
            
            // 2. 如果还没交易完，与市价单撮合
            // 限价单可以与市价单撮合，因为市价单没有价格限制
            if (exchangeOrder.getAmount().compareTo(exchangeOrder.getFilledAmount()) > 0) {
                matchLimitPriceWithMPList(marketPriceOrderList, exchangeOrder);
            }
        }
    }
    
    /**
     * 限价单与限价单撮合
     * 
     * 【作用】
     * 这是撮合算法的核心方法，负责限价单与限价单的撮合。
     * 
     * 【撮合算法】
     * 1. 价格优先：
     *    - 买单：从卖盘最低价开始撮合（价格从低到高）
     *    - 卖单：从买盘最高价开始撮合（价格从高到低）
     * 2. 时间优先：
     *    - 同价格的订单，先下单的优先成交
     * 3. 部分成交：
     *    - 如果订单数量不足，可以部分成交
     *    - 剩余部分继续等待撮合
     * 
     * 【内存处理】
     * - 所有订单簿都在内存中（TreeMap）
     * - 撮合过程完全在内存中进行
     * - 撮合完成后，结果通过 MQ 发送
     * 
     * 【为什么需要 synchronized？】
     * - TreeMap 不是线程安全的
     * - 多个线程可能同时操作同一个订单簿
     * - 需要保护整个撮合过程的原子性
     * 
     * @param lpList 对手盘限价单队列（TreeMap<价格, MergeOrder>）
     * @param focusedOrder 待撮合的订单
     * @param canEnterList 如果未完全成交，是否进入订单簿（通常为 false）
     */
    private void matchLimitPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, OrderDTO focusedOrder, boolean canEnterList) {
        // 收集撮合结果
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();  // 成交记录列表
        List<OrderDTO> completedOrders = new ArrayList<>();  // 已完成订单列表
        
        // ========== 关键：为什么需要 synchronized？ ==========
        // 
        // 【原因】
        // 1. TreeMap 不是线程安全的
        //    - iterator()、get()、put()、remove() 等操作不是原子的
        //    - 多个线程同时操作会导致 ConcurrentModificationException
        // 
        // 2. 撮合过程是复合操作
        //    - 遍历订单簿 → 匹配订单 → 更新订单状态 → 移除已完成订单
        //    - 这个操作序列需要原子性，否则可能导致数据不一致
        // 
        // 3. 即使配置了 concurrency: 1，也可能有多个线程
        //    - 虽然单线程消费，但可能有其他线程（如取消订单、查询订单簿等）
        // 
        // 【例子】
        // ```
        // 线程1：遍历订单簿，准备与订单A撮合
        // 线程2：同时取消订单A
        // 结果：线程1 可能操作已删除的订单，导致异常
        // ```
        // 
        // 【解决方案】
        // 使用 synchronized (lpList) 保护整个撮合过程
        // 这样同一时间只有一个线程能操作订单簿
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
     * 处理两个匹配的订单（撮合核心逻辑）
     * 
     * 【作用】
     * 这是撮合算法的核心方法，负责计算两个订单的成交价格、数量，并更新订单状态。
     * 
     * 【撮合规则】
     * 1. 成交价格：
     *    - 如果对手单是限价单，以对手单的价格成交（价格优先）
     *    - 如果对手单是市价单，以本方订单的价格成交
     * 2. 成交数量：
     *    - 取两个订单剩余数量的最小值
     *    - 例如：订单A 剩余 1 BTC，订单B 剩余 0.5 BTC → 成交 0.5 BTC
     * 3. 订单状态：
     *    - 完全成交：filledAmount >= amount → status = 2 (COMPLETED)
     *    - 部分成交：filledAmount < amount → status = 1 (PARTIAL)
     * 
     * 【内存处理】
     * - 所有计算都在内存中进行
     * - 只更新订单对象的状态，不涉及数据库
     * - 成交记录通过 MQ 发送，由其他服务负责落库
     * 
     * 【例子】
     * ```
     * 订单A：买入 BTC/USDT，限价 50000，数量 1 BTC
     * 订单B：卖出 BTC/USDT，限价 49000，数量 0.5 BTC
     * 
     * 撮合结果：
     * - 成交价格：49000（以对手单价格成交，价格优先）
     * - 成交数量：0.5 BTC（取最小值）
     * - 订单A：部分成交（剩余 0.5 BTC）
     * - 订单B：完全成交
     * ```
     * 
     * @param focusedOrder 待撮合的订单（新订单）
     * @param matchOrder 对手盘订单（订单簿中的订单）
     * @return TradeRecordDTO 成交记录（如果成交），null（如果无法成交）
     */
    private TradeRecordDTO processMatch(OrderDTO focusedOrder, OrderDTO matchOrder) {
        BigDecimal needAmount, dealPrice, availAmount;
        
        // ========== 第一步：确定成交价格 ==========
        // 【价格优先原则】
        // - 如果对手单是限价单，以对手单的价格成交（价格优先）
        // - 如果对手单是市价单，以本方订单的价格成交
        // 
        // 【例子】
        // - 买单 50000 与卖单 49000 撮合 → 成交价 49000（以卖单价格成交，更优）
        // - 买单 50000 与卖单 51000 撮合 → 不撮合（价格不匹配）
        if (matchOrder.getOrderType() == 1) {  // LIMIT_PRICE（限价单）
            dealPrice = matchOrder.getPrice();  // 以对手单价格成交
        } else {
            dealPrice = focusedOrder.getPrice();  // 以本方订单价格成交
        }
        
        // 成交价必须大于 0
        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        // ========== 第二步：计算剩余数量 ==========
        // 计算两个订单的剩余未成交数量
        BigDecimal focusedRemaining = focusedOrder.getAmount().subtract(focusedOrder.getFilledAmount());
        BigDecimal matchRemaining = matchOrder.getAmount().subtract(matchOrder.getFilledAmount());
        
        // ========== 第三步：确定需要交易的数量 ==========
        // 特殊处理：买单市价单
        // 买单市价单按金额计算（例如：买入 10000 USDT 的 BTC）
        // 其他订单按数量计算（例如：买入 1 BTC）
        if (focusedOrder.getOrderType() == 2 && focusedOrder.getSide() == 1) {  // MARKET_PRICE BUY
            // 买单市价单：需要交易的数量 = 对手单剩余数量
            needAmount = matchRemaining;
        } else {
            // 其他订单：需要交易的数量 = 本方订单剩余数量
            needAmount = focusedRemaining;
        }
        
        // 可用数量（对手单剩余数量）
        availAmount = matchRemaining;
        
        // ========== 第四步：计算实际成交量 ==========
        // 成交量 = min(需要数量, 可用数量)
        // 取两个订单剩余数量的最小值
        // 
        // 【例子】
        // - 订单A 剩余 1 BTC，订单B 剩余 0.5 BTC → 成交 0.5 BTC
        // - 订单A 剩余 0.3 BTC，订单B 剩余 1 BTC → 成交 0.3 BTC
        BigDecimal tradedAmount = (availAmount.compareTo(needAmount) >= 0 ? needAmount : availAmount);
        
        // 如果成交量为 0，无法成交
        if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        // ========== 第五步：计算成交额 ==========
        // 成交额 = 成交量 × 成交价格
        BigDecimal turnover = tradedAmount.multiply(dealPrice);
        
        // ========== 第六步：更新订单状态 ==========
        // 更新两个订单的已成交数量和已成交金额
        matchOrder.setFilledAmount(matchOrder.getFilledAmount().add(tradedAmount));
        matchOrder.setFilledMoney(matchOrder.getFilledMoney().add(turnover));
        focusedOrder.setFilledAmount(focusedOrder.getFilledAmount().add(tradedAmount));
        focusedOrder.setFilledMoney(focusedOrder.getFilledMoney().add(turnover));
        
        // 判断订单是否完全成交
        // 如果已成交数量 >= 订单数量，则完全成交
        if (matchOrder.getFilledAmount().compareTo(matchOrder.getAmount()) >= 0) {
            matchOrder.setStatus(2);  // 2 = COMPLETED（完全成交）
        } else {
            matchOrder.setStatus(1);  // 1 = PARTIAL（部分成交）
        }
        
        if (focusedOrder.getFilledAmount().compareTo(focusedOrder.getAmount()) >= 0) {
            focusedOrder.setStatus(2);  // 2 = COMPLETED（完全成交）
        } else {
            focusedOrder.setStatus(1);  // 1 = PARTIAL（部分成交）
        }
        
        // ========== 第七步：创建成交记录 ==========
        // 创建成交记录（TradeRecord），用于：
        // 1. 发送到 MQ，由其他服务负责落库
        // 2. 更新用户余额
        // 3. 前端展示成交记录
        TradeRecordDTO tradeRecord = new TradeRecordDTO();
        tradeRecord.setSymbol(symbol);  // 交易对
        tradeRecord.setPrice(dealPrice);  // 成交价格
        tradeRecord.setAmount(tradedAmount);  // 成交数量
        
        // 根据订单方向设置买卖双方信息
        if (focusedOrder.getSide() == 1) {  // BUY（买入）
            tradeRecord.setBuyOrderNo(focusedOrder.getOrderNo());  // 买方订单号
            tradeRecord.setSellOrderNo(matchOrder.getOrderNo());  // 卖方订单号
            tradeRecord.setBuyUserId(focusedOrder.getUserId());  // 买方用户ID
            tradeRecord.setSellUserId(matchOrder.getUserId());  // 卖方用户ID
        } else {  // SELL（卖出）
            tradeRecord.setBuyOrderNo(matchOrder.getOrderNo());  // 买方订单号
            tradeRecord.setSellOrderNo(focusedOrder.getOrderNo());  // 卖方订单号
            tradeRecord.setBuyUserId(matchOrder.getUserId());  // 买方用户ID
            tradeRecord.setSellUserId(focusedOrder.getUserId());  // 卖方用户ID
        }
        
        tradeRecord.setMoney(turnover);  // 成交额
        tradeRecord.setTradeTime(new java.util.Date());  // 成交时间
        
        log.info("撮合成功: price={}, amount={}", dealPrice, tradedAmount);
        
        return tradeRecord;
    }
    
    /**
     * 市价单与限价单撮合
     * 
     * 【作用】
     * 处理市价单与限价单的撮合。
     * 
     * 【市价单特点】
     * - 没有指定价格，按对手盘最优价格成交
     * - 买单市价单：按卖盘最低价成交（从低到高）
     * - 卖单市价单：按买盘最高价成交（从高到低）
     * 
     * 【撮合流程】
     * 1. 从对手盘限价单队列中按价格优先顺序撮合
     * 2. 如果市价单还有剩余，进入市价单队列等待
     * 3. 发送成交结果到 MQ
     * 
     * 【为什么需要 synchronized？】
     * - TreeMap 不是线程安全的
     * - 需要保护整个撮合过程的原子性
     * 
     * @param lpList 对手盘限价单队列
     * @param focusedOrder 市价单订单
     */
    private void matchMarketPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, OrderDTO focusedOrder) {
        // 收集撮合结果
        List<TradeRecordDTO> exchangeTrades = new ArrayList<>();  // 成交记录列表
        List<OrderDTO> completedOrders = new ArrayList<>();  // 已完成订单列表
        
        // 使用 synchronized 保护整个撮合过程
        // 原因：TreeMap 不是线程安全的，需要保护遍历和修改操作的原子性
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
     * 发送成交结果到 MQ
     * 
     * 【作用】
     * 将撮合产生的成交记录发送到 MQ，由其他服务（如 trade 模块）负责：
     * - 更新订单状态到数据库
     * - 更新用户余额
     * - 记录成交历史
     * 
     * 【内存处理】
     * - 撮合在内存中完成，不涉及数据库
     * - 结果通过 MQ 异步发送，由其他服务负责落库
     * - 这样设计可以保证撮合速度（微秒级）
     * 
     * 【批量发送】
     * - 如果成交记录数量 > 1000，分批发送（避免消息过大）
     * - 每批最多 1000 条记录
     * 
     * @param trades 成交记录列表
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
     * 发送订单完成通知到 MQ
     * 
     * 【作用】
     * 将已完成的订单（完全成交或已取消）发送到 MQ，通知其他服务更新订单状态。
     * 
     * 【订单状态】
     * - status = 2：完全成交（COMPLETED）
     * - status = 3：已取消（CANCELED）
     * 
     * 【内存处理】
     * - 订单状态在内存中更新
     * - 通过 MQ 异步通知其他服务更新数据库
     * 
     * @param orders 已完成的订单列表
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
     * 发送盘口变化消息到 MQ
     * 
     * 【作用】
     * 当订单簿发生变化时（新增订单、订单成交、订单取消），更新盘口信息并发送到 MQ。
     * 
     * 【盘口信息】
     * - 买盘：显示前 N 档最优买价和数量
     * - 卖盘：显示前 N 档最优卖价和数量
     * - 用于前端实时展示（类似币安的盘口）
     * 
     * 【为什么需要 synchronized？】
     * - TradePlate 内部的数据结构不是线程安全的
     * - 需要保护读取和发送的原子性
     * 
     * @param plate 盘口信息（买盘或卖盘）
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
     * 
     * 【作用】
     * 从订单簿中移除指定的订单。
     * 
     * 【处理流程】
     * 1. 判断订单类型（限价单/市价单）
     * 2. 从对应的订单队列中查找订单
     * 3. 移除订单
     * 4. 更新盘口信息
     * 
     * 【取消逻辑】
     * - 限价单：从对应价格的 MergeOrder 中移除
     * - 市价单：从市价单队列中移除
     * - 如果订单已部分成交，取消的是剩余未成交部分
     * 
     * 【为什么需要 synchronized？】
     * - TreeMap 和 LinkedList 不是线程安全的
     * - 需要保护查找和移除操作的原子性
     * 
     * @param exchangeOrder 要取消的订单
     * @return OrderDTO 被取消的订单对象（如果找到），null（如果未找到）
     */
    public OrderDTO cancelOrder(OrderDTO exchangeOrder) {
        log.info("取消订单: {}", exchangeOrder.getOrderNo());
        
        if (exchangeOrder.getOrderType() == 2) {  // MARKET_PRICE（市价单）
            // ========== 处理市价单 ==========
            // 从市价单队列中查找并移除
            List<OrderDTO> list = exchangeOrder.getSide() == 1 ? buyMarketQueue : sellMarketQueue;
            
            // 使用 synchronized 保护查找和移除操作
            synchronized (list) {
                Iterator<OrderDTO> orderIterator = list.iterator();
                while (orderIterator.hasNext()) {
                    OrderDTO order = orderIterator.next();
                    // 根据订单号匹配
                    if (order.getOrderNo().equals(exchangeOrder.getOrderNo())) {
                        orderIterator.remove();  // 从队列中移除
                        onRemoveOrder(order);  // 更新盘口信息
                        return order;
                    }
                }
            }
        } else {
            // ========== 处理限价单 ==========
            // 从限价单队列中查找并移除
            TreeMap<BigDecimal, MergeOrder> list = exchangeOrder.getSide() == 1 ? buyLimitPriceQueue : sellLimitPriceQueue;
            
            // 使用 synchronized 保护查找和移除操作
            synchronized (list) {
                // 根据价格获取 MergeOrder
                MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
                if (mergeOrder != null) {
                    Iterator<OrderDTO> orderIterator = mergeOrder.iterator();
                    while (orderIterator.hasNext()) {
                        OrderDTO order = orderIterator.next();
                        // 根据订单号匹配
                        if (order.getOrderNo().equals(exchangeOrder.getOrderNo())) {
                            orderIterator.remove();  // 从 MergeOrder 中移除
                            
                            // 如果该价格下没有订单了，移除整个 MergeOrder
                            if (mergeOrder.size() == 0) {
                                list.remove(exchangeOrder.getPrice());
                            }
                            
                            onRemoveOrder(order);  // 更新盘口信息
                            return order;
                        }
                    }
                }
            }
        }
        return null;  // 未找到订单
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

