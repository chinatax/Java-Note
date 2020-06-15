# Java 科学运算之 BigDecimal

## 一、`BigDecimal`的产生背景

首先我们先来看如下代码示例：

```java
@Test
public void countDemo() {
    logger.info("result:{}", 0.06 + 0.01);
    logger.info("result:{}", 1.0 - 0.42);
    logger.info("result:{}", 4.015 * 100);
    logger.info("result:{}", 303.1 / 1000);
}
```

结果如下

```xml
result:0.06999999999999999
result:0.5800000000000001
result:401.49999999999994
result:0.30310000000000004
```

问题在哪里呢？原因在于**我们的计算机是二进制的，浮点数是没有办法用二进制进行精确表示**。


`Java`的`float`只能用来进行科学计算或工程计算，在大多数的商业计算中，一般采用`java.math.BigDecimal`类来进行精确计算。


## 二、正确运用`BigDecimal`

### 2.1 `BigDecimal`常量

`BigDecimal`定义了几个常用的值，`0`、`1`、`10`，静态的，可以通过类名直接引用如：`BigDecimal.ZERO`。

```java
/**
 * The value 0, with a scale of 0.
 *
 * @since  1.5
 */
public static final BigDecimal ZERO =
    zeroThroughTen[0];

/**
 * The value 1, with a scale of 0.
 *
 * @since  1.5
 */
public static final BigDecimal ONE =
    zeroThroughTen[1];

/**
 * The value 10, with a scale of 0.
 *
 * @since  1.5
 */
public static final BigDecimal TEN =
    zeroThroughTen[10];
```


### 2.2 构造方法

`BigDecimal` 有`4`个常用构造方法

1. `new BigDecimal(int)` 创建一个具有参数所指定整数值的对象；
1. `new BigDecimal(double)` 创建一个具有参数所指定双精度值的对象；
1. `new BigDecimal(long)` 创建一个具有参数所指定长整数值的对象；
1. `new BigDecimal(String)` 创建一个具有参数所指定以字符串表示的数值的对象。


### 2.3 `BigDecimal`运算一般步骤

在使用`BigDecimal`类来进行计算的时候，主要三个步骤：

1. 用`float`或者`double`变量构建`BigDecimal`对象。
1. 通过调用`BigDecimal`的加，减，乘，除等相应的方法进行算术运算。
1. 把`BigDecimal`对象转换成`float`，`double`，`int`等类型。


## 三、常用方法详解

在一般开发过程中，我们数据库中存储的数据都是`float`和`double`类型的。我封装了一个工具类，该工具类提供加、减、乘、除、向上取值、向下取值等运算。

### 3.1 普通加减乘除

```java
/**
 * 默认除法运算精度
 */
private static final int DEF_DIV_SCALE = 10;

/**
 * 精确的加法运算。
 *
 * @param v1 被加数
 * @param v2 加数
 * @return 两个参数的和
 */
public static double add(Double v1, Double v2) {
    BigDecimal b1 = BigDecimal.valueOf(v1);
    BigDecimal b2 = BigDecimal.valueOf(v2);
    return b1.add(b2).doubleValue();
}

/**
 * 精确的减法运算。
 *
 * @param v1 被减数
 * @param v2 减数
 * @return 两个参数的差
 */
public static double sub(Double v1, Double v2) {
    BigDecimal b1 = BigDecimal.valueOf(v1);
    BigDecimal b2 = BigDecimal.valueOf(v2);
    return b1.subtract(b2).doubleValue();
}

/**
 * 精确的乘法运算。
 *
 * @param v1 被乘数
 * @param v2 乘数
 * @return 两个参数的积
 */
public static double mul(Double v1, Double v2) {
    BigDecimal b1 = BigDecimal.valueOf(v1);
    BigDecimal b2 = BigDecimal.valueOf(v2);
    return b1.multiply(b2).doubleValue();
}

/**
 * （相对）精确的除法运算，当发生除不尽的情况时，默认精确到小数点以后10位，以后的数字四舍五入。
 *
 * @param v1 被除数
 * @param v2 除数
 * @return 两个参数的商
 */
public static double div(Double v1, Double v2) {
    return div(v1, v2, DEF_DIV_SCALE, RoundingMode.HALF_UP);
}
``` 

### 3.2 按照精度四舍五入或者向上/向下取整

