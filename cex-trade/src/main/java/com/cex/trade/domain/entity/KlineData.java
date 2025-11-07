package com.cex.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * K线数据实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kline_data")
public class KlineData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 交易对 */
    private String symbol;

    /** 周期（1m, 5m, 15m, 30m, 1h, 4h, 1d） */
    private String period;

    /** 开盘价 */
    private BigDecimal openPrice;

    /** 最高价 */
    private BigDecimal highPrice;

    /** 最低价 */
    private BigDecimal lowPrice;

    /** 收盘价 */
    private BigDecimal closePrice;

    /** 成交量 */
    private BigDecimal volume;

    /** 成交额 */
    private BigDecimal amount;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 成交笔数 */
    private Integer count;
}
