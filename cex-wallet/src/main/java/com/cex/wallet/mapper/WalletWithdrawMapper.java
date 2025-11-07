package com.cex.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.wallet.domain.entity.WalletWithdraw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提现记录Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface WalletWithdrawMapper extends BaseMapper<WalletWithdraw> {

    /**
     * 根据提现单号查询
     */
    @Select("SELECT * FROM wallet_withdraw WHERE withdraw_no = #{withdrawNo} AND deleted = 0")
    WalletWithdraw selectByWithdrawNo(@Param("withdrawNo") String withdrawNo);

    /**
     * 查询用户提现记录
     */
    @Select("SELECT * FROM wallet_withdraw WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<WalletWithdraw> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待审核的提现记录
     */
    @Select("SELECT * FROM wallet_withdraw WHERE status = 0 AND deleted = 0 ORDER BY create_time ASC")
    List<WalletWithdraw> selectPendingWithdraws();

    /**
     * 查询用户某币种提现记录
     */
    @Select("SELECT * FROM wallet_withdraw WHERE user_id = #{userId} AND coin = #{coin} AND deleted = 0 ORDER BY create_time DESC")
    List<WalletWithdraw> selectByUserIdAndCoin(@Param("userId") Long userId, @Param("coin") String coin);
}
