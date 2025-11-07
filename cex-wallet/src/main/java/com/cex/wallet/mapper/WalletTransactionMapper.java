package com.cex.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.wallet.domain.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 钱包流水Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {

    /**
     * 查询用户流水记录
     */
    @Select("SELECT * FROM wallet_transaction WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<WalletTransaction> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户某币种流水
     */
    @Select("SELECT * FROM wallet_transaction WHERE user_id = #{userId} AND coin = #{coin} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<WalletTransaction> selectByUserIdAndCoin(@Param("userId") Long userId, @Param("coin") String coin, @Param("limit") Integer limit);

    /**
     * 查询用户某类型流水
     */
    @Select("SELECT * FROM wallet_transaction WHERE user_id = #{userId} AND flow_type = #{flowType} AND deleted = 0 ORDER BY create_time DESC")
    List<WalletTransaction> selectByUserIdAndType(@Param("userId") Long userId, @Param("flowType") Integer flowType);
}
