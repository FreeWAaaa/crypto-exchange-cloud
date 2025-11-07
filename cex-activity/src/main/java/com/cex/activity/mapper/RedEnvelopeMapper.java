package com.cex.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.activity.domain.entity.RedEnvelope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 红包Mapper
 * 
 * @author cex
 */
@Mapper
public interface RedEnvelopeMapper extends BaseMapper<RedEnvelope> {
    
    /**
     * 根据红包编号查询
     */
    @Select("SELECT * FROM red_envelope WHERE envelope_no = #{envelopeNo} AND deleted = 0")
    RedEnvelope selectByEnvelopeNo(String envelopeNo);
}

