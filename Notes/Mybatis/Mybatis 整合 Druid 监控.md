# Mybatis 整合 Druid 监控

> `Druid`是什么?
> 
>1. `Druid`是一个`JDBC`组件库，包括数据库连接池、`SQL Parser`等组件;
>1. `DruidDataSource`是最好的数据库连接池;
>1. `Druid`能够提供强大的监控(可视化)和扩展功能。


## 一、初始化项目

### 1.1 `sql`

```sql
DROP TABLE IF EXISTS `user_info_druid`;
CREATE TABLE `user_info_druid` (
                           `id` bigint(20) AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                           `user_name` varchar(50) NOT NULL COMMENT '用户名',
                           `user_age` int(3) DEFAULT 0 COMMENT '用户年龄'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'mybatis-druid';

-- 插入测试数据

INSERT INTO `user_info_druid` VALUES (1, '张三', 27);
INSERT INTO `user_info_druid` VALUES (2, '李四', 30);
INSERT INTO `user_info_druid` VALUES (3, '王五', 20);
```

### 1.2 `pom.xml`

> 引入`Swagger`相关包是为了方便测试。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!--mysql-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
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
<!-- lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>1.8.4</scope>
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
```


### 1.3 `application.yml`

> 这里需要注意的是最下面是我们`druid`管理账户及密码。

```yml
#端口
server:
  port: 8082

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://47.98.178.84:3306/van_mybatis
    username: username
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

    # 使用druid数据源
    type: com.alibaba.druid.pool.DruidDataSource
    # 配置获取连接等待超时的时间
    # 下面为连接池的补充设置，应用到上面所有数据源中
    # 初始化大小，最小，最大
    initialSize: 1
    minIdle: 3
    maxActive: 20
    # 配置获取连接等待超时的时间
    maxWait: 60000
    # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    timeBetweenEvictionRunsMillis: 60000
    # 配置一个连接在池中最小生存的时间，单位是毫秒
    minEvictableIdleTimeMillis: 30000
    validationQuery: select 'x'
    testWhileIdle: true
    testOnBorrow: false
    testOnReturn: false
    # 打开PSCache，并且指定每个连接上PSCache的大小
    poolPreparedStatements: true
    maxPoolPreparedStatementPerConnectionSize: 20
    # 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
    filters: stat,wall,slf4j
    # 通过connectProperties属性来打开mergeSql功能；慢SQL记录
    connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
    # 合并多个DruidDataSource的监控数据
    useGlobalDataSourceStat: true

# mybatis
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: cn.van.mybatis.druid.entity
# mybatis log
logging:
  level:
    cn:
      van:
        mybatis:
          druid:
            mapper: debug

# druid 相关配置放这里
druid: 
  # 管理用户账号
  login: 
    username: admin
    password: 123456
```

## 二、`Druid` 配置

> `UserInfoDruidDO.java`、`UserInfoDruidMapper.java`、`UserInfoDruidMapper.xml`省略了，详见[mybatis-druid](https://github.com/vanDusty/mybatis-Home/tree/master/mybatis-case/mybatis-druid)

### 2.1 `DruidConfig.java`

```java
@Configuration
@Slf4j
public class DruidConfig {

    @Value("${druid.login.username}")
    private String username;

    @Value("${druid.login.password}")
    private String password;

    private static final String DB_PREFIX = "spring.datasource";
    /**
     *  主要实现WEB监控的配置处理
     */
    @Bean
    public ServletRegistrationBean druidServlet() {
        log.info("enter DruidConfig ............");
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        servletRegistrationBean.addUrlMappings("/druid/*");
        // IP白名单，多个用逗号分割， 如果allow没有配置或者为空，则允许所有访问
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        // IP黑名单(共同存在时，deny优先于allow)
        servletRegistrationBean.addInitParameter("deny", "192.168.1.100");
        //控制台管理用户
        servletRegistrationBean.addInitParameter("loginUsername", username);
        servletRegistrationBean.addInitParameter("loginPassword", password);
        //是否能够重置数据 禁用HTML页面上的“Reset All”功能
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        //所有请求进行监控处理
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

    /**
     * 注入yml中配置的属性
     */
    @Data
    @ConfigurationProperties(prefix = DB_PREFIX)
    private class IDataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int initialSize;
        private int minIdle;
        private int maxActive;
        private int maxWait;
        private int timeBetweenEvictionRunsMillis;
        private int minEvictableIdleTimeMillis;
        private String validationQuery;
        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private boolean testOnReturn;
        private boolean poolPreparedStatements;
        private int maxPoolPreparedStatementPerConnectionSize;
        private String filters;
        private String connectionProperties;

        @Bean     //声明其为Bean实例
        @Primary  //在同样的DataSource中，首先使用被标注的DataSource
        public DataSource dataSource() {
            DruidDataSource datasource = new DruidDataSource();
            datasource.setUrl(url);
            datasource.setUsername(username);
            datasource.setPassword(password);
            datasource.setDriverClassName(driverClassName);

            //configuration
            datasource.setInitialSize(initialSize);
            datasource.setMinIdle(minIdle);
            datasource.setMaxActive(maxActive);
            datasource.setMaxWait(maxWait);
            datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            datasource.setValidationQuery(validationQuery);
            datasource.setTestWhileIdle(testWhileIdle);
            datasource.setTestOnBorrow(testOnBorrow);
            datasource.setTestOnReturn(testOnReturn);
            datasource.setPoolPreparedStatements(poolPreparedStatements);
            datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
            try {
                datasource.setFilters(filters);
            } catch (SQLException e) {
                System.err.println("druid configuration initialization filter: " + e);
            }
            datasource.setConnectionProperties(connectionProperties);
            return datasource;
        }
    }

}
```

**这里需要注意的**

1. `username`、`password` 放在配置文件中，用于`Druid`控制台访问的登陆；
1. 内部类`IDataSourceProperties.java` 读取的是我们配置文件中的`druid`配置，注意字段的一一对应。	


### 2.2 控制层接口

> 为了便于测试，我引用了`Swagger`方便测试

```java
@RestController
@RequestMapping("/mybatisDruid")
@Api(tags = {"Druid 测试"})
public class DruidController {

    @Resource
    UserInfoDruidMapper userInfoDruidMapper;

    @GetMapping("/selectOne")
    public UserInfoDruidDO selectOne(Long id) {
        return userInfoDruidMapper.selectByPrimaryKey(id);
    }

    @GetMapping("/selectAll")
    public List<UserInfoDruidDO> selectAll() {
        return userInfoDruidMapper.selectAll();
    }
}
```
## 三、使用

### 3.1 请求接口

[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

请求两个接口，如果配置没错，这里应该可以请求到数据。

### 3.2 查看 `Druid` 监控

访问[http://localhost:8082/druid/login.html](http://localhost:8082/druid/login.html)

输入设置的用户名和密码即可访问`Druid`面板，里面有啥功能，自己去看吧。

## 四、总结

[Github 完整示例](https://github.com/vanDusty/Frame-Home/tree/master/mybatis-case/mybatis-druid)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。

参考文章：[Druid 问题文档](https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)
