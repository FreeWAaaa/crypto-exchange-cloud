package com.cex.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.activity.domain.entity.SignRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 签到记录Mapper
 * 
 * @author cex
 */
@Mapper
public interface SignRecordMapper extends BaseMapper<SignRecord> {
    
    /**
     * 查询用户今日是否已签到
     */
    @Select("SELECT COUNT(*) FROM sign_record WHERE user_id = #{userId} AND DATE(sign_date) = CURDATE() AND deleted = 0")
    int countTodaySign(Long userId);
    
    /**
     * 查询用户连续签到天数
     */
    @Select("SELECT IFNULL(MAX(consecutive_days), 0) FROM sign_record WHERE user_id = #{userId} AND deleted = 0")
    int getConsecutiveDays(Long userId);
}

