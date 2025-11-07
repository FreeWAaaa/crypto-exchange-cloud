package com.cex.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 手机号注册DTO
 * 
 * @author cex
 */
@Data
public class UserRegisterByPhoneDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4-20位字母、数字或下划线")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$", 
             message = "密码必须包含大小写字母和数字，长度8-20位")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    /**
     * 邀请码（可选）
     */
    private String inviteCode;

    /**
     * 国家
     */
    private String country;

    /**
     * 超级合伙人标识（0普通 1超级合伙人）
     */
    private String superPartner;

    /**
     * 是否同意条款
     */
    private Boolean agreeTerms;
}

