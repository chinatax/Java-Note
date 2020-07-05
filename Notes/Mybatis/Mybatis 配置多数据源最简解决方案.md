# Mybatis 配置多数据源最简解决方案

> 现在主流的多数据源切换方式有：`AOP` 动态切换来做多数据源或者升级成`Mybatis-Plus`可以简单的配置多数据源，偶然见到大佬整理的很简单的多数据源的配置，改进一下，分享出来。

## 一、项目准备

### 1.1 `pom.xml`

> 没有特殊的依赖，`mysql `  + `mybatis` + `druid`

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
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
    <!-- druid连接池-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.1.10</version>
    </dependency>
</dependencies>
```

### 1.2 `application.yml`

我这里配置两个数据源：`master` 库和一个 `slave` 库，其中 `master`是默认库。可以有多个从库，自己去扩展吧，按照`slave` 库配置，再新增一个配置类即可。

```
spring:
  datasource:
    druid:
      master: # 主数据源
        username: master
        password: password
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://47.98.178.84:3306/master
        initialSize: 5
        minIdle: 5
        maxActive: 20
      slave: # 第二个数据源
        username: slave
        password: password
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://47.98.178.84:3306/slave
        initialSize: 5
        minIdle: 5
        maxActive: 20
```

### 1.3 初始化表

> 因为我的测试是在两个库分别插入数据，所以需要分别在两个库新建结构相同的表，`sql`如下：

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

## 二、数据源配置

### 2.1 主数据源配置

```java
@Configuration
@MapperScan(basePackages = "cn.van.mybatis.multipledata.simple.mapper.master", sqlSessionTemplateRef  = "masterSqlSessionTemplate")
// 指定主库扫描的 dao包，并给 dao层注入指定的 SqlSessionTemplate
public class DataSource1Config {
//    首先创建 DataSource
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    @Primary // 指定是主库
    public DataSource testDataSource() {
        return DataSourceBuilder.create().build();
    }
//  然后创建 SqlSessionFactory
    @Bean(name = "masterSqlSessionFactory")
    @Primary
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }
//  再创建事务
    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager testTransactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
//  最后包装到 SqlSessionTemplate 中
    @Bean(name = "masterSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("masterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

1. 首先创建 `DataSource`，然后创建 `SqlSessionFactory`，再创建事务，最后包装到 `SqlSessionTemplate` 中；
1. `@MapperScan(basePackages = "***", sqlSessionTemplateRef  = "***")`:指定主库扫描的 `mapper` 文件，并注入指定的 `SqlSessionTemplate`,同时`SqlSessionFactory`的`bean`中指定`xml`文件位置;
1. `@Primary` 说明指定了主库。

### 2.2 从库数据源配置

```java
@Configuration
@MapperScan(basePackages = "cn.van.mybatis.multipledata.simple.mapper.slave", sqlSessionTemplateRef  = "db2SqlSessionTemplate")
// 指定分库扫描的 dao包，并给 dao层注入指定的 SqlSessionTemplate
public class DataSource2Config {
    //    首先创建 DataSource
    @Bean(name = "db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
    public DataSource testDataSource() {
        return DataSourceBuilder.create().build();
    }
    //  然后创建 SqlSessionFactory
    @Bean(name = "db2SqlSessionFactory")
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("db2DataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }
    //  再创建事务
    @Bean(name = "db2TransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier("db2DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    //  最后包装到 SqlSessionTemplate 中
    @Bean(name = "db2SqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("db2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

相对于主库的配置，不同点有：

1. 方法上没有`@Primary`的注解，说明是从库，(如果需要扩展，按照次配置新增即可)；
1. `@MapperScan(basePackages = "***", sqlSessionTemplateRef  = "***")`:指定主库扫描的 `mapper`包 不同；
1. `@ConfigurationProperties(prefix = "spring.datasource.druid.**")`：指定的数据源不同。


## 三、简单测试

### 3.1 业务层接口

> 这里的接口方法比较简单，主要是两个：将数据插入主库和从库，完整代码见文末。

- `TestService.java`

```java
public interface TestService {
    // 插入主库
    void insertMater(UserInfo userInfo);
    // 插入从库
    void insertSlave(UserInfo userInfo);
}
```

- `TestServiceImpl.java`

```java
@Service
public class TestServiceImpl implements TestService {
    @Resource
    User1Mapper user1Mapper;
    @Resource
    User2Mapper user2Mapper;

    public void insertMater(UserInfo userInfo) {
        user1Mapper.insert(userInfo);
    }

    public void insertSlave(UserInfo userInfo) {
        user2Mapper.insert(userInfo);
    }
}
```

> `mapper` 接口和映射文件`xml`很简单，这里就不放出来了。


### 3.2 单元测试

- 往主库（`master`） 新增数据

```java
@Test
public void insertMater() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserName("master");
    userInfo.setPassWord("password");
    userInfo.setUserSex("男");
    testService.insertMater(userInfo);
}
```

- 往从库（`slave`） 新增数据

```java
@Test
public void insertSlave() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserName("slave");
    userInfo.setPassWord("password");
    userInfo.setUserSex("女");
    testService.insertSlave(userInfo);
}
```

> 经过测试，分别在在 `master` 库和 `slave` 库新增了不同的数据，证明我们的多数据源生效。

## 四、总结

如果你觉得：我只是想找一个简单的多数据支持而已，使用`AOP`配置多数据源有点小复杂，欢迎尝试该方式。

[Github 示例源码](https://github.com/vanDusty/Frame-Home/tree/master/mybatis-case/multipledata-simple)

参考文章：[Spring Boot(七)：Mybatis 多数据源最简解决方案](http://www.ityouknow.com/springboot/2016/11/25/spring-boot-multi-mybatis.html)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
