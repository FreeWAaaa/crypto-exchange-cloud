package com.cex.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码 */
    @JsonIgnore
    private String password;

    /** 密码盐 */
    @JsonIgnore
    private String salt;

    /** 手机号 */
    private String mobile;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 用户状态（0正常 1停用 2冻结） */
    private Integer status;

    /** 是否实名认证（0未认证 1已认证 2审核中 3审核失败） */
    private Integer verified;

    /** 实名认证姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 身份证正面照片 */
    private String idCardFront;

    /** 身份证反面照片 */
    private String idCardBack;

    /** 手持身份证照片 */
    private String idCardHand;

    /** 实名认证时间 */
    private Date verifiedTime;

    /** 邀请码 */
    private String inviteCode;

    /** 邀请人ID */
    private Long inviterId;

    /** 一级邀请人数 */
    private Integer firstLevelCount;

    /** 二级邀请人数 */
    private Integer secondLevelCount;

    /** 三级邀请人数 */
    private Integer thirdLevelCount;

    /** 用户等级（0普通 1VIP1 2VIP2...） */
    private Integer level;

    /** 交易密码 */
    @JsonIgnore
    private String tradePassword;

    /** 是否设置交易密码（0未设置 1已设置） */
    private Integer tradePasswordSet;

    /** 最后登录时间 */
    private Date lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 登录次数 */
    private Integer loginCount;

    /** 是否启用谷歌验证器（0未启用 1已启用） */
    private Integer googleAuthEnabled;

    /** 谷歌验证器密钥 */
    @JsonIgnore
    private String googleAuthSecret;

    /** 是否启用短信验证（0未启用 1已启用） */
    private Integer smsAuthEnabled;

    /** 是否启用邮箱验证（0未启用 1已启用） */
    private Integer emailAuthEnabled;

    /** 用户类型（0普通用户 1VIP用户 2商家） */
    private Integer userType;

    /** 超级合伙人标识（0普通用户 1超级合伙人 2专业超级合伙人） */
    private String superPartner;

    /** 交易次数 */
    private Integer transactions;

    /** 申诉次数 */
    private Integer appealTimes;

    /** 申诉胜诉次数 */
    private Integer appealSuccessTimes;

    /** 商家认证状态（0未认证 1已认证 2审核中 3审核失败） */
    private Integer businessStatus;

    /** 商家认证申请时间 */
    private Date businessApplyTime;

    /** 商家认证通过时间 */
    private Date businessVerifiedTime;

    /** 是否可以发布广告（0否 1是） */
    private Integer canPublishAd;

    /** 是否可以交易（0否 1是） */
    private Integer canTrade;

    /** 是否可以签到（0否 1是） */
    private Integer canSignIn;

    /** JWT Token（用于保持登录状态） */
    @JsonIgnore
    private String token;

    /** Token过期时间 */
    private Date tokenExpireTime;

    /** 国家 */
    private String country;

    /** 城市 */
    private String city;

    /** 地区 */
    private String district;

    /** 备注 */
    private String remark;
}
