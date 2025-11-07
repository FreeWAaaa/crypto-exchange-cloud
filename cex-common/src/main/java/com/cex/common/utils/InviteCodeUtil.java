package com.cex.common.utils;

/**
 * 邀请码生成工具类
 * 参考旧项目 GeneratorUtil.getPromotionCode() 逻辑
 * 
 * @author cex
 */
public class InviteCodeUtil {

    /**
     * 编码字符集（去掉容易混淆的字符：0O, 1I等）
     */
    private static final String CODE_SOURCE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int BASE = CODE_SOURCE.length();

    /**
     * 根据用户ID生成邀请码
     * 格式：U + 7位编码（总长度8位）
     * 
     * @param userId 用户ID
     * @return 8位邀请码，例如：U2A3B4C5
     */
    public static String generate(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return "U" + encodeUserId(userId, 7);
    }

    /**
     * 将用户ID编码为指定长度的字符串
     * 使用32进制编码（A-Z + 2-9，共32个字符）
     * 
     * @param userId 用户ID
     * @param length 编码长度
     * @return 编码后的字符串
     */
    private static String encodeUserId(Long userId, int length) {
        StringBuilder code = new StringBuilder();
        
        // 转换为32进制
        while (userId > 0) {
            int remainder = (int) (userId % BASE);
            code.insert(0, CODE_SOURCE.charAt(remainder));
            userId = userId / BASE;
        }
        
        // 左侧补齐到指定长度
        while (code.length() < length) {
            code.insert(0, CODE_SOURCE.charAt(0));
        }
        
        // 如果超长，只取右侧指定长度
        if (code.length() > length) {
            code = new StringBuilder(code.substring(code.length() - length));
        }
        
        return code.toString();
    }

    /**
     * 解码邀请码，还原用户ID（可选功能，用于验证）
     * 
     * @param inviteCode 邀请码
     * @return 用户ID，失败返回null
     */
    public static Long decode(String inviteCode) {
        if (inviteCode == null || inviteCode.length() != 8 || !inviteCode.startsWith("U")) {
            return null;
        }
        
        try {
            String encoded = inviteCode.substring(1);
            long userId = 0;
            
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                int index = CODE_SOURCE.indexOf(c);
                if (index == -1) {
                    return null;
                }
                userId = userId * BASE + index;
            }
            
            return userId > 0 ? userId : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证邀请码格式是否正确
     * 
     * @param inviteCode 邀请码
     * @return 是否有效
     */
    public static boolean isValid(String inviteCode) {
        return decode(inviteCode) != null;
    }
}

