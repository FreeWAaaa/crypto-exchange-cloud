package com.cex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     * 
     * @param mobile 手机号
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE mobile = #{mobile} AND deleted = 0")
    User selectByMobile(@Param("mobile") String mobile);

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据邀请码查询用户
     * 
     * @param inviteCode 邀请码
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE invite_code = #{inviteCode} AND deleted = 0")
    User selectByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 更新用户登录信息
     * 
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     * @param loginCount 登录次数
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime}, " +
            "last_login_ip = #{lastLoginIp}, login_count = #{loginCount} " +
            "WHERE id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId,
                       @Param("lastLoginTime") java.util.Date lastLoginTime,
                       @Param("lastLoginIp") String lastLoginIp,
                       @Param("loginCount") Integer loginCount);

    /**
     * 更新用户邀请统计
     * 
     * @param userId 用户ID
     * @param level 层级（1/2/3）
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE sys_user SET " +
            "<if test='level == 1'>first_level_count = first_level_count + 1</if>" +
            "<if test='level == 2'>second_level_count = second_level_count + 1</if>" +
            "<if test='level == 3'>third_level_count = third_level_count + 1</if>" +
            " WHERE id = #{userId}" +
            "</script>")
    int increaseInviteCount(@Param("userId") Long userId, @Param("level") Integer level);
}
