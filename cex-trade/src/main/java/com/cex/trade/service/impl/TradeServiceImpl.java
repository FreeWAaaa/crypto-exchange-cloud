package com.cex.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cex.common.core.exception.BusinessException;
import com.cex.trade.domain.dto.PlaceOrderDTO;
import com.cex.trade.domain.entity.TradeOrder;
import com.cex.trade.domain.entity.TradeSymbol;
import com.cex.trade.mapper.TradeOrderMapper;
import com.cex.trade.mapper.TradeSymbolMapper;
import com.cex.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeService {

    private final TradeSymbolMapper tradeSymbolMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String placeOrder(Long userId, PlaceOrderDTO placeOrderDTO) {
        // 验证交易对
        TradeSymbol symbol = tradeSymbolMapper.selectOne(new LambdaQueryWrapper<TradeSymbol>()
                .eq(TradeSymbol::getSymbol, placeOrderDTO.getSymbol())
                .eq(TradeSymbol::getStatus, 0));
        if (symbol == null) {
            throw new BusinessException("交易对不存在或已停用");
        }

        // 验证限价单价格
        if (placeOrderDTO.getOrderType() == 1 && placeOrderDTO.getPrice() == null) {
            throw new BusinessException("限价单必须填写价格");
        }

        // 验证数量精度
        if (placeOrderDTO.getAmount().scale() > symbol.getAmountPrecision()) {
            throw new BusinessException("数量精度不能超过" + symbol.getAmountPrecision() + "位小数");
        }

        // 验证价格精度
        if (placeOrderDTO.getOrderType() == 1 && placeOrderDTO.getPrice().scale() > symbol.getPricePrecision()) {
            throw new BusinessException("价格精度不能超过" + symbol.getPricePrecision() + "位小数");
        }

        // 验证最小交易数量
        if (placeOrderDTO.getAmount().compareTo(symbol.getMinTradeAmount()) < 0) {
            throw new BusinessException("交易数量不能小于" + symbol.getMinTradeAmount());
        }

        // 验证最大交易数量
        if (placeOrderDTO.getAmount().compareTo(symbol.getMaxTradeAmount()) > 0) {
            throw new BusinessException("交易数量不能大于" + symbol.getMaxTradeAmount());
        }

        // TODO: 验证交易密码
        // TODO: 验证用户资产是否充足

        // 创建订单
        TradeOrder order = new TradeOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setSymbol(placeOrderDTO.getSymbol());
        order.setOrderType(placeOrderDTO.getOrderType());
        order.setSide(placeOrderDTO.getSide());
        order.setPrice(placeOrderDTO.getPrice());
        order.setAmount(placeOrderDTO.getAmount());
        order.setFilledAmount(BigDecimal.ZERO);
        order.setFilledMoney(BigDecimal.ZERO);
        order.setAvgPrice(BigDecimal.ZERO);
        order.setStatus(1); // 待成交
        order.setFee(BigDecimal.ZERO);
        order.setFeeCoin(symbol.getQuoteCoin());
        order.setSource(1); // Web
        order.setClientOrderId(placeOrderDTO.getClientOrderId());

        save(order);

        // TODO: 发送订单到撮合引擎
        // TODO: 冻结用户资产

        log.info("用户下单成功: userId={}, orderNo={}, symbol={}, side={}, amount={}", 
                userId, order.getOrderNo(), placeOrderDTO.getSymbol(), placeOrderDTO.getSide(), placeOrderDTO.getAmount());

        return order.getOrderNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo)
                .eq(TradeOrder::getUserId, userId));
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() == 3) {
            throw new BusinessException("订单已完全成交，无法撤销");
        }

        if (order.getStatus() == 4) {
            throw new BusinessException("订单已撤销");
        }

        // 更新订单状态
        order.setStatus(4); // 已撤销
        order.setCancelTime(new java.util.Date());
        order.setCancelReason("用户主动撤销");
        updateById(order);

        // TODO: 发送撤销指令到撮合引擎
        // TODO: 解冻用户资产

        log.info("用户撤销订单成功: userId={}, orderNo={}", userId, orderNo);
    }

    @Override
    public List<TradeOrder> getUserOrders(Long userId, String symbol, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getUserId, userId)
                .eq(TradeOrder::getDeleted, 0)
                .orderByDesc(TradeOrder::getCreateTime);

        if (StrUtil.isNotBlank(symbol)) {
            wrapper.eq(TradeOrder::getSymbol, symbol);
        }

        if (status != null) {
            wrapper.eq(TradeOrder::getStatus, status);
        }

        Page<TradeOrder> pageParam = new Page<>(page, size);
        return page(pageParam, wrapper).getRecords();
    }

    @Override
    public TradeOrder getOrderDetail(Long userId, String orderNo) {
        return getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo)
                .eq(TradeOrder::getUserId, userId)
                .eq(TradeOrder::getDeleted, 0));
    }

    @Override
    public List<TradeSymbol> getSymbolList() {
        return tradeSymbolMapper.selectList(new LambdaQueryWrapper<TradeSymbol>()
                .eq(TradeSymbol::getStatus, 0)
                .eq(TradeSymbol::getDeleted, 0)
                .orderByAsc(TradeSymbol::getSort));
    }

    @Override
    public TradeSymbol getSymbolDetail(String symbol) {
        return tradeSymbolMapper.selectOne(new LambdaQueryWrapper<TradeSymbol>()
                .eq(TradeSymbol::getSymbol, symbol)
                .eq(TradeSymbol::getStatus, 0)
                .eq(TradeSymbol::getDeleted, 0));
    }

    @Override
    public List<Object> getKlineData(String symbol, String period, Integer limit) {
        // TODO: 实现K线数据获取
        return null;
    }

    @Override
    public Object getDepthData(String symbol, Integer limit) {
        // TODO: 实现深度数据获取
        return null;
    }

    @Override
    public List<Object> getTradeRecords(String symbol, Integer limit) {
        // TODO: 实现成交记录获取
        return null;
    }
}
