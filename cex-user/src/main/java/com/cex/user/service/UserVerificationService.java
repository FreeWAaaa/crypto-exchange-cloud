package com.cex.user.service;

import com.cex.user.domain.dto.UserVerificationDTO;
import com.cex.user.domain.entity.User;

/**
 * 用户实名认证服务接口
 * 
 * @author cex
 */
public interface UserVerificationService {

    /**
     * 提交实名认证
     * 
     * @param userId 用户ID
     * @param dto 认证信息
     */
    void submitVerification(Long userId, UserVerificationDTO dto);

    /**
     * 查询用户认证状态
     * 
     * @param userId 用户ID
     * @return 用户对象（包含认证状态）
     */
    User getVerificationStatus(Long userId);

    /**
     * 审核实名认证（管理后台使用）
     * 
     * @param userId 用户ID
     * @param passed 是否通过
     * @param reason 拒绝原因（不通过时填写）
     */
    void auditVerification(Long userId, Boolean passed, String reason);
}

