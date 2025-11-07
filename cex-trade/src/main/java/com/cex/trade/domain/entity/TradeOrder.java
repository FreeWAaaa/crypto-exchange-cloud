package com.cex.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_order")
public class TradeOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 交易对符号（如：BTC/USDT） */
    private String symbol;

    /** 基础币种（如：BTC） */
    private String baseCoin;

    /** 计价币种（如：USDT） */
    private String quoteCoin;

    /** 订单类型（1限价单 2市价单） */
    private Integer orderType;

    /** 买卖方向（1买入 2卖出） */
    private Integer side;

    /** 委托价格（市价单为0） */
    private BigDecimal price;

    /** 委托数量 */
    private BigDecimal amount;

    /** 已成交数量 */
    private BigDecimal filledAmount;

    /** 已成交金额 */
    private BigDecimal filledMoney;

    /** 平均成交价 */
    private BigDecimal avgPrice;

    /** 订单状态（0待成交 1部分成交 2完全成交 3已撤销 4超时） */
    private Integer status;

    /** 手续费 */
    private BigDecimal fee;

    /** 手续费币种 */
    private String feeCoin;

    /** 订单来源（1Web 2APP 3API） */
    private Integer source;

    /** 客户端订单ID */
    private String clientOrderId;

    /** 是否使用折扣（0不使用 1使用） */
    private Integer useDiscount;

    /** 撤销时间 */
    private java.util.Date cancelTime;

    /** 撤销原因 */
    private String cancelReason;

    /** 完成时间 */
    private java.util.Date completeTime;

    /** 备注 */
    private String remark;

    /**
     * 判断订单是否已完成
     */
    public boolean isCompleted() {
        // 状态为完全成交或已撤销
        if (status == 2 || status == 3 || status == 4) {
            return true;
        }
        
        // 或者已成交数量达到委托数量
        if (orderType == 1) {  // 限价单
            return amount.compareTo(filledAmount) <= 0;
        } else if (orderType == 2 && side == 1) {  // 市价买单（按金额）
            return amount.compareTo(filledMoney) <= 0;
        } else {  // 市价卖单（按数量）
            return amount.compareTo(filledAmount) <= 0;
        }
    }

    /**
     * 计算平均成交价
     */
    public void calculateAvgPrice() {
        if (filledAmount != null && filledAmount.compareTo(BigDecimal.ZERO) > 0 
            && filledMoney != null && filledMoney.compareTo(BigDecimal.ZERO) > 0) {
            this.avgPrice = filledMoney.divide(filledAmount, 8, BigDecimal.ROUND_DOWN);
        }
    }
}
