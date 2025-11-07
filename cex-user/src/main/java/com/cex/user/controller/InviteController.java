package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.dto.UserInviteStatDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserInviteRelation;
import com.cex.user.service.UserInviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户邀请Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user/invite")
public class InviteController {

    @Autowired
    private UserInviteService inviteService;

    /**
     * 获取邀请统计
     */
    @GetMapping("/statistics")
    public Result<UserInviteStatDTO> getInviteStatistics(@RequestParam Long userId) {
        UserInviteStatDTO statistics = inviteService.getInviteStatistics(userId);
        return Result.success(statistics, "查询成功");
    }

    /**
     * 获取邀请列表
     */
    @GetMapping("/list")
    public Result<List<User>> getInviteList(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer level) {
        
        List<User> inviteList = inviteService.getInviteList(userId, level);
        return Result.success(inviteList, "查询成功");
    }

    /**
     * 获取邀请关系详情
     */
    @GetMapping("/relations")
    public Result<List<UserInviteRelation>> getInviteRelations(@RequestParam Long userId) {
        List<UserInviteRelation> relations = inviteService.getUserInviteRelations(userId);
        return Result.success(relations, "查询成功");
    }
}

