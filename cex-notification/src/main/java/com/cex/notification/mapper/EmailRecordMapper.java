package com.cex.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.notification.domain.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件记录Mapper
 * 
 * @author cex
 */
@Mapper
public interface EmailRecordMapper extends BaseMapper<EmailRecord> {
}

