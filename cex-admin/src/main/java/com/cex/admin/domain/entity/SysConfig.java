package com.cex.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置名称 */
    private String configName;

    /** 配置类型（1系统配置 2业务配置） */
    private Integer configType;

    /** 配置描述 */
    private String description;

    /** 状态（0启用 1禁用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
