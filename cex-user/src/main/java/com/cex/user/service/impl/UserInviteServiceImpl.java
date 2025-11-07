package com.cex.user.service.impl;

import com.cex.user.domain.dto.UserInviteStatDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserInviteRelation;
import com.cex.user.mapper.UserInviteRelationMapper;
import com.cex.user.mapper.UserMapper;
import com.cex.user.service.UserInviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户邀请服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class UserInviteServiceImpl implements UserInviteService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInviteRelationMapper inviteRelationMapper;

    @Value("${app.promote.prefix:http://localhost:8080/#/register?code=}")
    private String promotePrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInviteRelation(Long userId, Long inviterId) {
        if (userId == null || inviterId == null) {
            log.warn("创建邀请关系失败：userId={}, inviterId={}", userId, inviterId);
            return;
        }

        log.info("开始创建邀请关系：userId={}, inviterId={}", userId, inviterId);

        // 1. 创建一级邀请关系
        createRelation(userId, inviterId, 1);
        userMapper.increaseInviteCount(inviterId, 1);

        // 2. 查询邀请人的上级（二级）
        List<UserInviteRelation> inviterRelations = inviteRelationMapper.selectInviters(inviterId);
        for (UserInviteRelation relation : inviterRelations) {
            if (relation.getInviteLevel() == 1) {
                // 找到邀请人的邀请人，建立二级关系
                Long secondLevelInviterId = relation.getInviterId();
                createRelation(userId, secondLevelInviterId, 2);
                userMapper.increaseInviteCount(secondLevelInviterId, 2);

                // 3. 继续查找三级
                List<UserInviteRelation> secondLevelRelations = inviteRelationMapper.selectInviters(secondLevelInviterId);
                for (UserInviteRelation secondRelation : secondLevelRelations) {
                    if (secondRelation.getInviteLevel() == 1) {
                        // 找到二级的邀请人，建立三级关系
                        Long thirdLevelInviterId = secondRelation.getInviterId();
                        createRelation(userId, thirdLevelInviterId, 3);
                        userMapper.increaseInviteCount(thirdLevelInviterId, 3);
                        break; // 只建立一个三级关系
                    }
                }
                break; // 只建立一个二级关系
            }
        }

        log.info("邀请关系创建成功：userId={}", userId);
    }

    @Override
    public List<User> getInviteList(Long userId, Integer level) {
        List<UserInviteRelation> relations = inviteRelationMapper.selectInviteList(userId, level);
        List<User> users = new ArrayList<>();
        
        for (UserInviteRelation relation : relations) {
            User user = userMapper.selectById(relation.getUserId());
            if (user != null) {
                // 脱敏处理
                user.setPassword(null);
                user.setSalt(null);
                user.setTradePassword(null);
                user.setGoogleAuthSecret(null);
                users.add(user);
            }
        }
        
        return users;
    }

    @Override
    public UserInviteStatDTO getInviteStatistics(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInviteStatDTO dto = new UserInviteStatDTO();
        dto.setInviteCode(user.getInviteCode());
        dto.setFirstLevelCount(user.getFirstLevelCount() != null ? user.getFirstLevelCount() : 0);
        dto.setSecondLevelCount(user.getSecondLevelCount() != null ? user.getSecondLevelCount() : 0);
        dto.setThirdLevelCount(user.getThirdLevelCount() != null ? user.getThirdLevelCount() : 0);
        dto.calculateTotal();
        
        // 生成推广链接
        if (user.getInviteCode() != null) {
            dto.setPromoteUrl(promotePrefix + user.getInviteCode());
        }
        
        return dto;
    }

    @Override
    public List<UserInviteRelation> getUserInviteRelations(Long userId) {
        return inviteRelationMapper.selectInviteList(userId, null);
    }

    /**
     * 创建单条邀请关系记录
     */
    private void createRelation(Long userId, Long inviterId, Integer level) {
        UserInviteRelation relation = new UserInviteRelation();
        relation.setUserId(userId);
        relation.setInviterId(inviterId);
        relation.setInviteLevel(level);
        relation.setRewardStatus(0); // 未发放
        inviteRelationMapper.insert(relation);
        
        log.info("创建{}级邀请关系：userId={}, inviterId={}", level, userId, inviterId);
    }
}

