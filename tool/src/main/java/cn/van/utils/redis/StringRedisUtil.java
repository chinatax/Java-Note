package cn.van.utils.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: StringRedisUtil
 *
 * @author: Van
 * Date:     2019-11-07 17:05
 * Description: Redis 工具类 for String
 * Version： V1.0
 */
@Component
public class StringRedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 维护一个本类的静态变量
     */
    private static StringRedisUtil stringRedis;

    @PostConstruct
    public void init() {
        stringRedis = this;
        stringRedis.redisTemplate = this.redisTemplate;
    }

    /**
     * 将参数中的字符串值设置为键的值，不设置过期时间
     * @param key
     * @param value
     */
    public static void setValue(String key, String value) {
        stringRedis.redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 将参数中的字符串值设置为键的值，设置过期时间
     * @param key
     * @param value
     * @param timeout 单位：秒
     */
    public static void setValue(String key, String value, long timeout) {
        stringRedis.redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取与指定键相关的值
     * @param key
     * @return
     */
    public static String getValue(String key) {
        return stringRedis.redisTemplate.opsForValue().get(key);
    }

    /**
     * 返回指定 key的剩余生存时间
     * @param key 键值
     */
    public static Long getExpire(String key) {
        return stringRedis.redisTemplate.getExpire(key);
    }

    /**
     * 设置某个键的过期时间
     * @param key 键值
     * @param timeout 过期秒数
     */
    public static boolean expire(String key, Integer timeout) {
        return stringRedis.redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 判断某个键是否存在
     * @param key 键值
     */
    public static boolean hasKey(String key) {
        return stringRedis.redisTemplate.hasKey(key);
    }

    /**
     * 删除指定的键
     * @param key
     * @return
     */
    public static boolean delKey(String key) {
        return stringRedis.redisTemplate.delete(key);
    }
    /**
     * 删除多个键
     * @param keys
     * @return
     */
    public static Long delKey(Collection<String> keys) {
        return stringRedis.redisTemplate.delete(keys);
    }


}
