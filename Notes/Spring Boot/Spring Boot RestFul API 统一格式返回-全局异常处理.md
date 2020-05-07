# Spring Boot RestFul API 统一格式返回-全局异常处理
## 一、背景

在分布式、微服务盛行的今天，绝大部分项目都采用的微服务框架，前后端分离方式。前端和后端进行交互，前端按照约定请求`URL`路径，并传入相关参数，后端服务器接收请求，进行业务处理，返回数据给前端。

所以统一接口的返回值，保证接口返回值的幂等性很重要，本文主要介绍博主当前使用的结果集。

## 二、统一格式设计

### 2.1 统一结果的一般形式

- 示例：

```xml
{
	# 是否响应成功
	success: true,
	# 响应状态码
	code: 200,		
	# 响应数据
	data: Object
	# 返回错误信息
	message: "",
}
```

### 2.2 结果类枚举

```java
public enum ResultCodeEnum {
    /*** 通用部分 100 - 599***/
    // 成功请求
    SUCCESS(200, "successful"),
    // 重定向
    REDIRECT(301, "redirect"),
    // 资源未找到
    NOT_FOUND(404, "not found"),
    // 服务器错误
    SERVER_ERROR(500,"server error"),

    /*** 这里可以根据不同模块用不同的区级分开错误码，例如:  ***/

    // 1000～1999 区间表示用户模块错误
    // 2000～2999 区间表示订单模块错误
    // 3000～3999 区间表示商品模块错误
    // 。。。

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
}
```
- `code`：响应状态码

一般小伙伴们是在开发的时候需要什么，就添加什么。**但是**，为了规范，我们应当参考`HTTP`请求返回的状态码。

|   | code区间 | 类型 | 含义 |
| -- | -- | -- | -- |
| 1** | 100-199 | 信息 | 服务器接收到请求，需要请求者继续执行操作 |
| 2** | 200-299 | 成功 | 请求被成功接收并处理 | 
| 3** | 300-399 | 重定向 | 需要进一步的操作以完成请求 |
| 4** | 400-499 | 客户端错误 | 请求包含语法错误或无法完成请求 |
| 5** | 500-599 | 服务器错误 | 服务器在处理的时候发生错误 |

常见的`HTTP`状态码：

1. `200` - 请求成功；
1. `301` - 资源（网页等）被永久转移到其它`URL`；
1. `404` - 请求的资源（网页等）不存在；
1. `500` - 内部服务器错误。

- `message`：错误信息

在发生错误时，如何友好的进行提示？

1. 根据`code` 给予对应的错误码定位；
2. 把错误描述记录到`message`中，便于接口调用者更详细的了解错误。

### 2.3 统一结果类

```java
public class HttpResult <T> implements Serializable {

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
    private HttpResult() {
        this.code = 200;
        this.success = true;
    }
    /**
     * 有参构造器
     * @param obj
     */
    private HttpResult(T obj) {
        this.code = 200;
        this.data = obj;
        this.success = true;
    }

    /**
     * 有参构造器
     * @param resultCode
     */
    private HttpResult(ResultCodeEnum resultCode) {
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
    public static<T> HttpResult<T> success(){
        return new HttpResult();
    }

    /**
     * 返回成功（有返回结果）
     * @param data
     * @param <T>
     * @return
     */
    public static<T> HttpResult<T> success(T data){
        return new HttpResult<T>(data);
    }

    /**
     * 通用返回失败
     * @param resultCode
     * @param <T>
     * @return
     */
    public static<T> HttpResult<T> failure(ResultCodeEnum resultCode){
        return  new HttpResult<T>(resultCode);
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
        return "HttpResult{" +
                "success=" + success +
                ", code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
```

**说明：**

1. 构造器私有，外部不可以直接创建；
1. 只可以调用统一返回类的静态方法返回对象；
1. `success` 是一个`Boolean` 值，通过这个值，可以直接观察到该次请求是否成功；
1. `data` 表示响应数据，用于请求成功后，返回客户端需要的数据。

## 三、测试及总结

### 3.1 简单的接口测试

```java
@RestController
@RequestMapping("/httpRest")
@Api(tags = "统一结果测试")
public class HttpRestController {

    @ApiOperation(value = "通用返回成功（没有返回结果）", httpMethod = "GET")
    @GetMapping("/success")
    public HttpResult success(){
        return HttpResult.success();
    }

    @ApiOperation(value = "返回成功（有返回结果）", httpMethod = "GET")
    @GetMapping("/successWithData")
    public HttpResult successWithData(){
        return HttpResult.success("风尘博客");
    }

    @ApiOperation(value = "通用返回失败", httpMethod = "GET")
    @GetMapping("/failure")
    public HttpResult failure(){
        return HttpResult.failure(ResultCodeEnum.NOT_FOUND);
    }

}
```

> 这里 `Swagger`以及`SpringMVC`的配置就没贴出来了，详见Github 示例代码。

### 3.2 返回结果

[http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)

```xml
{
  "code": 200,
  "success": true
}
```

