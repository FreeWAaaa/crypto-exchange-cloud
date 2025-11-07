package com.cex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.user.domain.entity.UserPaymentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户支付信息Mapper
 * 
 * @author cex
 */
@Mapper
public interface UserPaymentInfoMapper extends BaseMapper<UserPaymentInfo> {

    /**
     * 查询用户的支付方式列表
     * 
     * @param userId 用户ID
     * @return 支付信息列表
     */
    @Select("SELECT * FROM user_payment_info " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY payment_type, create_time DESC")
    List<UserPaymentInfo> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的某种支付方式
     * 
     * @param userId 用户ID
     * @param paymentType 支付类型
     * @return 支付信息列表
     */
    @Select("SELECT * FROM user_payment_info " +
            "WHERE user_id = #{userId} AND payment_type = #{paymentType} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<UserPaymentInfo> selectByUserIdAndType(@Param("userId") Long userId, 
                                                 @Param("paymentType") Integer paymentType);
}

