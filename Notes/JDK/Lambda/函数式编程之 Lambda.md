# 函数式编程之 Lambda

`Java`是一门强大的面向对象的语言，除了`8`种基本的数据类型，其他一切皆为对象。因此，在`Java`中定义函数或方法都离不开对象，也就意味着很难直接将方法或函数像参数一样传递，而`Java8`中的`Lambda`表达式解决了这个问题。

## 一、为什么需要`Lambda`？

简单的来说，引入`Lambda`就是为了简化代码，允许把函数作为一个方法的参数传递进方法中。

### 1.1 真的简化了？

> 示例：如果想把某个接口的实现类作为参数传递给一个方法会怎么做？

- `JDK 8`以前

```java
public void general() {
    // 用匿名内部类的方式来创建线程
    new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("公众号：风尘博客！");
        }
    }).run();
}
```

- `Lambda` 写法

```java
public void lambda() {
    // 使用Lambda来创建线程
    new Thread(() -> System.out.println("公众号：风尘博客！")).run();
}
```    
### 1.2 `Lambda`表达式是什么？


`Java`中，**将方法作为参数进行传递的方式被称为`Lambda`表达式**。

### 1.3 `Lambda` 表达式语法结构 

`Lambda`其实是一个箭头函数，也可称为匿名函数:`->`

箭头操作符将`Lambda`表达式分成了两部分：

1. 左侧：`Lambda`表达式的参数列表(接口中抽象方法的参数列表)
1. 右侧：`Lambda`表达式中所需执行的功能(`Lambda`体，对抽象方法的实现)


### 1.4 语法格式

- 无参，无返回值，`Lambda` 体只需一条语句。

```java
@Test
public void noParam() {
    Runnable r1 = () -> logger.info("noParam Test!");
    r1.run();
}
```

- `Lambda` 需要一个参数，参数的小括号可以省略。

```java
@Test
public void oneParam() {
    // Consumer<String> con = (s) -> logger.info(s);
    // 参数的小括号可以省略。
    Consumer<String> con = s -> logger.info(s);
    con.accept("oneParam Test!");
}
```


- `Lambda` 需要多个参数，并且有返回值。

```java
@Test
public void params() {
    Comparator<Integer> com = (x, y) -> {
        logger.info("params Test!");
        // 比较x/y的大小
        return Integer.compare(x, y);
    };
    logger.info("result:[{}]", com.compare(1, 2));
}
```

- 当 `Lambda` 体只有一条语句时，`return` 与大括号可以省略。

```java
@Test
public void one() {
    Comparator<Integer> com = (x, y) -> Integer.compare(x, y);
    logger.info("result:[{}]", com.compare(1, 2));
}
```    

> 上面几条示例好像有一个共性：参数列表的数据类型都没写，这是为什么呢？

### 1.5 类型推断

`Lambda` 表达式中的参数类型都是由编译器推断得出的。

```java
@Test
public void typeInference() {
    //Integer 类型可以省略
    Comparator<Integer> com = (Integer x, Integer y) -> {
        logger.info("函数式接口");
        return Integer.compare(x, y);
    };
    // 类型推断
    BinaryOperator<Long> addImplicit = (x, y) -> x + y;
}
```

`Lambda` 表达式中无需指定类型，程序依然可 以编译，这是因为 `javac`根据程序的上下文，在后台 推断出了参数的类型。`Lambda` 表达式的类型依赖于上下文环境，是由编译器推断出来的。

### 1.6 小节

`Lambda`表达式使得`Java`拥有了函数式编程的能力，但在`Java`中`Lambda`表达式是对象，它必须依附于一类特别的对象类型——函数式接口(`functional interface`)。

## 二、函数式接口

函数接口是只有一个抽象方法的接口，用作 `Lambda` 表达式的类型。使用`@FunctionalInterface`注解修饰的类，编译器会检测该类是否只有一个抽象方法或接口，否则，会报错。可以有多个默认方法，静态方法。

`JDK8`在 `java.util.function` 中定义了几个标准的函数式接口，供我们使用。

### 2.1 `Java` 内置四大核心函数式接口


| 函数式接口 | 参数类型 | 返回类型 | 用途 |
| - | - | - | - |
| Consumer\<T>  | T | void | 对类型为T的对象应用操作，包含方法：void accept(T t) |
| Supplier\<T>  | 无 | T | 返回类型为T的对象，包 含方法：T get(); |
| Function<T,R> | T | R | 对类型为T的对象应用操作，并返回结果。结果是R类型的对象。包含方法：R apply(T t); |
| Predicate\<T> | T | boolean | 确定类型为T的对象是否满足某约束，并返回 boolean 值。包含方法 boolean test(T t); |

- 消费型接口

`void accept(T t);`

```java
public void consumerDemo(Integer value, Consumer<Integer> consumer) {
    consumer.accept(value);
}   
```

- 供给型接口

`T get();`

```java
public List<Integer> supplierDemo(int num, Supplier<Integer> supplier) {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < num; i++) {
        Integer n = supplier.get();
        list.add(n);
    }
    return list;
}
```

- 函数型接口

`R apply(T t);`

