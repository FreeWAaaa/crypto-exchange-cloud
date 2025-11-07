package com.cex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.user.domain.entity.UserInviteRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户邀请关系Mapper
 * 
 * @author cex
 */
@Mapper
public interface UserInviteRelationMapper extends BaseMapper<UserInviteRelation> {

    /**
     * 查询用户的邀请列表
     * 
     * @param inviterId 邀请人ID
     * @param level 邀请层级（1/2/3，null表示全部）
     * @return 邀请关系列表
     */
    @Select("<script>" +
            "SELECT * FROM user_invite_relation " +
            "WHERE inviter_id = #{inviterId} " +
            "<if test='level != null'> AND invite_level = #{level} </if>" +
            "AND deleted = 0 " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<UserInviteRelation> selectInviteList(@Param("inviterId") Long inviterId, 
                                               @Param("level") Integer level);

    /**
     * 统计邀请人数
     * 
     * @param inviterId 邀请人ID
     * @param level 邀请层级
     * @return 人数
     */
    @Select("SELECT COUNT(*) FROM user_invite_relation " +
            "WHERE inviter_id = #{inviterId} AND invite_level = #{level} AND deleted = 0")
    Integer countByInviterAndLevel(@Param("inviterId") Long inviterId, 
                                    @Param("level") Integer level);

    /**
     * 查询用户的所有上级邀请人
     * 
     * @param userId 用户ID
     * @return 邀请关系列表（按层级排序）
     */
    @Select("SELECT * FROM user_invite_relation " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY invite_level ASC")
    List<UserInviteRelation> selectInviters(@Param("userId") Long userId);
}

