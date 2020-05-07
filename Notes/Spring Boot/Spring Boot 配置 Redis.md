# Spring Boot 配置 Redis

> 作为目前最火的的`NoSql`数据库,`Redis`现在已成为后台开发人员的标配，本文主要主要介绍`SpringBoot 2.* `和 `Redis` 的整合。


## 一、`Spring Data Redis`

> 在 `Spring Boot` 中，默认集成的 `Redis` 是 `Spring Data Redis`，默认底层的连接池使用了 `lettuce` ，开发者可以自行修改为自己的熟悉的，例如 `Jedis`。

`Spring Data Redis` 针对 `Redis` 提供了非常方便的操作模板 `RedisTemplate`。

### 1.1 `RedisTemplate`中定义了`5`种数据结构操作

```java 
redisTemplate.opsForValue();　　//操作字符串
redisTemplate.opsForHash();　　 //操作hash
redisTemplate.opsForList();　　 //操作list
redisTemplate.opsForSet();　　  //操作set
redisTemplate.opsForZSet();　 　//操作有序set
```

当然，我们平时用的最多的可能是`StringRedisTemplate`，那这个`StringRedisTemplate`是何许人也？

### 1.2 `StringRedisTemplate`与`RedisTemplate`

1. `StringRedisTemplate`继承自`RedisTemplate`;
1. **两者的数据是不共通的**：`StringRedisTemplate`只能管理`StringRedisTemplate`里面的数据，同样，`RedisTemplate`只能管理`RedisTemplate`中的数据；
1. `RedisTemplate `中存取数据都是字节数组；`StringRedisTemplate `中存取数据都是字符串。

## 二、上手实战

### 2.1 导入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.0</version>
</dependency>
```

> 需要手动引入 commos-pool2 的依赖，因为默认的连接池`lettuce`需要该依赖。

### 2.2 `application.yml`中`redis`参数配置

```yml
spring:
  redis:
    host: 47.98.178.84
    port: 6379
    database: 0
    password: password
    timeout: 60s  # 连接超时时间，2.0 中该参数的类型为Duration，这里在配置的时候需要指明单位
    # 连接池配置，2.0中直接使用jedis或者lettuce配置连接池（使用lettuce，依赖中必须包含commons-pool2包）
    lettuce:
      pool:
        # 最大空闲连接数
        max-idle: 500
        # 最小空闲连接数
        min-idle: 50
        # 等待可用连接的最大时间，负数为不限制
        max-wait:  -1s
        # 最大活跃连接数，负数为不限制
        max-active: -1
```

### 2.3 `Redis`配置类-`RedisConfig`

```java
@Configuration
@ConditionalOnClass(RedisOperations.class)
@EnableCaching
public class RedisConfig {

    /**
     * 采用RedisCacheManager作为缓存管理器
     * @param factory
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 生成一个默认配置，通过config对象即可对缓存进行自定义配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 设置缓存的默认过期时间，也是使用Duration设置
        config = config.entryTtl(Duration.ofMinutes(1))
                .disableCachingNullValues();     // 不缓存空值

        // 设置一个初始化的缓存空间set集合
        Set<String> cacheNames =  new HashSet<>();
        cacheNames.add("my-redis-cache1");
        cacheNames.add("my-redis-cache2");

        // 对每个缓存空间应用不同的配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("my-redis-cache1", config);
        configMap.put("my-redis-cache2", config.entryTtl(Duration.ofSeconds(120)));
        // 使用自定义的缓存配置初始化一个cacheManager
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                // 注意这两句的调用顺序，一定要先调用该方法设置初始化的缓存名，再初始化相关的配置
                .initialCacheNames(cacheNames)
                .withInitialCacheConfigurations(configMap)
                .build();
        return cacheManager;
    }
}
```

> `@ConditionalOnClass(RedisOperations.class)`表示：该配置在 `RedisOperations` 存在的情况下才会生效(即项目中引入了 `Spring Data Redis`)


### 2.4 测试类

```java
@SpringBootTest
@RunWith(SpringRunner.class)
public class StringCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(StringCacheTest.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 测试 StringRedisTemplate
     */
    @Test
    public void stringRedisTemplateTest() {
        stringRedisTemplate.opsForValue().set("name","redis测试");
        String name = stringRedisTemplate.opsForValue().get("name");
        logger.info(name);
        stringRedisTemplate.delete("name");
        name = stringRedisTemplate.opsForValue().get("name");
        logger.info(name);
    }
    /**
     * 测试 RedisTemplate
     */
    @Test
    public void redisTemplateTest() {
        redisTemplate.opsForValue().set("name","redis测试");
        String name = (String) redisTemplate.opsForValue().get("name");
        logger.info(name);
        redisTemplate.delete("name");
        name = (String) redisTemplate.opsForValue().get("name");
        logger.info(name);
    }

}
```

## 三、总结

### 3.1 `Redis` 可视化客户端  `rdm`

> 通常情况下，我们可以在命令行下查看 `Redis` 数据库，但是可视化工具能更真实地让我们看到数据的结构。

这里分享我用的一个`Mac`版的客户端，打开[链接](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-redis/file/redis-desktop-manager-0.8.3-2550.dmg)，点击 **Download** 即可下载。

### 3.2 示例源码

[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-redis/redis-demo)