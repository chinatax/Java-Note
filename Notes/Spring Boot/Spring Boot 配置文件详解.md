# Spring Boot 配置文件详解

> `Spring Boot`是为了简化`Spring`应用的创建、运行、调试、部署等一系列问题而诞生的产物，自动装配的特性让我们可以更好的关注业务本身而不是外部的`XML`配置，我们只需遵循规范，引入相关的依赖就可以轻易的搭建出一个 `WEB` 工程。

## 一、准备

### 1.1 背景
熟悉 `Spring Boot` 的小伙伴都知道，`Spring Boot` 中的配置文件有两种格式：`properties/yaml`，一般情况下，两者可以随意使用本文就来和大伙重点介绍下 `yaml` 配置。

### 1.2 建议使用的包

为了让`Spring Boot`更好的生成数据，我们需要添加如下依赖（该依赖可以不添加，但是在 `IDEA` 不会有属性提示），该依赖只会在编译时调用，所以不用担心会对生产造成影响…

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

## 二、自定义属性配置

### 2.1 `application.yml` 自定义属性

```yml
server:
  port: 8081

original:
  config:
    title: OriginalConfig
    description: 主配置文件中属性
```
### 2.2 新建`OriginalConfig`配置类

> 该配置类用来映射我们在`application.yml`中的内容，这样一来我们就可以通过操作对象的方式来获得配置文件的内容了。

```java
@Data
@Component
public class OriginalConfig {
    /**
     * 注入 application.yml 配置
     *
     */
    @Value("${original.config.title}")
    private String title;

    @Value("${original.config.description}")
    private String description;
    
}
```

> 这里，可以采用`@ConfigurationProperties`简化`@Value`注入值的写法,详见文末Github源码写法。

### 2.3 `ConfigTest.java`测试

#### 2.3.1 测试方法`testOriginalConfig`

```java
	@Autowired
    OriginalConfig originalConfig;

    /**
     * 读取自定义属性
     */
    @Test
    public void testOriginalConfig() {
        System.out.println("开始读取 application.yml 的自定义属性：");
        System.out.println("读取配置信息：");
        System.out.println("title: " + originalConfig.getTitle());
        System.out.println("desc: " + originalConfig.getDescription());
        System.out.println("application.yml 文件的属性读取完毕！");
    }
```

#### 2.3.2 控制台打印如下：

```
开始读取 application.yml 的自定义属性：
读取配置信息：
title: OriginalConfig
desc: 主配置文件中属性
application.yml 文件的属性读取完毕！
```

## 三、自定义文件配置 

### 3.1 定义一个名为`myConfig.properties`的自定义文件

> 自定义配置文件的命名不强制application开头

```
customize.config.title=CustomizeConfig
customize.config.description=自定义配置文件中属性
```
   
### 3.2 新建`CustomizeConfig`配置类

> 该配置类用来映射我们在`myConfig.properties`中的内容，这样一来我们就可以通过操作对象的方式来获得配置文件的内容了。

```java
@Data
@Component
@PropertySource(value = "classpath:myConfig.properties", encoding = "utf-8")
public class CustomizeConfig {
    /**
     * 注入 myConfig.properties 配置
     *
     */
    @Value("${customize.config.title}")
    private String title;

    @Value("${customize.config.description}")
    private String description;
}
```

- `@PropertySource` :配置文件路径和编码格式

### 2.3 单元测试

```java
	@Autowired
    CustomizeConfig customizeConfig;

    /**
     * 从 myConfig.properties 获取配置
     */
    @Test
    public void testCustomizeConfig() {
        System.out.println("开始读取 myConfig.properties 文件的属性：");
        System.out.println("title: " + customizeConfig.getTitle());
        System.out.println("desc: " + customizeConfig.getDescription());
        System.out.println("myConfig.properties 文件的属性读取完毕！");
    }
```

- 控制台打印如下：

```
开始读取 myConfig.properties 文件的属性：
title: CustomizeConfig
desc: 自定义配置文件中属性
myConfig.properties 文件的属性读取完毕！
```



## 三、多环境化配置

> 真实的应用中，常常会有多个环境（如：本地，开发，测试，正式），不同的环境数据库连接都不一样，这个时候就需要用到`spring.profile.active`，它的格式为`application-{profile}.yml`，这里的`application`为前缀不能改，`{profile}`是我们自己定义的。

### 3.1 指定不同环境的路径

> 通过`server.servlet.context-path`将不同环境指定不同的路径。


* `application-local.yml`

```
title: local配置文件
server:
  servlet:
    context-path: /local
```

* `application-daily.yml`

```
title: daily配置文件
server:
  servlet:
    context-path: /daily
```

* `application-gray.yml`

```
title: gray配置文件
server:
  servlet:
    context-path: /gray
```


* `application-production.yml`

```
title: production配置文件
server:
  servlet:
    context-path: /production
```

### 3.2 新增配置类`EnvConfig.java`

```java
@Data
@Configuration
public class EnvConfig {

    @Value("${title}")
    private String title;
}
```

### 3.3 测试方法

```java
	@Autowired
    EnvConfig envConfig;

    /**
     * 从 不同环境配置读取
     */
    @Test
    public void testEnvConfig() {
        System.out.println("开始读取 不同环境 文件的属性：");
        System.out.println("title: " + envConfig.getTitle());
        System.out.println("myConfig.properties 文件的属性读取完毕！");
    }
```

### 3.4 激活某个环境配置

在`application.yml`配置文件中指定环境，例如指定`gray`环境：

```
spring:
  profiles:
    active: daily
```

- 控制台打印如下：

```
开始读取 不同环境 文件的属性：
title: gray配置文件
myConfig.properties 文件的属性读取完毕！
```

> 这里指定不同的环境配置文件，即可访问到不同环境的配置。

## 四、源码地址

[Github 示例源码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-list/springboot-demo-config)