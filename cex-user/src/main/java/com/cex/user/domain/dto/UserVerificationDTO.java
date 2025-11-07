package com.cex.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户实名认证DTO
 * 
 * @author cex
 */
@Data
public class UserVerificationDTO {

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
             message = "身份证号格式不正确")
    private String idCard;

    /**
     * 身份证正面照片URL
     */
    @NotBlank(message = "请上传身份证正面照")
    private String idCardFront;

    /**
     * 身份证反面照片URL
     */
    @NotBlank(message = "请上传身份证反面照")
    private String idCardBack;

    /**
     * 手持身份证照片URL
     */
    @NotBlank(message = "请上传手持身份证照")
    private String idCardHand;
}