```xml
{
  "code": 200,
  "data": "风尘博客",
  "success": true
}
```

```xml
{
  "code": 404,
  "message": "not found",
  "success": false
}
```


## 四、全局异常处理

> 使用统一返回结果时，还有一种情况，就是程序的报错是由于运行时异常导致的结果，有些异常是我们在业务中抛出的，有些是无法提前预知。

因此，我们需要定义一个统一的全局异常，在`Controller`捕获所有异常，并且做适当处理，并作为一种结果返回。

### 4.1 设计思路：
	
1. 自定一个异常类（如：`TokenVerificationException`），捕获针对项目或业务的异常;
1. 使用`@ExceptionHandler`注解捕获自定义异常和通用异常；
1. 使用`@ControllerAdvice`集成`@ExceptionHandler`的方法到一个类中；
1. 异常的对象信息补充到统一结果枚举中；

### 4.2 自定义异常

```java
public class TokenVerificationException extends RuntimeException {

    /**
     * 错误码
     */
    protected Integer code;

    protected String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 有参构造器，返回码在枚举类中，这里可以指定错误信息
     * @param msg
     */
    public TokenVerificationException(String msg) {
        super(msg);
    }
}
```

### 4.3 统一异常处理器

`@ControllerAdvice`注解是一种作用于控制层的切面通知（`Advice`），能够将通用的`@ExceptionHandler`、`@InitBinder`和`@ModelAttributes`方法收集到一个类型，并应用到所有控制器上。

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常捕获
     * @param e 捕获的异常
     * @return 封装的返回对象
     **/
    @ExceptionHandler(Exception.class)
    public HttpResult handlerException(Exception e) {
        ResultCodeEnum resultCodeEnum;
        // 自定义异常
        if (e instanceof TokenVerificationException) {
            resultCodeEnum = ResultCodeEnum.TOKEN_VERIFICATION_ERROR;
            resultCodeEnum.setMessage(getConstraintViolationErrMsg(e));
            log.error("tokenVerificationException：{}", resultCodeEnum.getMessage());
        }else {
            // 其他异常，当我们定义了多个异常时，这里可以增加判断和记录
            resultCodeEnum = ResultCodeEnum.SERVER_ERROR;
            resultCodeEnum.setMessage(e.getMessage());
            log.error("common exception:{}", JSON.toJSONString(e));
        }
        return HttpResult.failure(resultCodeEnum);
    }

    /**
     * 获取错误信息
     * @param ex
     * @return
     */
    private String getConstraintViolationErrMsg(Exception ex) {
        // validTest1.id: id必须为正数
        // validTest1.id: id必须为正数, validTest1.name: 长度必须在有效范围内
        String message = ex.getMessage();
        try {
            int startIdx = message.indexOf(": ");
            if (startIdx < 0) {
                startIdx = 0;
            }
            int endIdx = message.indexOf(", ");
            if (endIdx < 0) {
                endIdx = message.length();
            }
            message = message.substring(startIdx, endIdx);
            return message;
        } catch (Throwable throwable) {
            log.info("ex caught", throwable);
            return message;
        }
    }
}
```

- 说明

1. 我使用的是`@RestControllerAdvice` ，等同于`@ControllerAdvice` + `@ResponseBody`
2.  错误枚举类这里省略了，详见[Github代码](https://github.com/vanDusty/SpringBoot-Home/blob/master/springboot-demo-restful/rest-api/src/main/java/cn/van/restful/enunm/ResultCodeEnum.java)。


## 五、测试及总结

### 5.1 测试接口

```java
@RestController
@RequestMapping("/exception")
@Api(tags = "异常测试接口")
public class ExceptionRestController {

    @ApiOperation(value = "业务异常(token 异常)", httpMethod = "GET")
    @GetMapping("/token")
    public HttpResult token() {
        // 模拟业务层抛出 token 异常
        throw new TokenVerificationException("token 已经过期");
    }


    @ApiOperation(value = "其他异常", httpMethod = "GET")
    @GetMapping("/errorException")
    public HttpResult errorException() {
        //这里故意造成一个其他异常，并且不进行处理
        Integer.parseInt("abc123");
        return HttpResult.success();
    }
}
```


### 5.2 返回结果

[http://localhost:8080/swagger-ui.html#/](http://localhost:8080/swagger-ui.html#/)

```xml
{
  "code": 500,
  "message": "For input string: \"abc123\"",
  "success": false
}
```

```xml
{
  "code": 4000,
  "message": "token 已经过期",
  "success": false
}
```

### 5.3 小结


`@RestControllerAdvice`和`@ExceptionHandler`会捕获所有`Rest`接口的异常并封装成我们定义的`HttpResult`的结果集返回，但是：**处理不了拦截器里的异常**


### 六、总结

没有哪一种方案是适用于各种情况的，如：分页情况，还可以增加返回分页结果的静态方案，具体实现，这里就不展示了。所以，适合自己的，具有一定可读性都是很好的，欢迎持不同意见的大佬给出意见建议。

### 6.1 示例代码

[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-restful/rest-api)