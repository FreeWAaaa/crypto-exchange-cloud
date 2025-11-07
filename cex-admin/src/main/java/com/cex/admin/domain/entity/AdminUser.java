package com.cex.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员用户实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin_user")
public class AdminUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 盐值 */
    private String salt;

    /** 真实姓名 */
    private String realName;

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 手机号 */
    private String mobile;

    /** 邮箱 */
    private String email;

    /** 状态（0正常 1禁用） */
    private Integer status;

    /** 最后登录时间 */
    private java.util.Date lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 备注 */
    private String remark;
}

