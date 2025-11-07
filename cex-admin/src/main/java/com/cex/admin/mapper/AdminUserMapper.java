package com.cex.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cex.admin.domain.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员用户Mapper
 * 
 * @author cex
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    
    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM sys_admin_user WHERE username = #{username} AND deleted = 0")
    AdminUser selectByUsername(String username);
}

