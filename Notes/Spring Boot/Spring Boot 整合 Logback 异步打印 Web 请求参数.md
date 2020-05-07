# Spring Boot 整合 Logback 异步打印 Web 请求参数

> 本文介绍：日志输出到文件并根据`LEVEL`级别将日志分类保存到不同文件、通过异步输出日志减少磁盘`IO`提高性能

## 一、`Logback`

### 1.1 背景

`Logback`是由`log4j`创始人设计的另一个开源日志组件，它分为下面下个模块：

1. `logback-core`：其它两个模块的基础模块
1. `logback-classic`：它是`log4j`的一个改良版本，同时它完整实现了`slf4j API`使你可以很方便地更换成其它日志系统如`log4j`或`JDK14 Logging`
1. `logback-access`：访问模块与`Servlet`容器集成提供通过`Http`来访问日志的功能

### 1.2 日志级别

> 包括：`TRACE`、`DEBUG`、`INFO`、`WARN` 和 `ERROR`。

#### 1.2.1 `TRACE`

特别详细的系统运行完成信息，业务代码中，不要使用。(除非有特殊用意，否则请使用`DEBUG`级别替代)

#### 1.2.2 `DEBUG`

1. 可以填写所有的想知道的相关信息(但不代表可以随便写，`debug`信息要有意义,最好有相关参数)；
1. 生产环境需要关闭`DEBUG`信息
1. 如果在生产情况下需要开启`DEBUG`,需要使用开关进行管理，不能一直开启。

#### 1.2.3 `INFO`

- 系统运行信息
	
	1. `Service`方法中对于系统/业务状态的变更；
	1. 主要逻辑中的分步骤。

- 外部接口部分
	
	1. 客户端请求参数；
	1. 调用第三方时的调用参数和调用结果。

- 说明

1. 并不是所有的`Service`都进行出入口打点记录,单一、简单`Service`是没有意义的；
1. 对于复杂的业务逻辑，需要进行日志打点，以及埋点记录，比如电商系统中的下订单逻辑，以及`OrderAction`操作(业务状态变更)；
1. 对于整个系统的提供出的接口，使用`INFO`记录入参；
1. 如果所有的`Service`为`SOA`架构，那么可以看成是一个外部接口提供方，那么必须记录入参；
1. 调用其他第三方服务时，所有的出参和入参是必须要记录的(因为你很难追溯第三方模块发生的问题)。

#### 1.2.4 `WARN`

- 不应该出现但是不影响程序、当前请求正常运行的异常情况:

	1. 有容错机制的时候出现的错误情况；
	1. 找不到配置文件，但是系统能自动创建配置文件；

- 即将接近临界值的时候，例如：缓存池占用达到警告线；
- 业务异常的记录,比如:当接口抛出业务异常时，应该记录此异常。

#### 1.2.5 `ERROR`

影响到程序正常运行、当前请求正常运行的异常情况:

1. 打开配置文件失败；
1. 所有第三方对接的异常(包括第三方返回错误码)；
1. 所有影响功能使用的异常，包括:`SQLException`和除了业务异常之外的所有异常(`RuntimeException`和`Exception`)。

> 不应该出现的情况:
> 如果进行了抛出异常操作，请不要记录`ERROR`日志，由最终处理方进行处理：

反例(**不要这么做**):

```java
try{
    ....
}catch(Exception ex){
  String errorMessage=String.format("Error while reading information of user [%s]",userName);
  logger.error(errorMessage,ex);
  throw new UserServiceException(errorMessage,ex);
}
```




## 二、`SpringBoot` 中 `logback`

### 2.1 背景

1. `SpringBoot`工程自带`logback`和`slf4j`的依赖，所以重点放在编写配置文件上，需要引入什么依赖，日志依赖冲突统统都不需要我们管了;
1. `logback`框架会默认加载`classpath`下命名为`logback-spring`或`logback`的配置文件。
1. 将所有日志都存储在一个文件中文件大小也随着应用的运行越来越大并且不好排查问题，正确的做法应该是将`ERROR`日志和其他日志分开，并且不同级别的日志根据时间段进行记录存储。

