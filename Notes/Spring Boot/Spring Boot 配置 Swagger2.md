# Spring Boot 配置 Swagger2

## 一、背景介绍

### 1.1 `Swagger` 介绍

`Swagger` 是一套基于 `OpenAPI` 规范构建的开源工具，可以帮助我们设计、构建、记录以及使用 `Rest API`。`Swagger` 主要包含了以下三个部分：

1. `Swagger Editor`：基于浏览器的编辑器，我们可以使用它编写我们 `OpenAPI` 规范。
1. `Swagger UI`：它会将我们编写的 `OpenAPI` 规范呈现为交互式的 `API` 文档，后文我将使用浏览器来查看并且操作我们的 `Rest API`。
1. `Swagger Codegen`：它可以通过为 `OpenAPI`（以前称为 Swagger）规范定义的任何 `API` 生成服务器存根和客户端 `SDK` 来简化构建过程。


### 1.2 `Swagger` 优缺点

* 优点

1. 易用性好。`Swagger UI`提供很好的`API`接口的`UI`界面，可以很方面的进行`API`接口的调用;
1. 时效性和可维护性好，`API`文档随着代码变更而变更。 `Swagger`是根据注解来生成文`API`档的，我们可以在变更代码的时候顺便更改相应的注解即可；
1. 易于测试，可以将文档规范导入相关的工具（例如 `SoapUI`）, 这些工具将会为我们自动地创建自动化测试。

* 缺点

1. 重复利用性差，因为`Swagger`毕竟是网页打开，在进行接口测试的时候很多参数无法进行保存，因此不易于重复利用。
1. 复杂的场景不易模拟，比如使用`token`鉴权的，可能每次都需要先模拟登录，再来进行接口调用。


### 1.3 `Swagger` 相关地址

