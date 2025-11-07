package com.cex.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cex.common.core.exception.BusinessException;
import com.cex.common.core.util.JwtUtils;
import com.cex.user.domain.dto.UserLoginDTO;
import com.cex.user.domain.dto.UserRegisterDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.mapper.UserMapper;
import com.cex.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO registerDTO) {
        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 验证协议同意
        if (!Boolean.TRUE.equals(registerDTO.getAgreeTerms())) {
            throw new BusinessException("请同意用户协议");
        }

        // 验证短信验证码
        if (!verifySmsCode(registerDTO.getMobile(), registerDTO.getSmsCode())) {
            throw new BusinessException("短信验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (getUserByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (getUserByMobile(registerDTO.getMobile()) != null) {
            throw new BusinessException("手机号已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(JwtUtils.encryptPassword(registerDTO.getPassword()));
        user.setMobile(registerDTO.getMobile());
        user.setNickname(registerDTO.getUsername());
        user.setStatus(0);
        user.setVerified(0);
        user.setLevel(1);
        user.setTradePasswordSet(0);
        user.setLoginCount(0);
        user.setGoogleAuthEnabled(0);
        user.setSmsAuthEnabled(1);
        user.setEmailAuthEnabled(0);
        user.setUserType(0);
        user.setInviteCode(generateInviteCode());

        // 处理邀请关系
        if (StrUtil.isNotBlank(registerDTO.getInviteCode())) {
            User inviter = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getInviteCode, registerDTO.getInviteCode()));
            if (inviter != null) {
                user.setInviterId(inviter.getId());
            }
        }

        save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 查找用户
        User user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            user = getUserByMobile(loginDTO.getUsername());
        }
        if (user == null) {
            user = getUserByEmail(loginDTO.getUsername());
        }

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!JwtUtils.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException("账户已被停用");
        }

        // 更新登录信息
        user.setLastLoginTime(new java.util.Date());
        user.setLoginCount(user.getLoginCount() + 1);
        updateById(user);

        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("mobile", user.getMobile());
        claims.put("verified", user.getVerified());
        claims.put("level", user.getLevel());

        String token = JwtUtils.generateToken(claims);
        log.info("用户登录成功: {}", user.getUsername());
        return token;
    }

    @Override
    public User getUserByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
    }

    @Override
    public User getUserByMobile(String mobile) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getMobile, mobile)
                .eq(User::getDeleted, 0));
    }

    @Override
    public User getUserByEmail(String email) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getDeleted, 0));
    }

    @Override
    public String generateInviteCode() {
        String inviteCode;
        do {
            inviteCode = RandomUtil.randomString(8).toUpperCase();
        } while (getOne(new LambdaQueryWrapper<User>()
                .eq(User::getInviteCode, inviteCode)) != null);
        return inviteCode;
    }

    @Override
    public void sendSmsCode(String mobile) {
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 存储到Redis，5分钟过期
        String key = "sms:code:" + mobile;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        
        // TODO: 调用短信服务发送验证码
        log.info("发送短信验证码到手机号: {}, 验证码: {}", mobile, code);
    }

    @Override
    public boolean verifySmsCode(String mobile, String code) {
        String key = "sms:code:" + mobile;
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (StrUtil.isBlank(storedCode)) {
            return false;
        }
        
        boolean verified = storedCode.equals(code);
        if (verified) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
        }
        
        return verified;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRealName(Long userId, String realName, String idCard) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getVerified() == 1) {
            throw new BusinessException("用户已实名认证");
        }

        // TODO: 调用第三方实名认证接口验证身份证信息
        
        user.setVerified(1);
        user.setRealName(realName);
        user.setIdCard(idCard);
        updateById(user);
        
        log.info("用户实名认证成功: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTradePassword(Long userId, String tradePassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setTradePassword(JwtUtils.encryptPassword(tradePassword));
        user.setTradePasswordSet(1);
        updateById(user);
        
        log.info("用户设置交易密码成功: {}", user.getUsername());
    }

    @Override
    public boolean verifyTradePassword(Long userId, String tradePassword) {
        User user = getById(userId);
        if (user == null || user.getTradePasswordSet() == 0) {
            return false;
        }

        return JwtUtils.verifyPassword(tradePassword, user.getTradePassword());
    }

    @Override
    public String enableGoogleAuth(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // TODO: 生成谷歌验证器密钥
        String secret = "TEMP_SECRET_KEY"; // 临时密钥，实际应该使用Google Authenticator库生成
        
        user.setGoogleAuthEnabled(1);
        user.setGoogleAuthSecret(secret);
        updateById(user);
        
        log.info("用户启用谷歌验证器: {}", user.getUsername());
        return secret;
    }

    @Override
    public boolean verifyGoogleAuth(Long userId, String code) {
        User user = getById(userId);
        if (user == null || user.getGoogleAuthEnabled() == 0) {
            return false;
        }

        // TODO: 验证谷歌验证码
        // 这里需要使用Google Authenticator库来验证code
        
        return true; // 临时返回true
    }

    @Override
    public String generateToken(User user) {
        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("mobile", user.getMobile());
        claims.put("verified", user.getVerified());
        claims.put("level", user.getLevel());
        
        return JwtUtils.generateToken(claims);
    }
}
