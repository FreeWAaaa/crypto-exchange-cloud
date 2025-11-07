package com.cex.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.activity.domain.entity.SignActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 签到活动Mapper
 * 
 * @author cex
 */
@Mapper
public interface SignActivityMapper extends BaseMapper<SignActivity> {
    
    /**
     * 查询当前进行中的签到活动
     */
    @Select("SELECT * FROM sign_activity WHERE status = 0 AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    SignActivity selectCurrentActivity();
}

