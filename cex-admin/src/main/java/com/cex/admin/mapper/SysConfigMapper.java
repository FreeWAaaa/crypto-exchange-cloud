package com.cex.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.admin.domain.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper
 * 
 * @author cex
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    
    /**
     * 根据配置键查询
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0")
    SysConfig selectByKey(String configKey);
}
