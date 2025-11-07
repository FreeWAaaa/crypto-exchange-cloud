package com.cex.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录DTO
 * 
 * @author cex
 */
@Data
public class UserLoginDTO {

    /** 用户名/手机号/邮箱 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 */
    private String captcha;

    /** 验证码key */
    private String captchaKey;

    /** 记住我 */
    private Boolean rememberMe;
}
