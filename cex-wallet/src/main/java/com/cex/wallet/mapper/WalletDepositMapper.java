package com.cex.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.wallet.domain.entity.WalletDeposit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 充值记录Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface WalletDepositMapper extends BaseMapper<WalletDeposit> {

    /**
     * 根据充值单号查询
     */
    @Select("SELECT * FROM wallet_deposit WHERE deposit_no = #{depositNo} AND deleted = 0")
    WalletDeposit selectByDepositNo(@Param("depositNo") String depositNo);

    /**
     * 根据交易哈希查询（防止重复充值）
     */
    @Select("SELECT * FROM wallet_deposit WHERE tx_hash = #{txHash} AND deleted = 0")
    WalletDeposit selectByTxHash(@Param("txHash") String txHash);

    /**
     * 根据地址和交易哈希查询
     */
    @Select("SELECT * FROM wallet_deposit WHERE address = #{address} AND tx_hash = #{txHash} AND deleted = 0")
    WalletDeposit selectByAddressAndTxHash(@Param("address") String address, @Param("txHash") String txHash);

    /**
     * 查询用户充值记录
     */
    @Select("SELECT * FROM wallet_deposit WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<WalletDeposit> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户某币种充值记录
     */
    @Select("SELECT * FROM wallet_deposit WHERE user_id = #{userId} AND coin = #{coin} AND deleted = 0 ORDER BY create_time DESC")
    List<WalletDeposit> selectByUserIdAndCoin(@Param("userId") Long userId, @Param("coin") String coin);
}
