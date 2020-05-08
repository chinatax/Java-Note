# Spring Boot 过滤器+拦截器+监听器

## 一、过滤器 - `Filter`

**过滤器是处于客户端和服务器资源文件之间的一道过滤网，帮助我们过滤掉一些不符合要求的请求。**

### 1.1 过滤器介绍

> 过滤器依赖于`Servlet`容器

过滤器可以拦截到方法的请求和响应(`ServletRequest request`, `ServletResponse response`),并对请求/响应做出过滤操作。

### 1.2 过滤器用途

用来做一些过滤操作，获取我们想要获取的数据：

1. 在过滤器中修改字符编码；
1. 在过滤器中修改`HttpServletRequest`的一些参数，包括：过滤低俗文字、危险字符等；
1. 用作 `Session` 校验，判断用户权限。

> 一个过滤器实例只能在容器初始化时调用一次。

### 1.3 过滤器的使用

`Filter`随`Web`应用的启动而启动，只初始化一次，随`Web`应用的停止而销毁。

1. 启动服务器时加载过滤器的实例，并调用`init()`方法来初始化实例；
2. 每一次请求时都只调用方法`doFilter()`进行处理；
3. 停止服务器时调用`destroy()`方法，销毁实例。

### 1.4 示例代码

首先需要实现 `Filter.java`接口然后重写它的三个方法，对包含我们要求的请求予以放行，将其它请求拦截并重定向。

```java
@Slf4j
public class MyFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 启动服务器时加载
        log.info("MyFilter init()");
    }

    // 每次请求都会调用该方法
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
        String requestUri = request.getRequestURI();
        log.info("本次请求地址是：{}", requestUri);
        if (requestUri.contains("/addSession")
                || requestUri.contains("/removeSession")
                || requestUri.contains("/getUserCount")) {
            filterChain.doFilter(servletRequest, response);
        } else {
            // 除了以上三个链接，其他请求转发到 /redirectUrl
            wrapper.sendRedirect("/redirectUrl");
        }
    }

    @Override
    public void destroy() {
        // 在服务关闭时销毁
        log.info("MyFilter destroy()");
    }
}
```


## 二、拦截器

`Java` 中的拦截器是动态拦截 `Action` 调用的对象，然后提供了可以在 `Action` 执行前后增加一些操作，也可以在 `Action` 执行前停止操作，功能与过滤器类似，但是标准和实现方式不同。

### 2.1 拦截器介绍

依赖于 `Web` 框架，在 `Spring` 中依赖于 `SpringMVC` 框架。在实现上,基于 `Java` 的反射机制，属于面向切面编程（`AOP`）的一种运用，就是在一个方法前，调用一个方法，或者在方法后，调用一个方法。

### 2.2 拦截器的用途

1. 登录认证：在一些应用中，可能会通过拦截器来验证用户的登录状态，如果没有登录或者登录失败，就会给用户一个友好的提示或者返回登录页面；
1. 记录系统日志：记录用户的请求信息，如请求 `Ip`，方法执行时间等，通过这些记录可以监控系统的状况，以便于对系统进行信息监控、信息统计、计算 `PV`、性能调优等；
1. 通用处理：如将接口封装成统一结果集返回。

### 2.3 拦截器的使用

我们需要实现 `HandlerInterceptor` 类，并且重写三个方法

1. `preHandle()`：在 `Controoler` 处理请求之前被调用，返回值是 `boolean`类型，如果是`true`就进行下一步操作；若返回`false`，则证明不符合拦截条件。在失败的时候不会包含任何响应，此时需要调用对应的`response`返回对应响应；
1. `postHandler()`：在 `Controoler` 处理请求执行完成后、生成视图前执行，可以通过`ModelAndView`对视图进行处理，当然`ModelAndView`也可以设置为 `null`；
1. `afterCompletion()`：在 `DispatcherServlet` 完全处理请求后被调用，通常用于记录消耗时间，也可以对一些资源进行处理。

### 2.4 示例代码

```java
@Component
@Slf4j
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("【MyInterceptor】调用了:{}", request.getRequestURI());
        request.setAttribute("requestTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception{
        if (!request.getRequestURI().contains("/getUserCount")) {
            HttpSession session = request.getSession();
            String sessionName = (String) session.getAttribute("name");
            if ("Van".equals(sessionName)) {
                log.info("【MyInterceptor】当前浏览器存在 session:{}",sessionName);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        long duration = (System.currentTimeMillis() - (Long)request.getAttribute("requestTime"));
        log.info("【MyInterceptor】[{}]调用耗时:{}ms",request.getRequestURI(), duration);
    }
}
```

1. 当`Controoler`内部有异常，`posthandler`方法是不会执行的；
1. 不管`Controoler`内部是否有异常，都会执行`afterCompletion`方法；此方法还会有个`Exception ex`这个参数；如果有异常，`ex`会有异常值；没有异常 此值为`null`；
1. 如果`Controoler`内部有异常，但异常被`@ControllerAdvice`异常统一捕获的话，`ex`也会为`null`。


## 三、监听器

### 3.1 监听器介绍

监听器通常用于监听 `Web` 应用程序中对象的创建、销毁等动作的发送，同时对监听的情况作出相应的处理，最常用于统计网站的在线人数、访问量等。

### 3.2 监听器的用途

大概分为以下几种：

