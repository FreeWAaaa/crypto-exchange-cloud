package com.cex.admin.service;

import com.cex.admin.domain.entity.AdminUser;

import java.util.Map;

/**
 * 管理员认证服务接口
 * 
 * @author cex
 */
public interface AdminAuthService {
    
    /**
     * 管理员登录
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 管理员登出
     */
    void logout(String token);
    
    /**
     * 获取管理员信息
     */
    AdminUser getAdminInfo(String token);
    
    /**
     * 修改密码
     */
    void changePassword(String token, String oldPassword, String newPassword);
}

