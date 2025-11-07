package com.cex.common.utils;

import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * 密码工具类
 * 保持与旧项目兼容的MD5+Salt加密方式
 * 
 * @author cex
 */
public class PasswordUtil {

    /**
     * 生成密码盐
     * @return 32位UUID字符串（去掉横线）
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码（小写MD5）
     */
    public static String encode(String rawPassword, String salt) {
        String combined = rawPassword + salt;
        return DigestUtils.md5DigestAsHex(combined.getBytes()).toLowerCase();
    }

    /**
     * 加密密码（别名方法）
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码（小写MD5）
     */
    public static String encrypt(String rawPassword, String salt) {
        return encode(rawPassword, salt);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 已加密的密码
     * @param salt 盐值
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword, String salt) {
        String encoded = encode(rawPassword, salt);
        return encoded.equals(encodedPassword);
    }

    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}

