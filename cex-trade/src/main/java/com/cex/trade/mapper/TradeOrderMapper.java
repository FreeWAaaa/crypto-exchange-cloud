package com.cex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.trade.domain.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易订单Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM trade_order WHERE order_no = #{orderNo} AND deleted = 0")
    TradeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户当前委托（交易中的订单）
     */
    @Select("SELECT * FROM trade_order WHERE user_id = #{userId} AND status IN (0, 1) AND deleted = 0 ORDER BY create_time DESC")
    List<TradeOrder> selectCurrentOrders(@Param("userId") Long userId);

    /**
     * 查询用户指定交易对的当前委托
     */
    @Select("SELECT * FROM trade_order WHERE user_id = #{userId} AND symbol = #{symbol} AND status IN (0, 1) AND deleted = 0 ORDER BY create_time DESC")
    List<TradeOrder> selectCurrentOrdersBySymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * 查询用户历史委托（已完成/已撤销）
     */
    @Select("SELECT * FROM trade_order WHERE user_id = #{userId} AND status IN (2, 3, 4) AND deleted = 0 ORDER BY create_time DESC LIMIT 100")
    List<TradeOrder> selectHistoryOrders(@Param("userId") Long userId);

    /**
     * 查询用户指定交易对的历史委托
     */
    @Select("SELECT * FROM trade_order WHERE user_id = #{userId} AND symbol = #{symbol} AND status IN (2, 3, 4) AND deleted = 0 ORDER BY create_time DESC")
    List<TradeOrder> selectHistoryOrdersBySymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * 统计用户当前委托数量
     */
    @Select("SELECT COUNT(*) FROM trade_order WHERE user_id = #{userId} AND symbol = #{symbol} AND status IN (0, 1) AND deleted = 0")
    Integer countCurrentOrders(@Param("userId") Long userId, @Param("symbol") String symbol);
}