1. `Swagger`官网：[http://swagger.io](http://swagger.io)
1. `Swagger`的`GitHub`地址：[https://github.com/swagger-api](https://github.com/swagger-api)

## 二、上手使用

### 2.1 项目准备

* 项目依赖

```pom
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
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
<!-- lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>1.8.4</scope>
</dependency>
```

* 项目配置

```yml
## 项目端口和项目路径
server:
  port: 8081
  servlet:
    context-path: /swagger
```

### 2.2 Swagger2 配置

> 在启动的时候添加`@EnableSwagger2`注解开启，然后再使用`@Bean`注解初始化一些相应的配置。


```java
@EnableSwagger2
@Configuration
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 指定controller存放的目录路径
                .apis(RequestHandlerSelectors.basePackage("cn.van.swagger.demo.web.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 文档标题
                .title("这里是Swagger2构建Restful APIs")
                // 文档描述
                .description("这里是文档描述")
                .termsOfServiceUrl("https://www.dustyblog.cn")
                //定义版本号
                .version("v1.0")
                .build();
    }

}
```

### 2.3 接口编写（控制器）

`Swagger` 主要的使用就是在控制层这块，它是通过一些注解来为接口提供`API`文档。下列是`Swagger`的一些注解说明，更详细的可以查看官方的wiki文档。

1. `@Api` ：将类标记为`Swagger`资源，并给该类增加说明
1. `@ApiOperation` :注解来给接口方法增加说明；
1. `@ApiImplicitParam` ：表示`API`操作中的单个参数。
1. `@ApiImplicitParams` ：一个包装器，允许列出多个`ApiImplicitParam`对象。
1. `@ApiModel` ：提供有关`Swagger`模型的其他信息，比如描述`POJO`对象。
1. `@ApiModelProperty` ： 添加和操作模型属性的数据。
1. `@ApiOperation` ： 描述针对特定路径的操作或通常是`HTTP`方法。
1. `@ApiParam` ： 为操作参数添加其他元数据。
1. `@ApiResponse` ： 描述操作的可能响应。
1. `@ApiResponses` ： 一个包装器，允许列出多个`ApiResponse`对象。
1. `@Authorization` ： 声明要在资源或操作上使用的授权方案。
1. `@AuthorizationScope` ： 描述`OAuth2`授权范围。
1. `@ResponseHeader` ： 表示可以作为响应的一部分提供的标头。
1. `@ApiProperty` ： 描述`POJO`对象中的属性值。
1. `@ApiError` ： 接口错误所返回的信息

官方`wiki`文档地址:

[https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations)

#### 2.3.1 参数实体`UserDTO`

```java
@Data
@ApiModel(value = "用户信息对象", description = "姓名、性别、年龄")
public class UserDTO {
    @ApiModelProperty(value = "主键id")
    private Long id;
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "用户性别")
    private String sex;
    @ApiModelProperty(value = "用户年龄")
    private Integer age;
    /**
     * 隐藏字段
     */
    @ApiModelProperty(value = "隐藏字段",hidden = true)
    private String extra;
}
```

#### 2.3.2 接口方法示例

```java
@RestController
@RequestMapping("/swagger")
@Api(tags = "Swagger接口")
public class SwaggerController {
    /**
     *  无参方法
     * @return
     */
    @ApiOperation(value = "无参方法", httpMethod = "GET")
    @GetMapping("/sayHello")
    public String sayHello(){
        return "hello Swagger!";
    }

    /**
     * 单参方法
     * @param id
     * @return
     */
    @ApiImplicitParam( name = "id", value = "主键id")
    @ApiOperation(value = "单参方法", httpMethod = "POST")
    @PostMapping("/hasParam")
    public Long hasParam(Long id) {
        return id;
    }

    /**
     * 多参方法(required = true可指定某个参数为必填)
     * @param id
     * @param userName
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam( name = "id", value = "主键id", required = true),
            @ApiImplicitParam( name = "userName", value = "用户名", required = true)
    })
    @ApiOperation(value = "多参方法", httpMethod = "POST")
    @PostMapping("/hasParams")
    public String hasParams(Long id, String userName) {
        return userName;
    }

    /**
     * 参数是实体类的方法（需要在实体类中增加注解进行参数说明）
     * @param userDTO
     * @return
     */
    @ApiOperation(value = "实体参数方法", httpMethod = "PUT")
    @PutMapping("/entityParam")
    public UserDTO entityParam(@RequestBody UserDTO userDTO) {
        return userDTO;
    }

    /**
     * 被忽略的接口，该接口不会在Swagger上显示
     * @return
     */
    @DeleteMapping(path = "/ignore")
    @ApiIgnore(value = "这是被忽略的接口，将不会在Swagger上显示")
    public String ignoreApi() {
        return "测试";
    }
}
```

### 2.4 测试

启动项目，打开`swagger`接口地址：[http://localhost:8081/swagger/swagger-ui.html](http://localhost:8081/swagger/swagger-ui.html)



## 三、配置多个接口路径的配置

### 3.1 分析

根据上面的配置文件可知单个接口路径的配置是：

```java
**.apis(RequestHandlerSelectors.basePackage("cn.van.swagger.group.web.controller"))
```

那我们就来参考一下`RequestHandlerSelectors.java`的源码

```java
public class RequestHandlerSelectors {
    private RequestHandlerSelectors() {
        throw new UnsupportedOperationException();
    }

    public static Predicate<RequestHandler> any() {
        return Predicates.alwaysTrue();
    }

    public static Predicate<RequestHandler> none() {
        return Predicates.alwaysFalse();
    }

    public static Predicate<RequestHandler> withMethodAnnotation(final Class<? extends Annotation> annotation) {
        return new Predicate<RequestHandler>() {
            public boolean apply(RequestHandler input) {
                return input.isAnnotatedWith(annotation);
            }
        };
    }

    public static Predicate<RequestHandler> withClassAnnotation(final Class<? extends Annotation> annotation) {
        return new Predicate<RequestHandler>() {
            public boolean apply(RequestHandler input) {
                return (Boolean)RequestHandlerSelectors.declaringClass(input).transform(RequestHandlerSelectors.annotationPresent(annotation)).or(false);
            }
        };
    }

    private static Function<Class<?>, Boolean> annotationPresent(final Class<? extends Annotation> annotation) {
        return new Function<Class<?>, Boolean>() {
            public Boolean apply(Class<?> input) {
                return input.isAnnotationPresent(annotation);
            }
        };
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return new Function<Class<?>, Boolean>() {
            public Boolean apply(Class<?> input) {
                return ClassUtils.getPackageName(input).startsWith(basePackage);
            }
        };
    }
	/**
   * Predicate 匹配RequestHandler，并为处理程序方法的类提供基本包名.
   * predicate 包括与所提供的basePackage匹配的所有请求处理程序
   *
   * @param basePackage - base package of the classes
   * @return this
   */
    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return new Predicate<RequestHandler>() {
            public boolean apply(RequestHandler input) {
                return (Boolean)RequestHandlerSelectors.declaringClass(input).transform(RequestHandlerSelectors.handlerPackage(basePackage)).or(true);
            }
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }
}
```

我们看到 `Swagger` 是通过 `Predicate` 的`apply()` 方法的返回值来判断是非匹配的,所以我们的方案是：**通过改造`basePackage()`方法来实现多包扫描**

### 3.2 验证

- `Swagger2Config.java`多包扫描配置

```java
@EnableSwagger2
@Configuration
public class Swagger2Config {

    /**
     * 定义分隔符
     */
    private static final String SEPARATE = ";";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 指定controller存放的目录路径
                // 单个路径
                // .apis(RequestHandlerSelectors.basePackage("cn.van.swagger.group.web.controller"))
                // 多个路径
                .apis(basePackage("cn.van.swagger.group.web.controller" + SEPARATE +"cn.van.swagger.group.api"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 文档标题
                .title("这里是Swagger2构建Restful APIs")
                // 文档描述
                .description("这里是文档描述")
                .termsOfServiceUrl("https://www.dustyblog.cn")
                //定义版本号
                .version("v1.0")
                .build();
    }
    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(true);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage)     {
        return input -> {
            // 循环判断匹配
            for (String strPackage : basePackage.split(SEPARATE)) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }
}
```


### 3.3 测试

- 在`api`包下建立另一个接口类：`MultipleController.java`

```java
@RestController
@RequestMapping("/swagger2")
@Api(tags = "第二个包的Swagger接口")
public class MultipleController {
    /**
     *  无参方法
     * @return
     */
    @GetMapping("/sayHello")
    public String sayHello(){
        return "hello MultipleController!";
    }
}
```

- 启动项目，查看`Swagger`接口

![风尘博客-0](/File/Imgs/article/swagger_group_0.png)

由上图可知扫描到两个路径的接口，说明配置多个接口路径成功！

## 四、接口分组

### 4.1 背景

我们在 `Spring Boot`中定义各个接口是以`Controller`作为第一级维度来进行组织的，`Controller`与具体接口之间的关系是**一对多**的关系。我们通常将同属一个模块的接口定义在一个`Controller`里。

默认情况下，`Swagger`是以`Controller`为单位，对接口进行分组管理的，这个分组的元素在`Swagger`中称为`Tag`。但是这里的`Tag`与接口的关系并不是一对多的，它**支持更丰富的多对多关系**。

### 4.2 默认分组

默认情况下，`Swagger`是如何根据`Controller`来组织`Tag`与接口关系的。例如，定义两个`Controller`：

- `TeacherController.java`

```java
@Api(tags = "教师工作")
@RestController
@RequestMapping(value = "/teacher")
public class TeacherController {

    @PostMapping("/teaching")
    @ApiOperation(value = "教书")
    public String teaching() {
        return "teaching....";
    }

    @PostMapping("/preparing")
    @ApiOperation(value = "备课")
    public String preparing() {
        return "preparing....";
    }
}
```

- `StudentController.java`

```java
@Api(tags = "学生任务")
@RestController
@RequestMapping(value = "/student")
public class StudentController {

    @ApiOperation("学习")
    @GetMapping("/study")
    public String study() {
        return "study....";
    }
}
```

启动项目，查看此时`Swagger`接口

![Swagger 分组-1](/File/Imgs/article/swagger_group_1.png)

到这里，我们还都只是`Tag`与`Controller`**一一对应**。

### 4.3 合并分组

`Swagger`中还支持更灵活的分组，查看`@Api`源码，`tags`属性其实是个数组类型：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Api {
    String value() default "";

    String[] tags() default {""};
	...    
}
```

由此可以推测：**可以通过定义同名的`Tag`来汇总`Controller`中的接口**。

### **上手验证**

- 定义一个`Tag`为“教学管理”，让这个分组同时包含教师工作和学生任务的所有接口。

- `TeacherController.java`

```java
@Api(tags = {"教师工作", "教学管理"})
@RestController
@RequestMapping(value = "/teacher")
public class TeacherController {

    @PostMapping("/teaching")
    @ApiOperation(value = "教书")
    public String teaching() {
        return "teaching....";
    }

    @PostMapping("/preparing")
    @ApiOperation(value = "备课")
    public String preparing() {
        return "preparing....";
    }
}
```

- `StudentController.java`

```java
@Api(tags = {"学生任务", "教学管理"})
@RestController
@RequestMapping(value = "/student")
public class StudentController {

    @ApiOperation("学习")
    @GetMapping("/study")
    public String study() {
        return "study....";
    }
}
```

- 启动项目，`Swagger`接口如下:

![Swagger 分组-2](/File/Imgs/article/swagger_group_2.png)

此时，学生和教师的接口均在**教学管理**之下，说明该方案下合并分组成功。

### 4.4 精准分组

> 通过`@Api`可以实现将指定`Controller`中的所有接口合并到一个`Tag`中，但是如果产品希望:**教学管理**接口下包含**学生任务**中所有接口以及**教师工作**管理中的接口`teaching`（**不包含接口`preparing`**）。

- 通过`@ApiOperation`中的`tags`属性做更细粒度的接口划分,修改`TeacherController.java`

```java
@Api(tags = "教师工作")
@RestController
@RequestMapping(value = "/teacher")
public class TeacherController {

    @PostMapping("/teaching")
    @ApiOperation(value = "教书", tags = "教学管理")
    public String teaching() {
        return "teaching....";
    }

    @PostMapping("/preparing")
    @ApiOperation(value = "备课")
    public String preparing() {
        return "preparing....";
    }

}
```

- 启动项目，`Swagger`接口如下:

![Swagger 分组-3](/File/Imgs/article/swagger_group_3.png)

此时，教师工作中的接口`preparing`不在**教学管理**中，说明粒度更细的精准分组生效。


## 五、总结

1. [Github 示例代码-demo](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-restful/swagger-demo)

1. [Github 示例代码-分组和多包扫描](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-restful/swagger-group)