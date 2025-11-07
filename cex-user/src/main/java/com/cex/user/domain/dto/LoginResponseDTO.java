package com.cex.user.domain.dto;

import com.cex.user.domain.entity.User;
import lombok.Data;

/**
 * 登录响应DTO
 * 
 * @author cex
 */
@Data
public class LoginResponseDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 实名认证状态
     */
    private Integer verified;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 是否设置交易密码
     */
    private Integer tradePasswordSet;

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token过期时间（毫秒时间戳）
     */
    private Long tokenExpireTime;

    /**
     * 个人推广链接
     */
    private String promoteUrl;

    /**
     * 是否有签到活动
     */
    private Boolean hasSignActivity;

    /**
     * 从User实体转换
     */
    public static LoginResponseDTO fromUser(User user, String token, String promotePrefix) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setMobile(user.getMobile());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setVerified(user.getVerified());
        dto.setLevel(user.getLevel());
        dto.setInviteCode(user.getInviteCode());
        dto.setTradePasswordSet(user.getTradePasswordSet());
        dto.setToken(token);
        
        // Token过期时间（7天后）
        if (user.getTokenExpireTime() != null) {
            dto.setTokenExpireTime(user.getTokenExpireTime().getTime());
        }
        
        // 生成推广链接
        if (promotePrefix != null && user.getInviteCode() != null) {
            dto.setPromoteUrl(promotePrefix + user.getInviteCode());
        }
        
        return dto;
    }
}

