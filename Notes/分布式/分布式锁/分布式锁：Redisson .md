# 分布式锁：Redisson


> `Jedis` 是`Redis`的`Java`实现的客户端，其`API`提供了比较全面的`Redis`命令的支持；`Redisson`实现了分布式和可扩展的`Java`数据结构，和`Jedis`相比，功能较为简单，不支持字符串操作，不支持排序、事务、管道、分区等`Redis`特性。`Redisson`的宗旨是促进使用者对`Redis`的关注分离，从而让使用者能够将精力更集中地放在处理业务逻辑上。

`Redisson`框架十分强大，基于`Redisson`框架可以实现几乎你能想到的所有类型的分布式锁，详见[【8. 分布式锁和同步器】](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8)。


## 一、如何加锁？

为了取到锁，客户端应该执行以下操作(`RedLock`算法加锁步骤):

1. 获取当前`Unix`时间，以毫秒为单位;
1. 依次尝试从`5`个实例，使用相同的`key`和具有唯一性的`value`（例如`UUID`）获取锁。当向`Redis`请求获取锁时，客户端应该设置一个网络连接和响应超时时间，这个超时时间应该小于锁的失效时间。例如你的锁自动失效时间为`10`秒，则超时时间应该在`5-50`毫秒之间。这样可以避免服务器端`Redis`已经挂掉的情况下，客户端还在死死地等待响应结果。如果服务器端没有在规定时间内响应，客户端应该尽快尝试去另外一个`Redis`实例请求获取锁;
1. 客户端使用当前时间减去开始获取锁时间（步骤`1`记录的时间）就得到获取锁使用的时间。当且仅当从大多数（`N/2+1`，这里是`3`个节点）的`Redis`节点都取到锁，并且使用的时间小于锁失效时间时，锁才算获取成功;
1. 如果取到了锁，`key`的真正有效时间等于有效时间减去获取锁所使用的时间（步骤`3`计算的结果）。
1. 如果因为某些原因，获取锁失败（没有在至少`N/2+1`个`Redis`实例取到锁或者取锁时间已经超过了有效时间），客户端应该在所有的`Redis`实例上进行解锁（即便某些`Redis`实例根本就没有加锁成功，防止某些节点获取到锁但是客户端没有得到响应而导致接下来的一段时间不能被重新获取锁）。

## 二、如何释放锁？

向所有的`Redis`实例发送释放锁命令即可，不用关心之前有没有从`Redis`实例成功获取到锁.

## 三、实际案例

> 这部分以最常见的案例：抢购时的商品超卖（库存数减少为负数）为例

### 3.1 项目准备


- `good`表

```sql
CREATE TABLE `good` (
                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                      `good_name` varchar(255) NOT NULL COMMENT '商品名称',
                      `good_counts` int(255) NOT NULL COMMENT '商品库存',
                      `create_time` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `good` VALUES (1, '哇哈哈', 5, '2019-09-20 17:39:04');
INSERT INTO `good` VALUES (2, '卫龙', 5, '2019-09-20 17:39:06');
```

- `pom.xml`

> `Redisson` 已经有 `redisson-spring-boot-starter`。

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!--mysql-->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <!-- mybatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>1.3.2</version>
    </dependency>
    <!-- lombok-->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!-- redisson -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.12.5</version>
    </dependency>
    <!--Swagger-ui配置-->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>2.9.2</version>
    </dependency>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger-ui</artifactId>
        <version>2.9.2</version>
    </dependency>
</dependencies>
```

- `application.yml`

```xml
# 数据库配置
server:
  port: 8081
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://47.98.178.84:3306/van_distributed
    username: van_distributed
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: 47.98.178.84
    port: 6379
    password: password
    timeout: 2000
