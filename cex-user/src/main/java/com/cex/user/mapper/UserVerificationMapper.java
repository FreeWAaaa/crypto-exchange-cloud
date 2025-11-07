package com.cex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.user.domain.entity.UserVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户实名认证记录Mapper
 * 
 * @author cex
 */
@Mapper
public interface UserVerificationMapper extends BaseMapper<UserVerification> {

    /**
     * 根据用户ID查询认证记录
     * 
     * @param userId 用户ID
     * @return 认证记录
     */
    @Select("SELECT * FROM user_verification WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    UserVerification selectByUserId(@Param("userId") Long userId);
}

