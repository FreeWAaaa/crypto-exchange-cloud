package com.cex.admin.service.impl;

import com.cex.admin.domain.entity.AdminUser;
import com.cex.admin.mapper.AdminUserMapper;
import com.cex.admin.service.AdminAuthService;
import com.cex.common.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 管理员认证服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class AdminAuthServiceImpl implements AdminAuthService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String TOKEN_PREFIX = "admin:token:";
    private static final long TOKEN_EXPIRE_HOURS = 8;  // Token 8小时过期
    
    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 查询管理员
        AdminUser admin = adminUserMapper.selectByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 验证状态
        if (admin.getStatus() != 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 3. 验证密码
        String encryptedPassword = PasswordUtil.encrypt(password, admin.getSalt());
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 4. 生成 Token
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 5. 保存到 Redis
        stringRedisTemplate.opsForValue().set(
            TOKEN_PREFIX + token, 
            admin.getId().toString(), 
            TOKEN_EXPIRE_HOURS, 
            TimeUnit.HOURS
        );
        
        // 6. 更新登录信息
        admin.setLastLoginTime(new Date());
        // admin.setLastLoginIp(ip);  // TODO: 获取IP
        adminUserMapper.updateById(admin);
        
        // 7. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("realName", admin.getRealName());
        result.put("roleName", admin.getRoleName());
        
        log.info("管理员登录成功：username={}", username);
        
        return result;
    }
    
    @Override
    public void logout(String token) {
        // 从 Redis 删除 Token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        stringRedisTemplate.delete(TOKEN_PREFIX + token);
        log.info("管理员登出成功");
    }
    
    @Override
    public AdminUser getAdminInfo(String token) {
        // 从 Token 获取管理员ID
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String adminIdStr = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (adminIdStr == null) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        
        Long adminId = Long.parseLong(adminIdStr);
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 清除敏感信息
        admin.setPassword(null);
        admin.setSalt(null);
        
        return admin;
    }
    
    @Override
    public void changePassword(String token, String oldPassword, String newPassword) {
        // 获取当前管理员
        AdminUser admin = getAdminInfo(token);
        
        // 验证旧密码
        String encryptedOldPassword = PasswordUtil.encrypt(oldPassword, admin.getSalt());
        AdminUser fullAdmin = adminUserMapper.selectById(admin.getId());
        if (!encryptedOldPassword.equals(fullAdmin.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码
        String newSalt = UUID.randomUUID().toString().substring(0, 6);
        String encryptedNewPassword = PasswordUtil.encrypt(newPassword, newSalt);
        
        fullAdmin.setPassword(encryptedNewPassword);
        fullAdmin.setSalt(newSalt);
        adminUserMapper.updateById(fullAdmin);
        
        log.info("管理员密码已修改：adminId={}", admin.getId());
    }
}

