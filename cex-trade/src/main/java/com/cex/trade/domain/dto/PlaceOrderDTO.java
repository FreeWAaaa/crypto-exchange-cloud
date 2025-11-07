package com.cex.trade.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 下单DTO
 * 
 * @author cex
 */
@Data
public class PlaceOrderDTO {

    /** 交易对 */
    @NotBlank(message = "交易对不能为空")
    private String symbol;

    /** 订单类型（1限价单 2市价单） */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    /** 买卖方向（1买入 2卖出） */
    @NotNull(message = "买卖方向不能为空")
    private Integer side;

    /** 订单价格（限价单必填） */
    @DecimalMin(value = "0.00000001", message = "价格必须大于0")
    private BigDecimal price;

    /** 订单数量 */
    @NotNull(message = "订单数量不能为空")
    @DecimalMin(value = "0.00000001", message = "数量必须大于0")
    private BigDecimal amount;

    /** 交易密码 */
    @NotBlank(message = "交易密码不能为空")
    private String tradePassword;

    /** 客户端订单ID */
    private String clientOrderId;
}
