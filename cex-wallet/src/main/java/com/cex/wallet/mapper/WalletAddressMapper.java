package com.cex.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.wallet.domain.entity.WalletAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 钱包地址Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface WalletAddressMapper extends BaseMapper<WalletAddress> {

    /**
     * 根据用户ID和币种查询充值地址
     */
    @Select("SELECT * FROM wallet_address WHERE user_id = #{userId} AND coin = #{coin} AND address_type = 1 AND deleted = 0")
    WalletAddress selectDepositAddress(@Param("userId") Long userId, @Param("coin") String coin);

    /**
     * 根据地址查询
     */
    @Select("SELECT * FROM wallet_address WHERE address = #{address} AND deleted = 0")
    WalletAddress selectByAddress(@Param("address") String address);

    /**
     * 查询用户所有地址
     */
    @Select("SELECT * FROM wallet_address WHERE user_id = #{userId} AND deleted = 0")
    List<WalletAddress> selectByUserId(@Param("userId") Long userId);
}
