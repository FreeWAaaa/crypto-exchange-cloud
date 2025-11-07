package com.cex.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.notification.domain.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站内消息Mapper
 * 
 * @author cex
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
    
    /**
     * 查询用户未读消息
     */
    @Select("SELECT * FROM sys_message WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<SysMessage> selectUnreadByUserId(Long userId);
    
    /**
     * 标记消息为已读
     */
    @Update("UPDATE sys_message SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Long id);
    
    /**
     * 标记用户所有消息为已读
     */
    @Update("UPDATE sys_message SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(Long userId);
}