### 2.1 `logback-spring.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <!-- 属性文件:在配置文件中找到对应的配置项 -->
    <springProperty scope="context" name="logPath" source="logging.path"/>

    <!-- 输出到控制台 -->
    <appender name="CONSOLE-LOG" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>[%d{yyyy-MM-dd' 'HH:mm:ss.sss}] [%C] [%t] [%L] [%-5p] %m%n</pattern>
        </layout>
    </appender>

    <!-- 获取比info级别高(包括info级别)但除error级别的日志 -->
    <appender name="INFO-LOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 指定过滤策略 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>DENY</onMatch>
            <onMismatch>ACCEPT</onMismatch>
        </filter>
        <encoder>
            <!-- 指定日志输出格式 -->
            <pattern>[%d{yyyy-MM-dd' 'HH:mm:ss.sss}] [%C] [%t] [%L] [%-5p] %m%n</pattern>
        </encoder>

        <!-- 指定收集策略：滚动策略-->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 指定生成日志保存地址 -->
            <fileNamePattern>${logPath}/info.%d.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <appender name="ERROR-LOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 指定过滤策略 -->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder>
            <!-- 指定日志输出格式 -->
            <pattern>[%d{yyyy-MM-dd' 'HH:mm:ss.sss}] [%C] [%t] [%L] [%-5p] %m%n</pattern>
        </encoder>
        <!-- 指定收集策略：滚动策略-->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--指定生成日志保存地址 -->
            <fileNamePattern>${logPath}/error.%d.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 异步输出 -->
    <appender name="ASYNC-INFO" class="ch.qos.logback.classic.AsyncAppender">
        <!-- 不丢失日志.默认的,如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->
        <discardingThreshold>0</discardingThreshold>
        <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->
        <queueSize>256</queueSize>
        <!-- 添加附加的appender,最多只能添加一个 -->
        <appender-ref ref="INFO-LOG"/>
    </appender>

    <appender name="ASYNC-ERROR" class="ch.qos.logback.classic.AsyncAppender">
        <!-- 不丢失日志.默认的,如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->
        <discardingThreshold>0</discardingThreshold>
        <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->
        <queueSize>256</queueSize>
        <!-- 添加附加的appender,最多只能添加一个 -->
        <appender-ref ref="ERROR-LOG"/>
    </appender>

    <!-- 指定最基础的日志输出级别 -->
    <root level="info">
        <appender-ref ref="CONSOLE-LOG" />
        <appender-ref ref="INFO-LOG" />
        <appender-ref ref="ERROR-LOG" />
    </root>

</configuration>
```

## 三、在`Web`请求做入参/出参打印

### 3.1 项目配置

```xml
server:
  port: 8088
# 日志输出文件地址
logging:
  path: ./springboot-demo-log/logback-demo/logs
```

### 3.2 定义一个切面`WebLogAspect`

> 详见文中注释

```java
@Aspect
@Component
@Slf4j
public class WebLogAspect {

    /**
     * 进入方法时间戳
     */
    private Long startTime;
    /**
     * 方法结束时间戳(计时)
     */
    private Long endTime;

    public WebLogAspect() {
    }

    /**
     * 定义请求日志切入点，其切入点表达式有多种匹配方式,这里是指定路径
     */
    @Pointcut("execution(public * cn.van.log.logback.web.controller.*.*(..))")
    public void webLogPointcut() {
    }

    /**
     * 前置通知：
     * 1. 在执行目标方法之前执行，比如请求接口之前的登录验证;
     * 2. 在前置通知中设置请求日志信息，如开始时间，请求参数，注解内容等
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("webLogPointcut()")
    public void doBefore(JoinPoint joinPoint) {

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //获取请求头中的User-Agent
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        //打印请求的内容
        startTime = System.currentTimeMillis();
        log.info("请求开始时间：{}" , LocalDateTime.now());
        log.info("请求Url : {}" , request.getRequestURL().toString());
        log.info("请求方式 : {}" , request.getMethod());
        log.info("请求ip : {}" , request.getRemoteAddr());
        log.info("请求参数 : {}" , Arrays.toString(joinPoint.getArgs()));
        // 系统信息
        log.info("浏览器：{}", userAgent.getBrowser().toString());
        log.info("浏览器版本：{}", userAgent.getBrowserVersion());
        log.info("操作系统: {}", userAgent.getOperatingSystem().toString());
    }

    /**
     * 返回通知：
     * 1. 在目标方法正常结束之后执行
     * 1. 在返回通知中补充请求日志信息，如返回时间，方法耗时，返回值，并且保存日志信息
     *
     * @param ret
     * @throws Throwable
     */
    @AfterReturning(returning = "ret", pointcut = "webLogPointcut()")
    public void doAfterReturning(Object ret) throws Throwable {
        endTime = System.currentTimeMillis();
        log.info("请求结束时间：{}" , LocalDateTime.now());
        log.info("请求耗时：{}ms" , (endTime - startTime));
        // 处理完请求，返回内容
        log.info("请求返回 : {}" , ret);
    }

