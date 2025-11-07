package com.cex.admin.controller;

import com.cex.admin.client.UserFeignClient;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 实名认证管理控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/verification")
@RequiredArgsConstructor
@Api(tags = "实名认证管理")
public class VerificationManageController {
    
    private final UserFeignClient userFeignClient;
    
    /**
     * 实名认证列表
     */
    @GetMapping("/list")
    @ApiOperation("查询实名认证列表")
    public Result<Object> getVerificationList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询实名认证列表，参数：{}", params);
        return userFeignClient.getVerificationList(params);
    }
    
    /**
     * 审核实名认证
     */
    @PostMapping("/audit")
    @ApiOperation("审核实名认证")
    public Result<Void> auditVerification(@RequestParam Long id,
                                           @RequestParam Integer status,
                                           @RequestParam(required = false) String remark) {
        log.info("管理员审核实名认证，id：{}，status：{}，remark：{}", id, status, remark);
        return userFeignClient.auditVerification(id, status, remark);
    }
}

