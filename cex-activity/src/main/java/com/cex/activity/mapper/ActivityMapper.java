package com.cex.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.activity.domain.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动Mapper接口
 * 
 * @author cex
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

}