# mybatis
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: cn.van.redisson.lock.entity
```

### 3.2 `Redisson` 配置

> 我这里配置的是单机，更多配置详见[Redisson 配置](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95)

```java
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    /**
     * RedissonClient,单机模式
     * @return
     */
    @Bean
    public RedissonClient redissonSentinel() {
        //支持单机，主从，哨兵，集群等模式,此为单机模式
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        return Redisson.create(config);
    }
}
```

### 3.3 业务代码

> 此处省略了`mapper` 映射和 `xml` 相关代码，只给出核心流程，详见源码。

- 不加锁时的商品被抢购。

```java
public HttpResult saleGoods(Long goodId) {
    GoodDO goodDO = goodMapper.selectByPrimaryKey(goodId);
    int goodStock = goodDO.getGoodCounts();
    if (goodStock >= 1) {
        // 如果库存大于一，再卖一件
        goodMapper.saleOneGood(goodId);
    }
    return HttpResult.success();
}
```

- 加锁时的商品被抢购。


```java
public HttpResult saleGoodsLock(Long goodId) {
    GoodDO goodDO = goodMapper.selectByPrimaryKey(goodId);
    int goodStock = goodDO.getGoodCounts();

    String key = goodDO.getGoodName();
    log.info("{}剩余总库存,{}件", key,goodStock);
    // 将商品的实时库存放在redis 中，便于读取
    stringRedisTemplate.opsForValue().set(key, Integer.toString(goodStock));
    // redisson 锁 的key
    String lockKey = goodDO.getId() + "_" + key;
    RLock lock = null;
    try {
        //获取Lock锁，设置锁的名称
        lock = redissonClient.getLock(lockKey);
        // 此步开始，串行执行
        // 设置60秒自动释放锁  （默认是30秒自动过期）
//            lock.lock(60, TimeUnit.SECONDS);
        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get(key));
        if (stock >= 1) {
            if (res) {
                log.info( "======获得锁后进行售卖一件======");
                goodMapper.saleOneGood(goodId);
                // 减库存成功，将缓存同步
                stringRedisTemplate.opsForValue().set(key,Integer.toString((stock-1)));
            } else {
                log.info( "======锁被占用======");
            }
            log.info("{}当前库存:{}件", key,stock);
        }
    }catch (Exception e) {
        e.printStackTrace();
    } finally {
        //释放锁
        if (lock != null) {
            log.info("释放锁，lockKey:{}", lockKey);
            lock.unlock();
        }
    }
    return HttpResult.success();
}
```

### 3.4 接口测试

> 为了便于测试，使用`Swagger`， 接口中使用多线程，模拟十个用户抢购商品。

Swagger 地址：[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
#### 3.4.1 先创建一个线程池

```java
private static int corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 创建线程池  调整队列数 拒绝服务
     */
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize + 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000));
```

#### 3.4.2 不加锁抢购

```java
@ApiOperation(value = "售卖商品（不加锁）")
@PostMapping("/saleGoods")
public HttpResult saleGoods() {
    // 销售十次，不加锁会导致库存减少到负数
    for (int i = 1; i <= 10; i++) {
        Runnable task = () -> {
            redissonLockService.saleGoods(1L);
        };
        executor.execute(task);
    }
    return HttpResult.success();
}
```


- 结果：

商品 `id = 1` 的**哇哈哈**库存减少到 `-3` 件（你也可能减少到 `-1`）,说明卖多了。

#### 3.4.3 加锁抢购


```java
@ApiOperation(value = "售卖商品（加锁）")
@PostMapping("/saleGoodsLock")
public HttpResult saleGoodsLock() {
    for (int i = 1; i <= 10; i++) {
        Runnable task = () -> {
            redissonLockService.saleGoodsLock(2L);
        };
        executor.execute(task);
    }
    return HttpResult.success();
}
```

- 结果：

```xml
... 21:07:21.712  INFO 4822 --- [pool-1-thread-6] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-1] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-5] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-7] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-3] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-8] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.712  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,5件
... 21:07:21.746  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : ======获得锁后进行售卖一件======
... 21:07:21.778  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙当前库存:5件
... 21:07:21.778  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:21.802  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,4件
... 21:07:21.805  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : ======获得锁后进行售卖一件======
... 21:07:21.831  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙当前库存:4件
... 21:07:21.832  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:21.855  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙剩余总库存,3件
... 21:07:21.861  INFO 4822 --- [pool-1-thread-8] c.v.r.l.s.impl.RedissonLockServiceImpl   : ======获得锁后进行售卖一件======
... 21:07:21.904  INFO 4822 --- [pool-1-thread-8] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙当前库存:3件
... 21:07:21.904  INFO 4822 --- [pool-1-thread-8] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:21.934  INFO 4822 --- [pool-1-thread-6] c.v.r.l.s.impl.RedissonLockServiceImpl   : ======获得锁后进行售卖一件======
... 21:07:21.964  INFO 4822 --- [pool-1-thread-6] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙当前库存:2件
... 21:07:21.964  INFO 4822 --- [pool-1-thread-6] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:21.989  INFO 4822 --- [pool-1-thread-3] c.v.r.l.s.impl.RedissonLockServiceImpl   : ======获得锁后进行售卖一件======
... 21:07:22.025  INFO 4822 --- [pool-1-thread-3] c.v.r.l.s.impl.RedissonLockServiceImpl   : 卫龙当前库存:1件
... 21:07:22.025  INFO 4822 --- [pool-1-thread-3] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:22.051  INFO 4822 --- [pool-1-thread-1] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:22.076  INFO 4822 --- [pool-1-thread-7] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:22.102  INFO 4822 --- [pool-1-thread-5] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:22.129  INFO 4822 --- [pool-1-thread-2] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
... 21:07:22.156  INFO 4822 --- [pool-1-thread-4] c.v.r.l.s.impl.RedissonLockServiceImpl   : 释放锁，lockKey:2_卫龙
```

商品 `id = 2` 的**卫龙**库存只减少到 `0` 件。

### 3.5 小结

通过两者结果对比很明显：前者出现了超卖情况，库存数卖到了负数，这是决不允许的；而加了锁的情况后，库存只会减少到`0`，便不再销售。

## 四、总结

基于`Redis`的分布式锁并不适合用于生产环境，`Redisson` 可用于生产环境。

> `Redisson` 实现分布式锁除了 `Redlock` 还有更多形式，详见[【高并发】你知道吗？大家都在使用Redisson实现分布式锁了！！](https://www.cnblogs.com/binghe001/p/12695168.html)

[Github 示例代码](https://github.com/vanDusty/Distributed/distributed-lock/redisson-lock)