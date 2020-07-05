# Spring 从应用获取 Bean 的常用姿势

> 如何在普通类中获取 `Spring` 管理的 `Bean` ,各种姿势，从本文中寻找。

通常，在`Spring`应用程序中，当我们使用`@Bean`/`@Service`/`@Controller`、`@Component`/`@Configuration`或者其它的注解将`Bean`注入的`Spring IOC`。然后我们可以使用`@Autowired`或者`@Resource`来使用由`Spring IOC`来管理的`Bean`。

## 一、从应用程序上下文中获取`Bean`

我们今天学习将来如何从`ApplicationContext`中的`Bean`。因为有些情况下我们不得不从应用程序上下文中来获取`Bean`，例如在没有注入`Spring`的工具类中。

### 1.1  在初始化时保存`ApplicationContext`对象

```java
ApplicationContext ac = new FileSystemXmlApplicationContext("applicationContext.xml"); 
ac.getBean("beanId");
```

这种方式适用于采用`Spring`框架的独立应用程序，需要程序通过配置文件手工初始化`Spring`的情况。

注意：在获取失败时抛出异常。

### 1.2 通过`Spring`提供的工具类获取`ApplicationContext`对象

```java
ApplicationContext ac1 = WebApplicationContextUtils.getRequiredWebApplicationContext(ServletContext sc); 
ApplicationContext ac2 = WebApplicationContextUtils.getWebApplicationContext(ServletContext sc); 
ac1.getBean("beanId"); 
ac2.getBean("beanId");
```

这种方式适合于采用`Spring`框架的`B/S`系统，通过`ServletContext`对象获取`ApplicationContext`对象，然后在通过它获取需要的类实例。获取失败时返回`null`。

### 1.3 继承自抽象类`ApplicationObjectSupport`

抽象类`ApplicationObjectSupport`提供`getApplicationContext()`方法，可以方便的获取`ApplicationContext`。

`Spring`初始化时，会通过该抽象类的`setApplicationContext(ApplicationContext context)`方法将`ApplicationContext` 对象注入。



### 1.4 继承自抽象类`WebApplicationObjectSupport`

类似上面方法，调用`getWebApplicationContext()`获取`WebApplicationContext`

### 1.5 通过`Spring`提供的`ContextLoader`


```java
WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
wac.getBean(beanID);
```

该方式不依赖于`servlet`,不需要注入的方式。


### 1.6 实现接口`ApplicationContextAware`(本文重点)

实现该接口的`setApplicationContext(ApplicationContext context)`方法，并保存`ApplicationContext` 对象。`Spring`初始化时，会通过该方法将`ApplicationContext`对象注入。


## 二、`Spring ApplicationContext` 工具类

### 2.1 项目准备

> 新增一个`Bean`，使其被`Spring IOC` 管理。 

- `SpringBean.java`

```java
@Component
public class SpringBean {

    private String desc;

    public String getDesc() {
        return "SpringBean desc";
    }
}
```

- `application.yml`

```xml
demo:
  value: 8888
```

### 2.2 定义一个工具类，实现 `ApplicationContextAware`

```java
@Component
public final class SpringUtils implements ApplicationContextAware {
    /**
     * Spring应用上下文环境
     */
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.applicationContext == null) {
            SpringUtils.applicationContext = applicationContext;
        }
    }

    /**
     * 获取对象，重写了bean方法
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return SpringUtils.applicationContext.getBean(clazz);
    }

    /**
     * 同上方法
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return SpringUtils.applicationContext.getBean(name);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     * @param name
     * @return
     */
    public static boolean containsBean(String name) {
        return SpringUtils.applicationContext.containsBean(name);
    }
    /**
     * 读取配置信息
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        return SpringUtils.applicationContext.getEnvironment().getProperty(key);
    }
}
```

1. 通过`@Component`注解将`SpringUtils.java`注入的`Spring IOC`，`Spring`能够为我们自动地执行 `setApplicationContext()` 方法；
1. 我在该工具类封装了三个方法，主要用于获取`Bean`/判断`Bean`是否存在/获取配置文件中信息。

### 2.3 `getBean()`

> 该方法用于获取对象，获取不到会抛异常。

- 测试

```java
@Test
public void getBeanDemo() {
    SpringBean springBean = SpringUtils.getBean(SpringBean.class);
    log.info("springBean.desc:{}", springBean.getDesc());
    // 如果bean 不存在
    log.info("springBeanOther:{}", SpringUtils.getBean("springBeanOther"));
}
```

### 2.4 `containsBean()`

> 用来判断该`Bean` 是否注入到`Spring IOC`。

- 测试

```java
@Test
public void containsBeanDemo() {
    log.info("containsBean:{}", SpringUtils.containsBean("springBean"));
    // 如果bean 不存在
    log.info("containsBean:{}", SpringUtils.containsBean("springBeanOther"));
}
```

### 2.5 `getProperty()`

> 用来获取配置文件属性，属性不存在返回`null`。

- 测试

```java
@Test
public void getPropertyDemo() {
    String value = SpringUtils.getProperty("demo.value");
    log.info("value:{}", value);
    // 配置中不存在
    String key = SpringUtils.getProperty("demo.key");
    log.info("key:{}", key);
}
```

## 三、总结

这里仅作简单展示，如需探究更多使用方法，可以直接定义`getApplicationContext()`方法，将`ApplicationContext`返回，具体详见文末工具类中代码。

```java
@Test
public void getApplicationContext() {
    ApplicationContext context = SpringUtils.getApplicationContext();
    log.info("context.containsBean():{}", context.containsBean("springBean"));
}
```

[Github 示例代码](https://github.com/vanDusty/Frame-Home/tree/master/spring-case/spring-demo)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
