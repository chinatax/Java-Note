package cn.van.utils.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: StringRedisUtilTest
 *
 * @author: Van
 * Date:     2019-11-07 17:07
 * Description: Redis 工具类 测试
 * Version： V1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class StringRedisUtilTest {

    @Test
    public void setValue() {
        StringRedisUtil.setValue("公众号", "风尘博客");
        String value = StringRedisUtil.getValue("公众号");
        System.out.println(value);

        boolean hasKey = StringRedisUtil.hasKey("公众号");
        System.out.println("hasKey:" + hasKey);

        boolean delKey = StringRedisUtil.delKey("公众号");
        System.out.println("delKey:" + delKey);

        hasKey = StringRedisUtil.hasKey("公众号");
        System.out.println("hasKey:" + hasKey);
    }

    @Test
    public void setValueWithTime() throws InterruptedException {
        StringRedisUtil.setValue("公众号", "风尘博客", 15);
        Long expireTime = StringRedisUtil.getExpire("公众号");
        System.out.println("expireTime:" + expireTime);

        Thread.sleep(15000);
        boolean hasKey = StringRedisUtil.hasKey("公众号");
        System.out.println("hasKey:" + hasKey);

    }

    @Test
    public void expire() {
        StringRedisUtil.setValue("公众号", "风尘博客");
        Long expireTime = StringRedisUtil.getExpire("公众号");
        System.out.println("expireTime:" + expireTime);

        StringRedisUtil.expire("公众号", 10);
        expireTime = StringRedisUtil.getExpire("公众号");
        System.out.println("expireTime:" + expireTime);

    }
}
