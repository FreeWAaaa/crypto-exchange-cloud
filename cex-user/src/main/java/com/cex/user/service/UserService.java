package com.cex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cex.user.domain.dto.UserLoginDTO;
import com.cex.user.domain.dto.UserRegisterDTO;
import com.cex.user.domain.entity.User;

/**
 * 用户服务接口
 * 
 * @author cex
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     */
    User getUserByMobile(String mobile);

    /**
     * 根据邮箱查询用户
     */
    User getUserByEmail(String email);

    /**
     * 生成邀请码
     */
    String generateInviteCode();

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String mobile);

    /**
     * 验证短信验证码
     */
    boolean verifySmsCode(String mobile, String code);

    /**
     * 用户实名认证
     */
    void verifyRealName(Long userId, String realName, String idCard);

    /**
     * 设置交易密码
     */
    void setTradePassword(Long userId, String tradePassword);

    /**
     * 验证交易密码
     */
    boolean verifyTradePassword(Long userId, String tradePassword);

    /**
     * 启用谷歌验证器
     */
    String enableGoogleAuth(Long userId);

    /**
     * 验证谷歌验证码
     */
    boolean verifyGoogleAuth(Long userId, String code);

    /**
     * 生成JWT Token
     */
    String generateToken(User user);
}
