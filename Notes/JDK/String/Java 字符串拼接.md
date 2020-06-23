# Java 字符串拼接

## 一、字符串分割之`split()`

### 1.1 理所应当的方式

如下代码，基本我们都会写：

```java
String string = "风尘-博客";
String[] arr = string.split("-");
String part1 = arr[0];
System.out.println(part1);
String part2 = arr[1];
System.out.println(part2);
```

打印结果也没毛病。但是，如果你遇到过这种：

```java
String str = "风尘.博客";
String[] strArr = str.split(".");
String string1 = strArr[0];
System.out.println(string1);
String string2 = strArr[1];
System.out.println(string2);
```

再次运行，控制台就抛错：

```xml
java.lang.ArrayIndexOutOfBoundsException: 0
```

### 1.2 正确的操作方式

追根溯源，看下源码。

很明显，`String.split(String regex) `方法中的参数`regex`是正则表达式分隔符。

- 单个转义字符

>  `.` 、`$`、`|` 和 `*` 等转义字符，必须得加 `\\`或者使用 `Pattern.quote()`

```java
@Test
public void trueMethod() {
    String str = "风尘.博客";
    String[] strArr = str.split("\\.");
    // 这一种写法也行
//        String[] strArr = str.split(Pattern.quote("."));
    String string1 = strArr[0];
    System.out.println(string1);
    String string2 = strArr[1];
    System.out.println(string2);
}
```

- 多个分隔符，可以用` | `作为连字符。

```java
@Test
public void upMethod() {
    String str = "风尘.博客|你好";
    String[] strArr = str.split("\\.|\\|");
    String string1 = strArr[0];
    System.out.println(string1);
    String string2 = strArr[1];
    System.out.println(string2);
    String string3 = strArr[2];
    System.out.println(string3);
}
```

## 二、字符串拼接

字符串拼接是我们在`Java`代码中比较经常要做的事情，就是把多个字符串拼接到一起。

> 我们都知道，`String`是`Java`中一个不可变的类，所以他一旦被实例化就无法被修改。

### 2.1 使用`+`拼接字符串

虽然字符串是不可变的，但是还是可以通过新建字符串的方式来进行字符串的拼接。

```java
@Test
public void plus() {
    String str = "风尘";
    String weChat = "博客";
    String string = str + weChat;
    logger.info(string);
}
```

> 如果拼接的字符串是 `null`，`+`号操作符会当做是一个`null`字符串来处理。

### 2.2 `concat`方式

当两个量都为`String`类型且值不为`null`时，可以用`concat`方式。

```java
@Test
public void concat() {
    String str = "风尘";
    String weChat = "博客";
    String string = str.concat(weChat);
    logger.info(string);
    // java.lang.NullPointerException
    str.concat(null);
}
```

> 如果拼接的字符串是 `null`，`concat` 时候就会抛出 `NullPointerException`。

### 2.3 `StringBuffer.append()`

当需要拼接至少三个量的时候，可以考虑使用`StringBuffer.append()`以避免临时字符串的产生。

```java
@Test
public void stringBuffer() {
    StringBuffer str = new StringBuffer("欢迎关注");
    StringBuffer string = str.append(":").append("风尘博客");
    logger.info(string.toString());
}
```

> 当`a`,`b`,`c`拼接起来会很长时，可以给在构造器中传入一个合适的预估容量以减少因扩展缓冲空间而带来的性能开销。

```java
StringBuffer buf = new StringBuffer(a.length() + b.length() + c.length());
```

### 2.4 `StringBuilder.append()`

其用法和`StringBuffer`类似,只不过 `StringBuffer` 是线程安全的。

```java
@Test
public void stringBuilder() {
    StringBuilder str = new StringBuilder("欢迎关注");
    StringBuilder string = str.append(":").append("风尘博客");
    logger.info(string.toString());
}
```

### 2.5 `StringJoiner.add()`

`StringJoiner`其实是通过`StringBuilder`实现的，所以他的性能和`StringBuilder`差不多，他也是非线程安全的。

```java
@Test
public void stringJoiner() {
    StringJoiner str = new StringJoiner(":");
    str.add("欢迎关注").add("风尘博客");
    logger.info(str.toString());
}
```

当我们使用`StringJoiner(CharSequence delimiter)`初始化一个`StringJoiner`的时候，这个`delimiter`其实是分隔符，并不是可变字符串的初始值。

- 集合进行字符串拼接

```java
@Test
public void listSplice() {
    List<String> list = new ArrayList<>();
    list.add("欢迎关注");
    list.add("风尘博客");
    String str = list.stream().collect(Collectors.joining(":"));
    logger.info(str);
}
```

### 2.6 `String` 类的 `join` 方法

> 第一个参数为字符串连接符。

```java
@Test
public void join() {
    // 第一个参数为字符串连接符。
    String str = String.join(":","欢迎关注","风尘博客");
    logger.info(str);
}
```

该方法是通过`StringJoiner`实现的。

### 2.7 总结

1. 如果只是简单的字符串拼接，考虑直接使用`+`即可；
1. 在不考虑线程安全和同步的情况下，为了获得最高的性能，我们应尽量使用`StringBuilder`，反之使用`StringBuffer `；
1. 如果是通过一个集合（如`List`）进行字符串拼接，则考虑使用`StringJoiner`。

[Github 示例代码](https://github.com/vanDusty/jdk/blob/master/JDK-String/src/test/java/cn/van/jdk/string/splice/StringSpliceTest.java)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
