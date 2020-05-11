# Mybatis 数据源灵活切换

在业务场景中，随着数据量迅速增长，一个库一个表已经满足不了我们的需求的时候，我们就会考虑分库分表的操作，本文主要介绍 `Mybatis` 动态数据源切换，可用于读写分离或多库存储。

## 一、开始项目

### 1.1 数据库准备

我这里操作两个数据库（`master`/`slave`），首先都在两个库中插入同一个表，`sql`如下：

```sql
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(10) NOT NULL COMMENT '用户名',
  `pass_word` varchar(10) NOT NULL COMMENT '密码',
  `user_sex` varchar(10) DEFAULT NULL COMMENT '性别',
  `nick_name` varchar(10) DEFAULT NULL COMMENT '昵称',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```

为了区分，`master` 库和 `slave` 分别插入一条不同的数据：

- `master`

```sql
INSERT INTO `user_info` VALUES (1, 'master', 'password', '男', '主库', '2019-04-10 16:24:14');
```

- `slave`

```sql
INSERT INTO `user_info` VALUES (2, 'slave', 'password', '女', '从库', '2019-04-10 16:24:14');
```

### 1.2 `pom.xml`

通过`AOP`注解切换数据源，需要`AOP`包，使用`druid`连接池，`mysql` + `mybatis`

```xml
<dependencies>
    <!-- aop 依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <!-- test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>1.3.2</version>
    </dependency>
    <!-- druid连接池-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.1.10</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>1.8.4</scope>
    </dependency>
</dependencies>
```

### 1.3 `application.yml`

配置了 `master` 和 `slave` 两个库，这里也可以使用两种不同类型的数据库，如`mysql + postgrapsql`，连接池使用 `druid` 。

```xml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 主数据源
    master:
      url: jdbc:mysql://47.98.178.84:3306/master
      username: master
      password: password
    # 其他数据源（可多个）
    slave:
      url: jdbc:mysql://47.98.178.84:3306/slave
      username: slave
      password: password
# mybatis sql 日志
logging:
  level:
    cn:
      van:
        annotation:
          multipleDataSource:
            mapper: debug
```

## 二、多数据源配置

### 2.1 数据源枚举

我这里有两个数据源，我们用枚举类来代替，方便我们后续扩展和管理。

```java
public enum DynamicDSEnum {
    MASTER(1,"master"),
    SLAVE(2,"slave"),
    
    ;

    DynamicDSEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String desc;
    
}
```

### 2.2 数据源配置

这里配置了两个数据源，主库：`masterDS ` 和 从库：`slaveDS `

```java
@Configuration
public class DBConfig {

    /**
     * 主库
     * @return
     */
    @ConfigurationProperties(prefix = "spring.datasource.master")
    @Bean("masterDS")
    public DataSource masterDS() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 从库
     * @return
     */
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    @Bean("slaveDS")
    @Primary
    public DataSource slaveDS() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 主从动态配置
     */
    @Bean
    public DynamicDataSource dynamicDS(@Qualifier("masterDS") DataSource masterDataSource,
                                       @Autowired(required = false) @Qualifier("slaveDS") DataSource slaveDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
        targetDataSources.put(DynamicDSEnum.MASTER.getDesc(), masterDataSource);
        if (slaveDataSource != null) {
            targetDataSources.put(DynamicDSEnum.SLAVE.getDesc(), slaveDataSource);
        }
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        return dynamicDataSource;
    }

    @Bean
    public SqlSessionFactory sessionFactory(@Qualifier("dynamicDS") DataSource dynamicDataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*Mapper.xml"));
        bean.setDataSource(dynamicDataSource);
        return bean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlTemplate(@Qualifier("sessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "dataSourceTx")
    public DataSourceTransactionManager dataSourceTx(@Qualifier("dynamicDS") DataSource dynamicDataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dynamicDataSource);
        return dataSourceTransactionManager;
    }
}
```

### 2.3 设置路由

用于记录当前线程使用的数据源的`key`是什么，以及记录所有注册成功的数据源的key的集合。用 `ThreadLocal` 保存数据源的信息到每个线程中，方便我们需要时获取。

```java
public class DataSourceContextHolder {
    private static final ThreadLocal<String> DYNAMIC_DATASOURCE_CONTEXT = new ThreadLocal();

    public static void set(String datasourceType) {
        DYNAMIC_DATASOURCE_CONTEXT.set(datasourceType);
    }

    public static String get() {
        return DYNAMIC_DATASOURCE_CONTEXT.get();
    }

    public static void clear() {
        DYNAMIC_DATASOURCE_CONTEXT.remove();
    }
}
```

### 2.4 获取路由

`Spring`提供一个接口，名为`AbstractRoutingDataSource`的抽象类，我们只需要重写`determineCurrentLookupKey`方法就可以通知`Spring`用`key`获取当前的数据源。

```java
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.get();
    }
}
```

## 三、通过`AOP`注解实现数据源动态切换

### 3.1 切换数据源的注解

为了可以方便切换数据源，我们可以写一个注解，注解中包含数据源对应的枚举值，默认是主库，

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DS {
    /**
     * 如果没设置数据源，默认 用 master 库
     * @return
     */
    DynamicDSEnum value() default DynamicDSEnum.MASTER;

    boolean clear() default true;
}
```

### 3.2 切换数据源切面

对有注解的方法做切换数据源的操作。

```
@Aspect
@Component
@Slf4j
public class DataSourceAspect {

    @Pointcut("@annotation(cn.van.multipledata.demo.annotation.DS)")
    public void dataSourceAspect() {
    }

    @Around("dataSourceAspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        boolean clear = true;
        try {
            Method method = this.getMethod(pjp);
            DS DS = method.getAnnotation(DS.class);
            clear = DS.clear();
            DataSourceContextHolder.set(DS.value().getDesc());
            log.info("========数据源切换至：{}", DS.value().getDesc());
            return pjp.proceed();
        } finally {
            if (clear) {
                DataSourceContextHolder.clear();
            }

        }

    }

    private Method getMethod(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod();
    }
}
```

## 四、测试多数据源配置

`UserService.java`两个方法`selectMaster()`、`selectSlave()`及其实现；分别查询 `master` 和 `slave` 库的数据。

```java
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    @Override
    public List<UserInfo> selectMaster() {
        return userMapper.selectAll();
    }

    @DS(value = DynamicDSEnum.SLAVE)
    @Override
    public List<UserInfo> selectSlave() {
        return userMapper.selectAll();
    }
}
```

### 4.1 查询 `master` 库

```java
@Test
public void selectMaster() {
    List<UserInfo> users = userService.selectMaster();
    log.info("master-users:{}", users);
}
```

- 测试结果：

```xml
....: master-users:[User{id=1, userName='master', passWord='password', userSex='男', nickName='主库', gmtCreate=2019-04-11T05:24:14}]
```

### 4.2 查询 `slave` 库

```java
@Test
public void selectSlave() {
    List<UserInfo> users = userService.selectSlave();
    log.info("slave-users:{}", users);
}
```

- 测试结果：

```xml
...: slave-users:[User{id=2, userName='slave', passWord='password', userSex='女', nickName='从库', gmtCreate=2019-04-11T05:24:14}]
```


## 五、总结

相较于另一篇文章，该方式虽然配置麻烦，但是如果新增数据源，配置更简单。文中代码不全，文章代码，详见

[Github 示例代码](https://github.com/vanDusty/Mybatis-Home/tree/master/mybatis-case/multipledata-demo)

