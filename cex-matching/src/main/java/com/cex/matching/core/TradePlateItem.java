package com.cex.matching.core;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘口信息项
 *
 * @author cex
 */
@Data
public class TradePlateItem {
    /** 价格 */
    private BigDecimal price;
    /** 数量 */
    private BigDecimal amount;
}