```java
public String functionDemo(String str, Function<String, String> function) {
    return function.apply(str);
}
```

- 断言型接口

`boolean test(T t);`

```java
public List<String> predicateDemo(List<String> list, Predicate<String> predicate) {
    List<String> newList = new ArrayList<>();
    for (String s : list) {
        if (predicate.test(s)) {
            newList.add(s);
        }
    }
    return newList;
}
```

### 2.2 自定义函数式接口

我们可以在任意函数式接口上使用 `@FunctionalInterface` 注解， 这样做可以检查它是否是一个函数式接口，同时 `javadoc` 也会包含一条声明，说明这个接口是一个函数式接口。

- `SelfFunctionalInterface.java`

```java
@FunctionalInterface
public interface SelfFunctionalInterface<T> {

    T getValue(T t);
}
```

- 自定义函数式接口

```java
public String selfFunctionalInterface(SelfFunctionalInterface<String> selfFunctionalInterface, String str) {
    return selfFunctionalInterface.getValue(str);
}
```

### 2.3 函数接口测试

```java
@Test
public void functionDemo() {
    // 修改参数
    consumerDemo(3, s -> logger.info("result:[{}]",s * 3));

    // 生成10个以内的随机数
    List<Integer> numList = supplierDemo(10, () -> (int)(100 * Math.random()));
    logger.info("result:[{}]",numList);

    // 处理字符串
    String str1 = functionDemo("Hello!风尘博客", s -> s.substring(6));
    logger.info("result:[{}]",str1);
    String str2 = functionDemo("vanDusty", s -> s.toUpperCase());
    logger.info("result:[{}]",str2);

    // 将满足条件的字符串放入集合
    List<String> list = Arrays.asList("hello", "van", "function", "predicate");
    List<String> newList = predicateDemo(list, s -> s.length() > 5);
    logger.info("result:[{}]",newList);

    // 字符串转大写
    String newStr = selfFunctionalInterface((str) -> str.toUpperCase(), "abc");
    logger.info("result:[{}]",newStr);
}
```

## 三、方法引用和构造器引用

### 3.1 方法引用

方法引用是指通过方法的名字来指向一个方法。

#### 3.1.1 方法引用使用的前提条件是什么呢？

1. 方法引用所引用的方法的参数列表必须要和函数式接口中抽象方法的参数列表相同（完全一致）；
1. 方法引用所引用的方法的的返回值必须要和函数式接口中抽象方法的返回值相同（完全一致）。

#### 3.1.2 方法引用三种格式

- 实例对象名::实例方法名

```java
@Test
public void instanceMethod() {
    UserDomain user = new UserDomain(1L, "Van");

    Supplier<String> sup = () -> user.getUserName();
    logger.info("result:[{}]",sup.get());

    // 等价于
    Supplier<String> supplier = user::getUserName;
    logger.info("result:[{}]",supplier.get());
}
```

- 类名::静态方法名

```java
@Test
public void staticMethod() {
    Comparator<Integer> com = (x, y) -> Integer.compare(x, y);
    logger.info("result:[{}]",com.compare(3, 9));

    // 等价于
    Comparator<Integer> com2 = Integer::compare;
    logger.info("result:[{}]",com2.compare(3, 9));
}
```

- 类名::实例方法名

```java
@Test
public void instanceMethodObject() {
    UserDomain user = new UserDomain(1L, "Van");

    Function<UserDomain, String> fun = (e) -> e.getUserName();
    logger.info("result:[{}]",fun.apply(user));

    // 等价于
    Function<UserDomain, String> fun2 = UserDomain::getUserName;
    logger.info("result:[{}]",fun2.apply(user));
}
```

### 3.2 构造器引用

1. 前提：构造器参数列表要与接口中抽象方法的参数列表一致！
1. 语法格式：`类名 :: new`

- 构造器引用

```java
@Test
public void object() {
    // UserDomain 中必须有一个 UserDomain(String userName) 的构造器,下同
    Function<String, UserDomain> fun = (n) -> new UserDomain(n);
    fun.apply("Van");

    // 等价于
    Function<String, UserDomain> function = UserDomain::new;
    function.apply("Van");

    // 带两个参数的构造器引用就要用BiFunction，多个参数的话，还可以自定义一个这样的函数式接口
    BiConsumer<Long, String> biConsumer = UserDomain::new;
    biConsumer.accept(1L, "Van");
}
```

- 数组引用

```java
@Test
public void array() {
    //传统Lambda实现
    Function<Integer, int[]> function = (i) -> new int[i];
    int[] apply = function.apply(10);
    logger.info("result:[{}]", apply.length);

    //数组类型引用实现
    function = int[]::new;
    apply = function.apply(100);
    logger.info("result:[{}]", apply.length);
}
```

## 四、 总结

[Github 示例代码](https://github.com/vanDusty/JDK/tree/master/JDK-Lambda/src/test/java/cn/van/jdk/lambda)

`Lambda`表达式是`Java`对于函数式编程的温和转变，面向对象编程和函数式编程不是互相对立的，结合使用能够更加有效地帮助我们管理程序的复杂性。

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
