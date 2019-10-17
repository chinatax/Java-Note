package cn.van.utils.encryption;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: SummaryEncryptionUtil
 *
 * @author: Van
 * Date:     2019-10-16 20:12
 * Description: 摘要算法加密工具类
 * Version： V1.0
 */
public class SummaryEncryptionUtil {
    /**
     * 生成摘要长度 512 位，理论上越长的摘要越难破解。
     */
    private static final int HASH_BIT_SIZE = 512;

    /**
     * 迭代次数，按照 在RFC2898文案中推荐 的建议，不少以100次
     */
    private static final int ITERATIONS = 2000;

    /**
     *  盐的长度，按照 RFC2898 中的建议，盐的长度不低于64位
     */
    private static final int SALT_BIT_SIZE = 64;

    /**
     * 创建密码摘要
     * @param password
     * @param salt
     * @return
     */
    public static String encryptPasswordHash(String password, String salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.decodeBase64(salt), ITERATIONS, HASH_BIT_SIZE);
        SecretKeyFactory skf ;
        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeBase64String(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成随机盐
     * @return
     */
    public static String genRandomSalt() {
        byte[] salt = new byte[SALT_BIT_SIZE];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(salt);
        return Base64.encodeBase64String(salt);
    }

    /**
     * 验证密码
     * @param password
     * @param salt
     * @param passHash
     * @return
     */
    public static boolean verify(String password, String salt, String passHash){
        String hash = encryptPasswordHash(password, salt);
        return hash.equals(passHash);
    }
}
