package com.cex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.trade.domain.entity.TradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成交记录Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface TradeRecordMapper extends BaseMapper<TradeRecord> {
}

