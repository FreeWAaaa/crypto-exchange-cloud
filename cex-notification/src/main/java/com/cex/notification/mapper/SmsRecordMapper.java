package com.cex.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.notification.domain.entity.SmsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信记录Mapper
 * 
 * @author cex
 */
@Mapper
public interface SmsRecordMapper extends BaseMapper<SmsRecord> {
}