```java
/**
 * 得到计算结果后四舍五入
 *
 * @param val
 * @param scale 精度
 * @return 例如保留三位小数：0.646464 =》 0.646
 */
public static double roundHalfUp(double val, int scale) {
    BigDecimal dec = BigDecimal.valueOf(val);
    return dec.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
}

/**
 * 得到计算结果后向上取整
 *
 * @param val   val
 * @param scale 精度
 * @return 例如保留三位小数：0.646464 =》 0.647
 */
public static double roundUp(double val, int scale) {
    BigDecimal dec = BigDecimal.valueOf(val);
    return dec.setScale(scale, RoundingMode.UP).doubleValue();
}

/**
 * 得到计算结果后向下取整
 *
 * @param val   val
 * @param scale 精度
 * @return 例如保留三位小数：0.646464 =》 0.646
 */
public static double roundDown(double val, int scale) {
    BigDecimal dec = BigDecimal.valueOf(val);
    return dec.setScale(scale, RoundingMode.DOWN).doubleValue();
}

/**
 * 除法运算加上向上取整。
 *
 * @param v1    被除数
 * @param v2    除数
 * @param scale 精度
 * @return
 */
public static double divOfUp(Double v1, Double v2, int scale) {
    return div(v1, v2, scale, RoundingMode.UP);
}

/**
 * 除法运算加上向下取整。
 *
 * @param v1    被除数
 * @param v2    除数
 * @param scale 精度
 * @return
 */
public static double divOfDown(Double v1, Double v2, int scale) {
    return div(v1, v2, scale, RoundingMode.DOWN);
}

/**
 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由 scale 参数指定精度，roundingMode 指定取舍方式。
 *
 * @param v1           被除数
 * @param v2           除数
 * @param scale        表示表示需要精确到小数点以后几位。
 * @param roundingMode 指定取舍方式。
 * @return 两个参数的商
 */
private static double div(double v1, double v2, int scale, RoundingMode roundingMode) {
    //如果精确范围小于0，抛出异常信息
    if (scale < 0) {
        throw new IllegalArgumentException("The scale must be a positive integer or zero");
    }
    BigDecimal b1 = BigDecimal.valueOf(v1);
    BigDecimal b2 = BigDecimal.valueOf(v2);
    return b1.divide(b2, scale, roundingMode).doubleValue();
}
```

### 3.3 如果正好是除以或乘以整十

有时候，比如人民币的分/圆/万计算，我们正好是除以或者乘以百/万的计算，除了加法和减法，`BigDecimal` 提供直接移动小数点的方法。


```java
/**
 * 小数点向右移动指定位数
 *
 * @param val   被乘数
 * @param index 移动位数
 * @return 例如向右移动四位小数：1000.01 =》 1000010.0
 */
public static double movePointRight(Double val, int index) {
    BigDecimal value = BigDecimal.valueOf(val);
    return value.movePointRight(index).doubleValue();
}

/**
 * 小数点向左移动指定位数
 *
 * @param val   被除数
 * @param index 移动位数
 * @return 例如向左移动四位小数：1000.01 =》 1.00001
 */
public static double movePointLeft(Double val, int index) {
    BigDecimal value = BigDecimal.valueOf(val);
    return value.movePointLeft(index).doubleValue();
}
```

### 3.4 `BigDecimal` 大小比较

在比较两个`BigDecimal`的值是否相等时，必须使用`compareTo()`方法来比较，它根据两个值的大小分别返回`-1`、`1`和 `0`，分别表示小于、大于和等于。

```java
@Test
public void compareDecimal() {
    BigDecimal v1 = BigDecimal.valueOf(1.21);
    BigDecimal v2 = BigDecimal.valueOf(1.22);
    BigDecimal v3 = BigDecimal.valueOf(1.22);
    // -1:小于、1:大于、0:等于
    logger.info("result:{}", v1.compareTo(v2));
    logger.info("result:{}", v2.compareTo(v1));
    logger.info("result:{}", v2.compareTo(v3));
}
```

### 3.5`BigDecimal`精度也丢失

仔细的你一定发现了，在工具类中使用`BigDecimal`中，都是使用`BigDecimal` 的 `valueOf()`创建对象的，而`valueOf()` 的内部实现是

```java
public static BigDecimal valueOf(double val) {
    return new BigDecimal(Double.toString(val));
}
```
及构造器用的是`new BigDecimal(String)`，因为其他的如`new BigDecimal(int)`/`new BigDecimal(long)`/`new BigDecimal(double)`，还是会发生精度丢失的问题。

- 示例

```java
@Test
public void precisionLose() {
    BigDecimal v1 = new BigDecimal(1.01);
    BigDecimal v2 = new BigDecimal(1.02);
    BigDecimal v3 = new BigDecimal("1.01");
    BigDecimal v4 = new BigDecimal("1.02");
    // 2.0300000000000000266453525910037569701671600341796875
    logger.info("result:{}", v1.add(v2));
    // 2.03
    logger.info("result:{}", v3.add(v4));
}
```

## 四、总结

1. `BigDecimal`用于表示精确的小数，常用于财务计算；

1. 比较`BigDecimal`的值是否相等，必须使用`compareTo()`而不能使用`equals()`。


#### [【Github 示例代码】](https://github.com/vanDusty/jdk/blob/master/JDK-Tool/src/main/java/cn/van/jdk/tool/math/BigDecimalUtil.java)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
