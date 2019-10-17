package cn.van.utils.encryption;

import cn.van.utils.encryption.SummaryEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: SummaryEncryptionUtilTest
 *
 * @author: Van
 * Date:     2019-10-16 20:47
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Slf4j
public class SummaryEncryptionUtilTest {

    @Test
    public void summaryEncryptionUtilTest() {
        //原始密码（弱密码）
        String weakPassword = "123456";
        log.info("原始密码（弱密码）是：{}", weakPassword);
        Long startTime = System.currentTimeMillis();
        //生成随机盐
        String salt = SummaryEncryptionUtil.genRandomSalt();
        log.info("生成随机盐为：{},耗时：{}毫秒", salt, System.currentTimeMillis() - startTime);
        //经过加盐后的密码摘要
        String passwordHash = SummaryEncryptionUtil.encryptPasswordHash(weakPassword, salt);
        log.info("经过加盐后的密码摘要为：{}, 生成加盐密码摘要总耗时：{}毫秒", passwordHash, System.currentTimeMillis() - startTime);
        // todo 将密码Hash 和 salt 同时存储到数据库
        log.info("正在将密码和salt 存储到数据库。。。。");
        //验证密码
        startTime = System.currentTimeMillis();
        boolean result = SummaryEncryptionUtil.verify("ddd", salt, passwordHash);
        log.info("输入的密码验证结果：{}, 校验密码耗时：{}毫秒", result , System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        result = SummaryEncryptionUtil.verify("123456", salt, passwordHash);
        log.info("输入的密码验证结果：{}, 校验密码耗时：{}毫秒", result , System.currentTimeMillis() - startTime);
    }
}
