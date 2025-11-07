package com.cex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.common.utils.InviteCodeUtil;
import com.cex.common.utils.PasswordUtil;
import com.cex.user.domain.dto.LoginResponseDTO;
import com.cex.user.domain.dto.UserRegisterByEmailDTO;
import com.cex.user.domain.dto.UserRegisterByPhoneDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.mapper.UserMapper;
import com.cex.user.service.UserInviteService;
import com.cex.user.service.UserRegisterService;
import com.cex.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 用户注册服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class UserRegisterServiceImpl implements UserRegisterService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInviteService userInviteService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.promote.prefix:http://localhost:8080/#/register?code=}")
    private String promotePrefix;

    private static final String SMS_CODE_PREFIX = "sms:register:";
    private static final String EMAIL_CODE_PREFIX = "email:register:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO registerByPhone(UserRegisterByPhoneDTO dto, String clientIp) {
        // 1. 参数校验
        validateRegisterParams(dto);

        // 2. 验证短信验证码
        validateSmsCode(dto.getMobile(), dto.getSmsCode());

        // 3. 检查用户名、手机号是否已存在
        if (isUsernameExist(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (isMobileExist(dto.getMobile())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 4. 验证邀请码（如果有）
        User inviter = null;
        if (!StringUtils.isEmpty(dto.getInviteCode())) {
            inviter = userMapper.selectByInviteCode(dto.getInviteCode());
            if (inviter == null) {
                throw new RuntimeException("邀请码无效");
            }
        }

        // 5. 创建用户
        User user = buildUserFromPhoneDTO(dto);
        userMapper.insert(user);

        // 6. 生成邀请码
        String inviteCode = InviteCodeUtil.generate(user.getId());
        user.setInviteCode(inviteCode);
        userMapper.updateById(user);

        // 7. 建立邀请关系
        if (inviter != null) {
            userInviteService.createInviteRelation(user.getId(), inviter.getId());
        }

        // 8. 删除验证码
        redisTemplate.delete(SMS_CODE_PREFIX + dto.getMobile());

        // 9. 生成Token并登录
        String token = userService.generateToken(user);
        user.setToken(token);
        
        // Token过期时间（7天）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        user.setTokenExpireTime(calendar.getTime());
        userMapper.updateById(user);

        log.info("用户注册成功：userId={}, username={}, mobile={}", 
                 user.getId(), user.getUsername(), user.getMobile());

        // 10. TODO: 发送注册成功消息到RocketMQ
        // sendRegisterSuccessMessage(user);

        return LoginResponseDTO.fromUser(user, token, promotePrefix);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO registerByEmail(UserRegisterByEmailDTO dto, String clientIp) {
        // 1. 参数校验
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 2. 验证邮箱验证码
        validateEmailCode(dto.getEmail(), dto.getEmailCode());

        // 3. 检查用户名、邮箱是否已存在
        if (isUsernameExist(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (isEmailExist(dto.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 4. 验证邀请码
        User inviter = null;
        if (!StringUtils.isEmpty(dto.getInviteCode())) {
            inviter = userMapper.selectByInviteCode(dto.getInviteCode());
            if (inviter == null) {
                throw new RuntimeException("邀请码无效");
            }
        }

        // 5. 创建用户
        User user = buildUserFromEmailDTO(dto);
        userMapper.insert(user);

        // 6. 生成邀请码
        String inviteCode = InviteCodeUtil.generate(user.getId());
        user.setInviteCode(inviteCode);
        userMapper.updateById(user);

        // 7. 建立邀请关系
        if (inviter != null) {
            userInviteService.createInviteRelation(user.getId(), inviter.getId());
        }

        // 8. 删除验证码
        redisTemplate.delete(EMAIL_CODE_PREFIX + dto.getEmail());

        // 9. 生成Token并登录
        String token = userService.generateToken(user);
        user.setToken(token);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        user.setTokenExpireTime(calendar.getTime());
        userMapper.updateById(user);

        log.info("用户注册成功：userId={}, username={}, email={}", 
                 user.getId(), user.getUsername(), user.getEmail());

        return LoginResponseDTO.fromUser(user, token, promotePrefix);
    }

    @Override
    public boolean isUsernameExist(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    @Override
    public boolean isMobileExist(String mobile) {
        return userMapper.selectByMobile(mobile) != null;
    }

    @Override
    public boolean isEmailExist(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    @Override
    public boolean isInviteCodeValid(String inviteCode) {
        if (StringUtils.isEmpty(inviteCode)) {
            return false;
        }
        return userMapper.selectByInviteCode(inviteCode) != null;
    }

    /**
     * 校验注册参数
     */
    private void validateRegisterParams(UserRegisterByPhoneDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }
        if (dto.getAgreeTerms() == null || !dto.getAgreeTerms()) {
            throw new RuntimeException("请先同意服务条款");
        }
    }

    /**
     * 验证短信验证码
     */
    private void validateSmsCode(String mobile, String code) {
        String cacheCode = (String) redisTemplate.opsForValue().get(SMS_CODE_PREFIX + mobile);
        if (StringUtils.isEmpty(cacheCode)) {
            throw new RuntimeException("验证码不存在或已过期");
        }
        if (!cacheCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
    }

    /**
     * 验证邮箱验证码
     */
    private void validateEmailCode(String email, String code) {
        String cacheCode = (String) redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);
        if (StringUtils.isEmpty(cacheCode)) {
            throw new RuntimeException("验证码不存在或已过期");
        }
        if (!cacheCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
    }

    /**
     * 从手机注册DTO构建User对象
     */
    private User buildUserFromPhoneDTO(UserRegisterByPhoneDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        
        // 生成Salt并加密密码
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encode(dto.getPassword(), salt);
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        
        user.setMobile(dto.getMobile());
        user.setNickname(dto.getUsername());
        user.setStatus(0); // 正常状态
        user.setVerified(0); // 未实名
        user.setLevel(0); // 普通用户
        user.setTradePasswordSet(0); // 未设置交易密码
        user.setLoginCount(0);
        user.setGoogleAuthEnabled(0);
        user.setSmsAuthEnabled(1);
        user.setEmailAuthEnabled(0);
        user.setUserType(0);
        user.setCanTrade(1);
        user.setCanPublishAd(1);
        user.setCanSignIn(1);
        user.setCountry(dto.getCountry());
        
        // 超级合伙人（需要审核）
        if (!StringUtils.isEmpty(dto.getSuperPartner()) && !"0".equals(dto.getSuperPartner())) {
            user.setSuperPartner(dto.getSuperPartner());
            user.setStatus(1); // 待审核状态
        } else {
            user.setSuperPartner("0");
        }
        
        // 邀请人ID（后续建立关系时会设置）
        if (!StringUtils.isEmpty(dto.getInviteCode())) {
            User inviter = userMapper.selectByInviteCode(dto.getInviteCode());
            if (inviter != null) {
                user.setInviterId(inviter.getId());
            }
        }
        
        return user;
    }

    /**
     * 从邮箱注册DTO构建User对象
     */
    private User buildUserFromEmailDTO(UserRegisterByEmailDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encode(dto.getPassword(), salt);
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getUsername());
        user.setStatus(0);
        user.setVerified(0);
        user.setLevel(0);
        user.setTradePasswordSet(0);
        user.setLoginCount(0);
        user.setGoogleAuthEnabled(0);
        user.setSmsAuthEnabled(0);
        user.setEmailAuthEnabled(1);
        user.setUserType(0);
        user.setCanTrade(1);
        user.setCanPublishAd(1);
        user.setCanSignIn(1);
        user.setCountry(dto.getCountry());
        
        if (!StringUtils.isEmpty(dto.getSuperPartner()) && !"0".equals(dto.getSuperPartner())) {
            user.setSuperPartner(dto.getSuperPartner());
            user.setStatus(1);
        } else {
            user.setSuperPartner("0");
        }
        
        if (!StringUtils.isEmpty(dto.getInviteCode())) {
            User inviter = userMapper.selectByInviteCode(dto.getInviteCode());
            if (inviter != null) {
                user.setInviterId(inviter.getId());
            }
        }
        
        return user;
    }
}

