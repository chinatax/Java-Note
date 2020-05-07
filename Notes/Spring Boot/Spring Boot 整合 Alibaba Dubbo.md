# Spring Boot 整合 Alibaba Dubbo

> 注册中心采用的是`zookeeper`，详见[Mac 安装并运行 Zookeeper](../Mac%20办公/Mac%20安装与配置%20Zookeeper.md)

## 一、项目搭建

### 1.1 新建主项目-`dubbo`

- `pom.xml` 中放入整个项目都需要的依赖。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <artifactId>dubbo</artifactId>
    <groupId>cn.van</groupId>
    <version>1.0-SNAPSHOT</version>

    <!--在这里设置打包类型为pom，作用是为了实现多模块项目-->
    <packaging>pom</packaging>
    <!-- 这里是我们子模块的设置 -->
    <modules>
        <module>dubbo-api</module>
        <module>dubbo-provider</module>
        <module>dubbo-consumer</module>
    </modules>

    <!-- 第一步：添加SpringBoot的parent -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
    </parent>

    <!-- 设置我们项目的一些版本属性 -->
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
        <dubbo.version>2.5.5</dubbo.version>
        <zkclient.version>0.10</zkclient.version>
        <lombok.version>1.18.4</lombok.version>
        <spring-boot.version>2.1.1.RELEASE</spring-boot.version>
    </properties>

    <!-- 声明一些项目依赖管理，方便我们的依赖版本管理 -->
    <dependencies>
        <!--&lt;!&ndash; Springboot依赖 &ndash;&gt;-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Springboot-web依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- 使用lombok实现JavaBean的get、set、toString、hashCode、equals等方法的自动生成  -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Dubbo依赖 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>${dubbo.version}</version>
        </dependency>
        <!-- zookeeper的客户端依赖 -->
        <dependency>
            <groupId>com.101tec</groupId>
            <artifactId>zkclient</artifactId>
            <version>${zkclient.version}</version>
        </dependency>
    </dependencies>
</project>
```

### 1.2 创建子模块-暴露服务接口项目`dubbo-api`

- `pom.xml` 暂时不包含特有依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>dubbo</artifactId>
        <groupId>cn.van</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>dubbo-api</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

</project>
```

### 1.3 创建子模块-生产者项目`dubbo-provider`

- `pom.xml` 暂时不包含特有依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>dubbo</artifactId>
        <groupId>cn.van</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>dubbo-provider</artifactId>

    <dependencies>
        <dependency>
            <groupId>cn.van</groupId>
            <artifactId>dubbo-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

</project>
```

### 1.4 创建子模块-消费者项目`dubbo-consumer`

- `pom.xml` 暂时不包含特有依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>dubbo</artifactId>
        <groupId>cn.van</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>dubbo-consumer</artifactId>

    <dependencies>
        <dependency>
            <groupId>cn.van</groupId>
            <artifactId>dubbo-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

</project>
```

## 二、子项目：`dubbo-api`

### 2.1 创建一个服务暴露实体类

```java
 */
@Data
public class UserDomain implements Serializable {
    private Integer id;
    private String username;
    private String password;
    private Integer age;
    private Integer gender;
}
```

### 2.2 创建需要`dubbo`暴露的接口

```java
public interface DubboService {
    UserDomain findUser();
}
```

## 三、子项目：`dubbo-provider`

### 3.1 首先实现我们在`dubbo-api`上定义的接口

创建一个`DubboServiceImpl`类并实现 `DubboService`

```java
@Service("dubboService")
public class DubboServiceImpl implements DubboService {

    @Override
    public UserDomain findUser() {
        /**
         * 模拟查询出一条数据
         */
        UserDomain userDomain = new UserDomain();
        userDomain.setId(1001);
        userDomain.setUsername("scott");
        userDomain.setPassword("tiger");
        userDomain.setAge(20);
        userDomain.setGender(0);
        return userDomain;
    }
}
```

### 3.2 `dubbo`提供者配置

在`resources`下创建一个`dubbo`文件夹，在`config`下创建`spring-dubbo.xml`配置文件。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://code.alibabatech.com/schema/dubbo
       http://code.alibabatech.com/schema/dubbo/dubbo.xsd">

    <dubbo:application name="${dubbo.application.name}"/>
    <!-- 注册中心的ip地址 -->
    <dubbo:registry protocol="zookeeper" address="${dubbo.registry.address}"/>
    <!-- 协议/端口-->
    <dubbo:protocol name="dubbo" port="${dubbo.protocol.port}"/>
    <!-- 暴露的接口 -->
    <dubbo:service ref="dubboService" interface="cn.van.dubbo.service.DubboService"
                   timeout="3000" />
</beans>
```

- 注意：需要在启动类上将该文件配置注入：

```java
@ImportResource({"classpath:dubbo/spring-dubbo.xml"})
```

### 3.3 `application.yml`配置

```
# dubbo 相关配置
dubbo:
  application:
    name: provider
  registry:
    address: 127.0.0.1:2181
  protocol:
    port: 20880
```

## 四、子项目：`dubbo-consumer`

### 4.1 新建一个测试接口

```java
@RestController
@RequestMapping("/consumer")
public class DubboTestController {

    @Resource
    private DubboService dubboService;

    @GetMapping("/getInfo")
    public UserDomain user() {
        return dubboService.findUser();
    }
}
```

### 4.2 `dubbo` 消费者配置

在`resources`下创建一个`dubbo`文件夹，在`config`下创建`spring-dubbo.xml`配置文件。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://code.alibabatech.com/schema/dubbo
       http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
    <!-- 应用名 -->
    <dubbo:application name="consumer"/>
    <!-- 注册中心的ip地址 -->
    <dubbo:registry protocol="zookeeper" address="${dubbo.registry.address}"/>
    <!-- 消费方用什么协议获取服务（用dubbo协议在20880端口暴露服务）-->
    <dubbo:protocol name="dubbo" port="${dubbo.protocol.port}"/>
    <!-- 消费的服务 -->
    <dubbo:reference id="dubboService" interface="cn.van.dubbo.service.DubboService" check="false" timeout="5000" lazy="true" />

</beans>
```

- 注意：需要在启动类上将该文件配置注入：

```java
@ImportResource({"classpath:dubbo/spring-dubbo.xml"})
```


### 4.3 `application.yml`配置

```
server:
  # 指定端口号
  port: 8090
  servlet:
    # 指定项目地址
    context-path: /dubbo
# dubbo 相关配置
dubbo:
  registry:
    address: 127.0.0.1:2181
  protocol:
    port: 20880
```

## 五、测试

1. 启动`zookeeper`；
2. 启动提供者项目`dubbo-provider`
3. 启动消费者项目`dubbo-consumer`
4. 测试，访问如下借口

[http://localhost:8090/dubbo/consumer/getInfo](http://localhost:8090/dubbo/consumer/getInfo)

返回数据如下：

```xml   
{
    "id":1001,
    "username":"scott",
    "password":"tiger",
    "age":20,
    "gender":0
}
```

说明消费者消费到提供者提供的服务。

## 四、总结

[Github 完整示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-dubbo/alibaba-dubbo-demo)