package com.cex.user.service;

import com.cex.user.domain.dto.LoginResponseDTO;
import com.cex.user.domain.dto.UserRegisterByEmailDTO;
import com.cex.user.domain.dto.UserRegisterByPhoneDTO;

/**
 * 用户注册服务接口
 * 
 * @author cex
 */
public interface UserRegisterService {

    /**
     * 手机号注册
     * 
     * @param dto 注册信息
     * @param clientIp 客户端IP
     * @return 登录响应（含Token）
     */
    LoginResponseDTO registerByPhone(UserRegisterByPhoneDTO dto, String clientIp);

    /**
     * 邮箱注册
     * 
     * @param dto 注册信息
     * @param clientIp 客户端IP
     * @return 登录响应（含Token）
     */
    LoginResponseDTO registerByEmail(UserRegisterByEmailDTO dto, String clientIp);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExist(String username);

    /**
     * 检查手机号是否存在
     * 
     * @param mobile 手机号
     * @return 是否存在
     */
    boolean isMobileExist(String mobile);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExist(String email);

    /**
     * 验证邀请码是否有效
     * 
     * @param inviteCode 邀请码
     * @return 是否有效
     */
    boolean isInviteCodeValid(String inviteCode);
}

