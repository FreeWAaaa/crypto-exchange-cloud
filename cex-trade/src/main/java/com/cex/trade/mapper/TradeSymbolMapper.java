package com.cex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.trade.domain.entity.TradeSymbol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易对Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface TradeSymbolMapper extends BaseMapper<TradeSymbol> {

    /**
     * 根据交易对名称查询
     */
    @Select("SELECT * FROM trade_symbol WHERE symbol = #{symbol} AND deleted = 0")
    TradeSymbol selectBySymbol(@Param("symbol") String symbol);

    /**
     * 查询所有启用的交易对
     */
    @Select("SELECT * FROM trade_symbol WHERE status = 0 AND deleted = 0 ORDER BY sort ASC")
    List<TradeSymbol> selectEnabled();

    /**
     * 查询热门交易对
     */
    @Select("SELECT * FROM trade_symbol WHERE is_hot = 1 AND status = 0 AND deleted = 0 ORDER BY sort ASC")
    List<TradeSymbol> selectHot();

    /**
     * 查询推荐交易对
     */
    @Select("SELECT * FROM trade_symbol WHERE is_recommend = 1 AND status = 0 AND deleted = 0 ORDER BY sort ASC")
    List<TradeSymbol> selectRecommend();
}
