package com.cex.user.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.common.core.domain.Result;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserVerification;
import com.cex.user.mapper.UserMapper;
import com.cex.user.mapper.UserVerificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * 实名认证管理后台接口
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user/admin/verification")
@RequiredArgsConstructor
public class VerificationAdminController {
    
    private final UserVerificationMapper verificationMapper;
    private final UserMapper userMapper;
    
    /**
     * 查询实名认证列表
     */
    @GetMapping("/list")
    public Result<Object> getVerificationList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询实名认证列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户ID
            if (params.get("userId") != null) {
                wrapper.eq(UserVerification::getUserId, Long.parseLong(params.get("userId").toString()));
            }
            // 真实姓名模糊查询
            if (params.get("realName") != null) {
                wrapper.like(UserVerification::getRealName, params.get("realName").toString());
            }
            // 身份证号
            if (params.get("idCard") != null) {
                wrapper.like(UserVerification::getIdCard, params.get("idCard").toString());
            }
            // 状态
            if (params.get("status") != null) {
                wrapper.eq(UserVerification::getStatus, Integer.parseInt(params.get("status").toString()));
            }
        }
        
        wrapper.orderByDesc(UserVerification::getCreateTime);
        
        Page<UserVerification> page = new Page<>(pageNum, pageSize);
        Page<UserVerification> result = verificationMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 审核实名认证
     */
    @PostMapping("/audit")
    public Result<Void> auditVerification(@RequestParam Long id,
                                           @RequestParam Integer status,
                                           @RequestParam(required = false) String remark) {
        log.info("审核实名认证，id：{}，status：{}，remark：{}", id, status, remark);
        
        UserVerification verification = verificationMapper.selectById(id);
        if (verification == null) {
            return Result.error("实名认证记录不存在");
        }
        
        if (verification.getStatus() != 0) {
            return Result.error("该认证已审核，无法重复审核");
        }
        
        // 更新认证状态
        verification.setStatus(status);
        verification.setAuditRemark(remark);
        verificationMapper.updateById(verification);
        
        // 如果审核通过，更新用户信息
        if (status == 1) {  // 审核通过
            User user = userMapper.selectById(verification.getUserId());
            if (user != null) {
                user.setRealName(verification.getRealName());
                user.setIdCard(verification.getIdCard());
                user.setLevel(1);  // 实名认证级别
                user.setVerifiedTime(new Date());
                userMapper.updateById(user);
            }
        }
        
        log.info("实名认证审核完成，id：{}，status：{}", id, status);
        return Result.success();
    }
}

