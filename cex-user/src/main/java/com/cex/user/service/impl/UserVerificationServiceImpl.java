package com.cex.user.service.impl;

import com.cex.user.domain.dto.UserVerificationDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserVerification;
import com.cex.user.mapper.UserMapper;
import com.cex.user.mapper.UserVerificationMapper;
import com.cex.user.service.UserVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 用户实名认证服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class UserVerificationServiceImpl implements UserVerificationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private UserVerificationMapper userVerificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitVerification(Long userId, UserVerificationDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已经实名认证
        if (user.getVerified() != null && user.getVerified() == 1) {
            throw new RuntimeException("已完成实名认证，无需重复提交");
        }

        // 检查是否正在审核中
        if (user.getVerified() != null && user.getVerified() == 2) {
            throw new RuntimeException("认证审核中，请勿重复提交");
        }

        // 更新用户认证信息
        user.setRealName(dto.getRealName());
        user.setIdCard(dto.getIdCard());
        user.setIdCardFront(dto.getIdCardFront());
        user.setIdCardBack(dto.getIdCardBack());
        user.setIdCardHand(dto.getIdCardHand());
        user.setVerified(2); // 审核中
        userMapper.updateById(user);

        // 如果有独立的认证记录表，也保存一份
        if (userVerificationMapper != null) {
            UserVerification verification = new UserVerification();
            verification.setUserId(userId);
            verification.setRealName(dto.getRealName());
            verification.setIdCard(dto.getIdCard());
            verification.setIdCardFront(dto.getIdCardFront());
            verification.setIdCardBack(dto.getIdCardBack());
            verification.setIdCardHand(dto.getIdCardHand());
            verification.setStatus(2); // 审核中
            userVerificationMapper.insert(verification);
        }

        log.info("用户提交实名认证：userId={}, realName={}", userId, dto.getRealName());

        // TODO: 发送审核通知给管理员
        // sendAuditNotification(userId);
    }

    @Override
    public User getVerificationStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 脱敏处理
        user.setPassword(null);
        user.setSalt(null);
        user.setTradePassword(null);
        user.setGoogleAuthSecret(null);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVerification(Long userId, Boolean passed, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getVerified() != 2) {
            throw new RuntimeException("该用户未提交认证申请");
        }

        if (passed) {
            // 审核通过
            user.setVerified(1);
            user.setVerifiedTime(new Date());
            log.info("实名认证审核通过：userId={}, realName={}", userId, user.getRealName());
            
            // TODO: 发送认证通过通知
            // sendVerifiedNotification(userId);
        } else {
            // 审核拒绝
            user.setVerified(3);
            user.setRemark(reason); // 拒绝原因记录到备注
            log.info("实名认证审核拒绝：userId={}, reason={}", userId, reason);
            
            // TODO: 发送认证失败通知
            // sendRejectedNotification(userId, reason);
        }

        userMapper.updateById(user);

        // 更新认证记录表
        if (userVerificationMapper != null) {
            UserVerification verification = userVerificationMapper.selectByUserId(userId);
            if (verification != null) {
                verification.setStatus(passed ? 1 : 3);
                // auditTime是String类型，需要格式化
                verification.setAuditTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                verification.setAuditRemark(reason);
                userVerificationMapper.updateById(verification);
            }
        }
    }
}