    /**
     * 异常通知：
     * 1. 在目标方法非正常结束，发生异常或者抛出异常时执行
     * 1. 在异常通知中设置异常信息，并将其保存
     *
     * @param throwable
     */
    @AfterThrowing(value = "webLogPointcut()", throwing = "throwable")
    public void doAfterThrowing(Throwable throwable) {
        // 保存异常日志记录
        log.error("发生异常时间：{}" , LocalDateTime.now());
        log.error("抛出异常：{}" , throwable.getMessage());
    }
}
```

### 3.3 两个测试接口

> 包含一个正常请求接口和异常接口。

```java
@RestController
@RequestMapping("/log")
public class TestController {
    /**
     * 测试正常请求
     * @param msg
     * @return
     */
    @GetMapping("/normal/{msg}")
    public String getMsg(@PathVariable String msg) {
        return msg;
    }

    /**
     * 测试抛异常
     * @return
     */
    @GetMapping("/exception/{msg}")
    public String getException(@PathVariable String msg){
        // 故意造出一个异常
        Integer.parseInt("abc123");
        return msg;
    }
}
```

### 3.4 启动项目测试


- 正常请求

打开浏览器，访问[http://localhost:8088/log/normal/hello](http://localhost:8088/log/normal/hello)

日志打印如下：

```xml
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [65] [INFO ] 请求开始时间：2019-02-24T22:37:50.892
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [66] [INFO ] 请求Url : http://localhost:8088/log/normal/hello
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [67] [INFO ] 请求方式 : GET
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [68] [INFO ] 请求ip : 0:0:0:0:0:0:0:1
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [70] [INFO ] 请求参数 : [hello]
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [72] [INFO ] 浏览器：CHROME
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [73] [INFO ] 浏览器版本：76.0.3809.100
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [74] [INFO ] 操作系统: MAC_OS_X
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [88] [INFO ] 请求结束时间：2019-02-24T22:37:50.901
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [89] [INFO ] 请求耗时：14
[2019-02-24 22:37:50.050] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-1] [91] [INFO ] 请求返回 : hello
```

- 异常请求

访问：[http://localhost:8088/log/exception/hello](http://localhost:8088/log/exception/hello)

日志打印如下：

```xml
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [65] [INFO ] 请求开始时间：2019-02-24T22:39:57.728
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [66] [INFO ] 请求Url : http://localhost:8088/log/exception/hello
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [67] [INFO ] 请求方式 : GET
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [68] [INFO ] 请求ip : 0:0:0:0:0:0:0:1
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [70] [INFO ] 请求参数 : [hello]
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [72] [INFO ] 浏览器：CHROME
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [73] [INFO ] 浏览器版本：76.0.3809.100
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [74] [INFO ] 操作系统: MAC_OS_X
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [104] [ERROR] 发生异常时间：2019-02-24T22:39:57.731
[2019-02-24 22:39:57.057] [cn.van.log.aop.logback.WebLogAspect] [http-nio-8088-exec-9] [105] [ERROR] 抛出异常：For input string: "abc123"
[2019-02-24 22:39:57.057] [org.apache.juli.logging.DirectJDKLog] [http-nio-8088-exec-9] [175] [ERROR] Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.NumberFormatException: For input string: "abc123"] with root cause
java.lang.NumberFormatException: For input string: "abc123"
```

### 四、总结

1. [Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-log/logback-demo)