1. `ServletContextListener`：用来监听 `ServletContext` 属性的操作，比如新增、修改、删除；
1. `HttpSessionListener`：用来监听 `Web` 应用中的 `Session` 对象，通常用于统计在线情况；
1. `ServletRequestListener`：用来监听 `Request` 对象的属性操作。

### 3.3 监听器的使用

- `HttpSessionListener`

通常用来统计当前在线人数、`ip` 等信息，为了避免并发问题我们使用`AtomicInteger`来计数。

- `ServletContext`

是一个全局的储存信息的空间，它的生命周期与 `Servlet` 容器也就是服务器保持一致，服务器关闭才销毁。`request`，一个用户可有多个；`session`，一个用户一个；而 `ServletContext`，所有用户共用一个。所以，为了节省空间，提高效率，`ServletContext` 中，要放必须的、重要的、所有用户需要共享的线程又是安全的一些信息。因此我们用 `ServletContext` 来存储在线人数最为合适。


### 3.4 示例代码

```java
@Slf4j
public class MyHttpSessionListener implements HttpSessionListener {

    /**
     * 在线人数
     */
    public static AtomicInteger userCount = new AtomicInteger(0);

    @Override
    public synchronized void sessionCreated(HttpSessionEvent se) {
        userCount.getAndIncrement();
        se.getSession().getServletContext().setAttribute("sessionCount", userCount.get());
        log.info("【在线人数】人数增加为:{}",userCount.get());
    }

    @Override
    public synchronized void sessionDestroyed(HttpSessionEvent se) {
        userCount.getAndDecrement();
        se.getSession().getServletContext().setAttribute("sessionCount", userCount.get());
        log.info("【在线人数】人数减少为:{}",userCount.get());
    }
}
```


> `Spring Boot` 中监听器也可通过实现`ApplicationListener`接口实现，这里就不演示了。

## 四、过滤器、拦截器、监听器

### 4.1 过滤器与拦截器的区别

**1. 参考标准**

*   过滤器是 `JavaEE` 的标准，依赖于 `Servlet` 容器，生命周期也与容器一致，利用这一特性可以在销毁时释放资源或者数据入库；
*   拦截器是 `SpringMVC` 中的内容，依赖于 `Web` 框架，通常用于验证用户权限或者记录日志，但是这些功能也可以利用 `AOP` 来代替。

**2. 实现方式**

*   过滤器是基于回调函数实现，无法注入 `IOC` 容器；
*   拦截器是基于反射来实现，因此拦截器中可以注入 `IOC` 容器中。

### 4.2 实例化三器

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    MyInterceptor myInterceptor;

    /**
     * 注册过滤器
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new MyFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }
    
    /**
     * 注册拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myInterceptor);
    }

    /**
     * 注册监听器
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean registrationBean(){
        ServletListenerRegistrationBean registrationBean = new ServletListenerRegistrationBean();
        registrationBean.setListener(new MyHttpSessionListener());
        return registrationBean;
    }
}
```

### 4.3 测试接口

```java
@RestController
@RequestMapping("")
public class TestController {

    /**
     * 增加在线人数
     * @param request
     */
    @GetMapping("/addSession")
    public void addSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("name", "Van");
    }

    /**
     * 减少在线人数
     * @param request
     */
    @GetMapping("/removeSession")
    public void removeSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
    }

    /**
     * 统计在线人数
     * @return
     */
    @GetMapping("/getUserCount")
    public String getUserCount() {
        return "当前在线人数" + MyHttpSessionListener.userCount.get() + "人";
    }

    /**
     * 重定向的地址
     * @return
     */
    @GetMapping("/redirectUrl")
    public String redirectUrl() {
        return "欢迎关注：风尘博客！";
    }

}
```

## 五、测试

- 启动项目，过滤器成功加载：

```xml
...
cn.van.fil.filter.MyFilter               : MyFilter init()
....
```

### 5.1 查看在线人数

- 接口链接

[http://localhost:8080/getUserCount](http://localhost:8080/getUserCount)

- 返回结果：

```xml
当前在线人数0人
```
- 控制台日志：

```xml
c.v.fil.listener.MyHttpRequestListener   : 接口：/getUserCount被调用
cn.van.fil.filter.MyFilter               : 本次请求地址是：/getUserCount
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】调用了:/getUserCount
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】[/getUserCount]调用耗时:1ms
c.v.fil.listener.MyHttpRequestListener   : request 监听器被销毁
```

### 5.2 增加在线人数

- 接口链接

[http://localhost:8080/addSession](http://localhost:8080/addSession)

- 无返回结果：

- 控制台日志：

```xml
cn.van.fil.filter.MyFilter               : 本次请求地址是：/addSession
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】调用了:/addSession
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】当前浏览器存在 session:Van
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】[/addSession]调用耗时:16ms
```

### 5.3 再次请求 `5.1` 查看在线人数接口

```xml
当前在线人数1人
```
- 控制台日志：

```xml
cn.van.fil.filter.MyFilter               : 本次请求地址是：/getUserCount
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】调用了:/getUserCount
cn.van.fil.interceptor.MyInterceptor     : 【MyInterceptor】[/getUserCount]调用耗时:2ms
```

### 5.4 减少在线人数

[http://localhost:8080/addSession](http://localhost:8080/addSession)

## 六、总结

[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-list/filter-interceptor-listener)

> 参考文章：[SpringBoot使用拦截器、过滤器、监听器](https://juejin.im/post/5d64f867f265da03cf7a9d15?utm_source=gold_browser_extension)