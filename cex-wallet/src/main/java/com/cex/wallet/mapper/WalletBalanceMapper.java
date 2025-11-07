package com.cex.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.wallet.domain.entity.WalletBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包资产Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface WalletBalanceMapper extends BaseMapper<WalletBalance> {

    /**
     * 根据用户ID和币种查询钱包
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @return 钱包对象
     */
    @Select("SELECT * FROM wallet_balance WHERE user_id = #{userId} AND coin = #{coin} AND deleted = 0")
    WalletBalance selectByUserIdAndCoin(@Param("userId") Long userId, @Param("coin") String coin);

    /**
     * 查询用户所有钱包
     * 
     * @param userId 用户ID
     * @return 钱包列表
     */
    @Select("SELECT * FROM wallet_balance WHERE user_id = #{userId} AND deleted = 0 ORDER BY coin")
    List<WalletBalance> selectByUserId(@Param("userId") Long userId);

    /**
     * 增加可用余额（带乐观锁）
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数（0表示版本冲突）
     */
    @Update("UPDATE wallet_balance SET " +
            "available_balance = available_balance + #{amount}, " +
            "total_balance = total_balance + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = 0")
    int increaseBalance(@Param("id") Long id, 
                       @Param("amount") BigDecimal amount, 
                       @Param("version") Integer version);

    /**
     * 减少可用余额（带乐观锁）
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数（0表示余额不足或版本冲突）
     */
    @Update("UPDATE wallet_balance SET " +
            "available_balance = available_balance - #{amount}, " +
            "total_balance = total_balance - #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND available_balance >= #{amount} AND version = #{version} AND deleted = 0")
    int decreaseBalance(@Param("id") Long id, 
                       @Param("amount") BigDecimal amount, 
                       @Param("version") Integer version);

    /**
     * 冻结余额（带乐观锁）
     * 可用余额减少，冻结余额增加，总余额不变
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET " +
            "available_balance = available_balance - #{amount}, " +
            "frozen_balance = frozen_balance + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND available_balance >= #{amount} AND version = #{version} AND deleted = 0")
    int freezeBalance(@Param("id") Long id, 
                     @Param("amount") BigDecimal amount, 
                     @Param("version") Integer version);

    /**
     * 解冻余额（带乐观锁）
     * 冻结余额减少，可用余额增加，总余额不变
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET " +
            "available_balance = available_balance + #{amount}, " +
            "frozen_balance = frozen_balance - #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND frozen_balance >= #{amount} AND version = #{version} AND deleted = 0")
    int unfreezeBalance(@Param("id") Long id, 
                       @Param("amount") BigDecimal amount, 
                       @Param("version") Integer version);

    /**
     * 扣减冻结余额（带乐观锁）
     * 冻结余额减少，总余额减少
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET " +
            "frozen_balance = frozen_balance - #{amount}, " +
            "total_balance = total_balance - #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND frozen_balance >= #{amount} AND version = #{version} AND deleted = 0")
    int decreaseFrozen(@Param("id") Long id, 
                      @Param("amount") BigDecimal amount, 
                      @Param("version") Integer version);

    /**
     * 增加冻结余额（不减少可用余额，用于特殊场景）
     * 
     * @param id 钱包ID
     * @param amount 金额
     * @param version 当前版本号
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET " +
            "frozen_balance = frozen_balance + #{amount}, " +
            "total_balance = total_balance + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = 0")
    int increaseFrozen(@Param("id") Long id, 
                      @Param("amount") BigDecimal amount, 
                      @Param("version") Integer version);

    /**
     * 锁定钱包
     * 
     * @param id 钱包ID
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET is_lock = 1 WHERE id = #{id}")
    int lockWallet(@Param("id") Long id);

    /**
     * 解锁钱包
     * 
     * @param id 钱包ID
     * @return 更新行数
     */
    @Update("UPDATE wallet_balance SET is_lock = 0 WHERE id = #{id}")
    int unlockWallet(@Param("id") Long id);

    /**
     * 查询某币种的总余额（所有用户）
     * 
     * @param coin 币种
     * @return 总余额
     */
    @Select("SELECT COALESCE(SUM(total_balance), 0) FROM wallet_balance WHERE coin = #{coin} AND deleted = 0")
    BigDecimal getTotalBalanceByCoin(@Param("coin") String coin);
}