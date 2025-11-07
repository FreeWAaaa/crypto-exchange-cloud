package com.cex.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.activity.domain.entity.RedEnvelopeDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 红包详情Mapper
 * 
 * @author cex
 */
@Mapper
public interface RedEnvelopeDetailMapper extends BaseMapper<RedEnvelopeDetail> {
    
    /**
     * 查询用户是否已领取红包
     */
    @Select("SELECT COUNT(*) FROM red_envelope_detail WHERE envelope_id = #{envelopeId} AND user_id = #{userId} AND deleted = 0")
    int countUserReceived(Long envelopeId, Long userId);
    
    /**
     * 查询红包领取记录
     */
    @Select("SELECT * FROM red_envelope_detail WHERE envelope_no = #{envelopeNo} AND deleted = 0 ORDER BY create_time DESC")
    List<RedEnvelopeDetail> selectByEnvelopeNo(String envelopeNo);
}

