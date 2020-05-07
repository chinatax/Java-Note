# Spring Boot 整合 Apache Dubbo


`Apache Dubbo`是一款高性能、轻量级的开源 `Java` `RPC` 框架，它提供了三大核心能力：面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。

> 注意，是 `Apache Dubbo`，不再是 `Alibaba Dubbo`。简单来说就是 `Alibaba` 将 `Dubbo` 移交给 `Apache` 开源社区进行维护。参见 [dubbo-spring-boot-project](https://github.com/apache/dubbo-spring-boot-project/blob/master/README_CN.md)

## 一、本文示例说明

### 1.1 框架版本

- `Dubbo` 版本

```xml
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>2.7.5</version>
</dependency>
```

- `Spring Boot` 版本

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.1.RELEASE</version>
    <relativePath/>
</parent>
```

### 1.2 模块关系

- 根工程 `order`：管理工程信息；
- 子工程 `order-api`：定义`RPC`服务的接口、参数以及响应结果的结果集；
- 子工程 `order-provider`：`RPC`服务的提供端；
- 子工程 `order-consumer`：`RPC`服务的消费端，实际开发过程中实际情况是其它服务的调用该订单`RPC`服务

## 二、根工程

### 2.1 创建项目 `order`

> 我这里为了和之前老版本的`alibaba`的`dubbo`项目区分，文件名取为`apache-dubbo-demo`，`maven` 项目名称为`order`。

该项目主要作用是定义工程信息、管理整个项目依赖版本等等，所以`src`目录不需要。

### 2.2 `pom.xml`

根工程中使用了`<dependencyManagement>`和`<dependencies>`进行依赖管理。

1. `<dependencyManagement>`：声明全局依赖，当子项目指定引用才会继承依赖；
2. `<dependencies>`：声明全局依赖，子项目直接自动继承依赖。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父级引用 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
        <relativePath/>
    </parent>

    <!-- 基本信息 -->
    <groupId>cn.van.order</groupId>
    <artifactId>order</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>${project.artifactId}</name>
    <description>Apache Dubbo 根项目</description>

    <!--配置-->
    <properties>
        <java.version>1.8</java.version>
        <dubbo.version>2.7.5</dubbo.version>
        <zookeeper.version>3.4.14</zookeeper.version>
    </properties>

    <!-- 子项目 -->
    <modules>
        <module>order-api</module>
        <module>order-provider</module>
        <module>order-consumer</module>
    </modules>

    <!--声明全局依赖（子项目需要显示的引用才会继承依赖）-->
    <dependencyManagement>
        <dependencies>
            <!-- dubbo-start依赖 -->
            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-spring-boot-starter</artifactId>
                <version>${dubbo.version}</version>
            </dependency>
            <!--zookeeper 注册中心客户端引入 使用的是curator客户端 -->
            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-dependencies-zookeeper</artifactId>
                <version>${dubbo.version}</version>
                <type>pom</type>
                <exclusions>
                    <exclusion>
                        <artifactId>slf4j-log4j12</artifactId>
                        <groupId>org.slf4j</groupId>
                    </exclusion>
                </exclusions>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!--声明全局依赖（子项目不需要显示的引用，自动继承依赖）-->
    <dependencies>
        <!-- spring boot 依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <!-- 打包插件 -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 三、`order-api`

### 3.1 项目依赖

> 无需更多依赖，所以很简单。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>cn.van.order</groupId>
        <artifactId>order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>cn.van.order</groupId>
    <artifactId>order-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>${project.artifactId}</name>
    <description>dubbo公共项目</description>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### 3.2 封装 `RPC` 结果集

- 先封装一个返回码枚举类`ResultCodeEnum.java`

```java
public enum ResultCodeEnum {
    /*** 通用部分 100 - 599***/
    // 成功请求
    SUCCESS(200, "successful"),
    
    /*** 这里可以根据不同模块用不同的区级分开错误码，例如:  ***/

    // 1000～1999 区间表示用户模块错误
    // 2000～2999 区间表示订单模块错误
    // 3000～3999 区间表示商品模块错误
    // 。。。

    ORDER_NOT_FOUND(2000, "order not found"),
    ;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
```

- 先封装一个RPC 响应结果集`RpcResult.java`

```java
public class RpcResult <T> implements Serializable {

    /**
     * 是否响应成功
     */
    private Boolean success;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应数据
     */
    private T data;
    /**
     * 错误信息
     */
    private String message;

    // 构造器开始
    /**
     * 无参构造器(构造器私有，外部不可以直接创建)
     */
    private RpcResult() {
        this.code = 200;
        this.success = true;
    }
    /**
     * 有参构造器
     * @param obj
     */
    private RpcResult(T obj) {
        this.code = 200;
        this.data = obj;
        this.success = true;
    }

    /**
     * 有参构造器
     * @param resultCode
     */
    private RpcResult(ResultCodeEnum resultCode) {
        this.success = false;
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    // 构造器结束

    /**
     * 通用返回成功（没有返回结果）
     * @param <T>
     * @return
     */
    public static<T> RpcResult<T> success(){
        return new RpcResult();
    }

    /**
     * 返回成功（有返回结果）
     * @param data
     * @param <T>
     * @return
     */
    public static<T> RpcResult<T> success(T data){
        return new RpcResult<T>(data);
    }

    /**
     * 通用返回失败
     * @param resultCode
     * @param <T>
     * @return
     */
    public static<T> RpcResult<T> failure(ResultCodeEnum resultCode){
        return  new RpcResult<T>(resultCode);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RpcResult{" +
                "success=" + success +
                ", code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
```

### 3.3 编写一个 `RPC` 接口

```java
public interface OrderDubboService {
    RpcResult<OrderDomain> getOrder();
}
```

> 实体`OrderDomain.java`挺简单的，详见 `Github` 仓库。

## 四、`order-provider`

此子项目是一个服务类项目，也就是将接口服务注册到`zookeeper`注册中心供消费端调取使用。

### 4.1 项目依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>cn.van.order</groupId>
        <artifactId>order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>cn.van.order</groupId>
    <artifactId>order-provider</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>${project.artifactId}</name>
    <description>Dubbo 服务提供者</description>

    <dependencies>
        <dependency>
            <groupId>cn.van.order</groupId>
            <artifactId>order-api</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>
        <!-- zookeeper依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-dependencies-zookeeper</artifactId>
            <version>${dubbo.version}</version>
            <type>pom</type>
            <exclusions>
                <exclusion>
                    <artifactId>slf4j-log4j12</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 4.2 服务实现接口

```java
@Service
public class OrderDubboServiceImpl implements OrderDubboService {
    
    @Override
    public RpcResult<OrderDomain> getOrder() {
        return RpcResult.success(new OrderDomain(1, 10086, LocalDateTime.now()));
    }
}
```

**注意：**
`@Service` 是 `dubbo` 包下面的注解不是 `Spring` 里面的注解。

### 4.3 项目配置

1. `dubbo`  的配置直接用 `dubbo`，不再以 `Spring` 开头;
1. `base-packages`：指定接口实现所在路径。

```xml
server:
  # 服务端口
  port: 7777
spring:
  application:
    name: order-provider
# dubbo 相关配置(dubbo 的配置不再以 Spring 开头)
dubbo:
  application:
    # 应用名称
    name: order-provider
  scan:
    # 接口实现者（服务实现）包
    base-packages: cn.van.order.service.impl
  # 注册中心信息
  registry:
    address: zookeeper://127.0.0.1:2181
  protocol:
    # 协议名称
    name: dubbo
    # 协议端口
    port: 20880
```

## 五、`order-consumer`

此子项目就是一个消费项目，比如商品模块、财务模块等等。

### 5.1 项目依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>cn.van.order</groupId>
        <artifactId>order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>cn.van.order</groupId>
    <artifactId>order-consumer</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>${project.artifactId}</name>
    <description>Dubbo 消费者</description>


    <dependencies>
        <dependency>
            <groupId>cn.van.order</groupId>
            <artifactId>order-api</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- web项目依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- dubbo依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>

        <!-- dubbo的zookeeper依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-dependencies-zookeeper</artifactId>
            <version>${dubbo.version}</version>
            <type>pom</type>
            <exclusions>
                <exclusion>
                    <artifactId>slf4j-log4j12</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 5.2 测试接口

模拟一个接口获取订单详情。

```java
@RestController
@RequestMapping("/order")
public class OrderConsumerController {
    @Reference
    OrderDubboService orderDubboService;

    @GetMapping("getOrder")
    public RpcResult getOrder() {
        return orderDubboService.getOrder();
    }
}
```

**注意：**`@Reference`引入的是 `Dubbo` 接口，所以是 `Dubbo` 的注解。

### 5.3 配置文件

```xml
server:
  port: 7000
spring:
  application:
    name: order-consumer
# dubbo 相关配置
dubbo:
  application:
    name: order-consumer
  registry:
    address: zookeeper://127.0.0.1:2181
```

## 六、测试

一切就绪，如果在`order-consumer` 的测试接口能成功请求到数据，则证明 `Dubbo` 服务搭建成功。

### 6.1 启动 `zookeeper`

我们选用`zookeeper`作为注册中心，因此启动项目之前需要先启动它。

### 6.2 `dubbo-admin`

`dubbo-admin` 便于观察 `order-provider` 是否成功将接口注册，具体安装步骤详见[apache/dubbo-admin](https://github.com/apache/dubbo-admin/blob/develop/README_ZH.md)

> 默认端口：`8080`。

### 6.3 启动 `dubbo-provider`

成功启动后可以在`dubbo-admin`：已经成功将接口 `OrderService` 注册到 `zookeeper` 上如下：

![图一](/File/Imgs/article/Apache%20Dubbo.png)

成功将借口注册到注册中心，说明`dubbo-provider` 注册成功。

### 6.4 启动 `order-cosumer`

启动消费者项目，在浏览器请求消费接口：[http://localhost:7000/order/getOrder](http://localhost:7000/order/getOrder)，成功返回数据如下：

```json
{
    "success":true,
    "code":200,
    "data":{
        "id":1,
        "orderNum":10086,
        "gmtCreate":"2020-05-06T11:59:45.535"
    },
    "message":null
}
```

成功请求到 `order-provider` 提供的数据，说明 `Dubbo` 搭建成功！


## 七、总结

以上的完整代码我已上传到 [Github](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-dubbo/apache-dubbo-demo)，需要的可以自取测试，欢迎`star`！