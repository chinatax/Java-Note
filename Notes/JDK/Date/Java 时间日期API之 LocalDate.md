# Java 时间日期API之 LocalDate

## 一、背景

`jdk 1.8` 之前， `Java` 时间使用`java.util.Date` 和 `java.util.Calendar` 类。 

```java
Date today = new Date();
System.out.println(today);
	
 // 转为字符串
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String todayStr = sdf.format(today);
System.out.println(todayStr);
```

`Date` 的几个问题：

1. 如果不格式化，`Date`打印出的日期可读性差；
1. 可以使用 `SimpleDateFormat` 对时间进行格式化，但 `SimpleDateFormat` 是线程不安全的（阿里巴巴开发手册中禁用`static`修饰`SimpleDateFormat`）；
1. `Date`对时间处理比较麻烦，比如想获取某年、某月、某星期，以及 `n` 天以后的时间，如果用`Date`来处理的话真是太难了，并且 `Date` 类的 `getYear()`、`getMonth()` 这些方法都被弃用了；

## 二、`JDK 1.8` 新的日期时间类型

> `Java8`引入的新的一系列`API`，对时间日期的处理提供了更好的支持，清楚的定义了时间日期的一些概念，比如说，瞬时时间（`Instant`）,持续时间（`duration`），日期（`date`），时间（`time`），时区（`time-zone`）以及时间段（`Period`）。

1. `LocalDate`：不包含时间的日期，比如`2019-10-14`。可以用来存储生日，周年纪念日，入职日期等。
1. `LocalTime`：与`LocalDate`想对照，它是不包含日期的时间。
1. `LocalDateTime`：包含了日期及时间，没有偏移信息（时区）。
1. `ZonedDateTime`：包含时区的完整的日期时间，偏移量是以`UTC`/格林威治时间为基准的。
1. `Instant`：时间戳，与`System.currentTimeMillis()`类似。
1. `Duration`：表示一个时间段。
1. `Period`：用来表示以年月日来衡量一个时间段。
1. `DateTimeFormatter`：新的日期解析格式化类。


### 2.1 `LocalDate`

`LocalDate`类内只包含日期，不包含具体时间。只需要表示日期而不包含时间，就可以使用它。

```java
@Test
public void localDate() {
    //获取当前年月日
    LocalDate today = LocalDate.now();
    logger.info("当前年月日：[{}]",today);

    // 获取年的两种方式
    int thisYear = today.getYear();
    int thisYearAnother = today.get(ChronoField.YEAR);
    logger.info("今年是：[{}]年",thisYear);
    logger.info("今年是：[{}]年",thisYearAnother);

    // 获取月
    Month thisMonth = today.getMonth();
    logger.info("这个月是：[{}]月",thisMonth.toString());
    // 这是今年的第几个月(两种写法)
    int monthOfYear = today.getMonthValue();
    // int monthOfYear = today.get(ChronoField.MONTH_OF_YEAR);
    logger.info("这个月是今年的第[{}]个月",monthOfYear);
    // 月份的天数
    int length = today.lengthOfMonth();
    logger.info("这个月有[{}]天",length);

    // 获取日的两种方式
    int thisDay = today.getDayOfMonth();
    int thisDayAnother = today.get(ChronoField.DAY_OF_MONTH);
    logger.info("今天是这个月的第[{}]天",thisDay);
    logger.info("今天是这个月的第[{}]天",thisDayAnother);

    // 获取星期
    DayOfWeek thisDayOfWeek = today.getDayOfWeek();
    logger.info("今天是[{}]",thisDayOfWeek.toString());
    // 今天是这周的第几天
    int dayOfWeek = today.get(ChronoField.DAY_OF_WEEK);
    logger.info("今天是这周的第[{}]天",dayOfWeek);

    // 是否为闰年
    boolean leapYear = today.isLeapYear();
    logger.info("今年是闰年：[{}]",leapYear);

    //构造指定的年月日
    LocalDate anotherDay = LocalDate.of(2008, 8, 8);
    logger.info("指定年月日：[{}]",anotherDay);
}
```

### 2.2 `LocalTime`

`LocalTime`只会获取时间，不获取日期。`LocalTime`和`LocalDate`类似，区别在于`LocalDate`不包含具体时间，而`LocalTime`不包含具体日期。


```java
@Test
public void localTime() {
    // 获取当前时间
    LocalTime nowTime = LocalTime.now();
    logger.info("当前时间：[{}]",nowTime);

    //获取小时的两种方式
    int hour = nowTime.getHour();
    int thisHour = nowTime.get(ChronoField.HOUR_OF_DAY);
    logger.info("当前时：[{}]",hour);
    logger.info("当前时：[{}]",thisHour);

    //获取分的两种方式
    int minute = nowTime.getMinute();
    int thisMinute = nowTime.get(ChronoField.MINUTE_OF_HOUR);
    logger.info("当前分：[{}]",minute);
    logger.info("当前分：[{}]",thisMinute);

    //获取秒的两种方式
    int second = nowTime.getSecond();
    int thisSecond = nowTime.get(ChronoField.SECOND_OF_MINUTE);
    logger.info("当前秒：[{}]",second);
    logger.info("当前秒：[{}]",thisSecond);

    // 构造指定时间(最多可到纳秒)
    LocalTime anotherTime = LocalTime.of(20, 8, 8);
    logger.info("构造指定时间：[{}]",anotherTime);
}
```

### 2.3 `LocalDateTime`

`LocalDateTime`表示日期和时间组合。可以通过of()方法直接创建，也可以调用`LocalDate`的`atTime()`方法或`LocalTime`的`atDate()`方法将`LocalDate`或`LocalTime`合并成一个`LocalDateTime`。

```java
@Test
public void localDateTime() {
    // 当前日期和时间
    LocalDateTime today = LocalDateTime.now();
    logger.info("现在是：[{}]",today);

    // 创建指定日期和时间
    LocalDateTime anotherDay = LocalDateTime.of(2008, Month.AUGUST, 8, 8, 8, 8);
    logger.info("创建的指定时间是：[{}]",anotherDay);

    // 拼接日期和时间
    // 使用当前日期，指定时间生成的 LocalDateTime
    LocalDateTime thisTime = LocalTime.now().atDate(LocalDate.of(2008, 8, 8));
    logger.info("拼接的日期是：[{}]",thisTime);
    // 使用当前日期，指定时间生成的 LocalDateTime
    LocalDateTime thisDay = LocalDate.now().atTime(LocalTime.of(12, 24, 12));
    logger.info("拼接的日期是：[{}]",thisDay);
    // 指定日期和时间生成 LocalDateTime
    LocalDateTime thisDayAndTime = LocalDateTime.of(LocalDate.of(2008, 8, 8), LocalTime.of(12, 24, 12));
    logger.info("拼接的日期是：[{}]",thisDayAndTime);

    // 获取LocalDate
    LocalDate todayDate = today.toLocalDate();
    logger.info("今天日期是：[{}]",todayDate);

    // 获取LocalTime
    LocalTime todayTime = today.toLocalTime();
    logger.info("现在时间是：[{}]",todayTime);
}
```

### 2.4 `Instant`

`Instant`用于一个获取时间戳，与`System.currentTimeMillis()`类似，但`Instant`可以精确到纳秒。


```java
@Test
public void instantDemo() {
    // 创建Instant对象
    Instant instant = Instant.now();
    // 通过ofEpochSecond方法创建(第一个参数表示秒，第二个参数表示纳秒)
    Instant another = Instant.ofEpochSecond(365 * 24 * 60, 100);

    // 获取到秒数
    long currentSecond = instant.getEpochSecond();
    logger.info("获取到秒数：[{}]", currentSecond);

    // 获取到毫秒数
    long currentMilli = instant.toEpochMilli();
    logger.info("获取到毫秒数：[{}]", currentMilli);
}
```

### 2.5 `Duration`

`Duration`的内部实现与`Instant`类似，但`Duration`表示时间段，通过`between`方法创建，还可以通过`of()`方法创建。


```java
@Test
public void durationDemo() {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = LocalDateTime.now().plusDays(1);
    // 通过between()方法创建
    Duration duration = Duration.between(from, to);
    logger.info("result:[{}]", duration);
    // 通过of()方法创建,该方法参数为时间段长度和时间单位。
    // 7天
    Duration duration1 = Duration.of(7, ChronoUnit.DAYS);
    logger.info("result:[{}]", duration1);

    // 60秒
    Duration duration2 = Duration.of(60, ChronoUnit.SECONDS);
    logger.info("result:[{}]", duration2);
}
```

### 2.5 `Period`

`Period`与`Duration`类似，获取一个时间段，只不过单位为年月日，也可以通过`of`方法和`between`方法创建，`between`方法接收的参数为`LocalDate`。

```java
@Test
public void periodDemo() {
    // 通过of方法
    Period period = Period.of(2012, 12, 24);
    logger.info("result:[{}]", period);

    // 通过between方法
    Period period1 = Period.between(LocalDate.now(), LocalDate.of(2020,12,31));
    logger.info("result:[{}]", period1);
}
```

## 三、时间操作

### 3.1 时间比较

> `isBefore()`和`isAfter()`判断给定的时间或日期是在另一个时间/日期之前还是之后。
> 以`LocalDate`为例，`LocalDateTime`/`LocalTime `同理。

```java
@Test
public void compare() {
    LocalDate thisDay = LocalDate.of(2008, 8, 8);
    LocalDate otherDay = LocalDate.of(2018, 8, 8);
    // 晚于
    boolean isAfter = thisDay.isAfter(otherDay);
    logger.info("result:{}", isAfter);
    // 早于
    boolean isBefore = thisDay.isBefore(otherDay);
    logger.info("result:{}", isBefore);
}
```

### 3.2 增加/减少年数、月数、天数

> 以`LocalDateTime`为例，`LocalDate`/`LocalTime`同理。

```java
@Test
public void plusAndMinus() {
    // 增加
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime nextYearDay = today.plusYears(1);
    logger.info("下一年的今天是：[{}]", nextYearDay);
    LocalDateTime nextMonthDay = today.plus(1, ChronoUnit.MONTHS);
    logger.info("下一个月的今天是：[{}]", nextMonthDay);

    //减少
    LocalDateTime lastMonthDay = today.minusMonths(1);
    LocalDateTime lastYearDay = today.minus(1, ChronoUnit.YEARS);
    logger.info("一个月前是：[{}]", lastMonthDay);
    logger.info("一年前是：[{}]", lastYearDay);
}
```

### 3.3 时间修改

> 通过`with()`修改时间

```java
@Test
public void edit() {
    LocalDateTime today = LocalDateTime.now();
    // 修改年为2012年
    LocalDateTime thisYearDay = today.withYear(2012);
    logger.info("修改年后的时间为：[{}]", thisYearDay);
    // 修改为12月
    LocalDateTime thisMonthDay = today.with(ChronoField.MONTH_OF_YEAR, 12);
    logger.info("修改月后的时间为：[{}]", thisMonthDay);
}
```

### 3.4 时间计算

> 通过 `TemporalAdjusters` 的静态方法 和 `Duration` 计算时间

```java
@Test
public void compute() {
    // TemporalAdjusters 的静态方法
    LocalDate today = LocalDate.now();
    // 获取今年的第一天
    LocalDate date = today.with(firstDayOfYear());
    logger.info("今年的第一天是：[{}]", date);

    // Duration 计算
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = LocalDateTime.now().plusMonths(1);
    Duration duration = Duration.between(from, to);

    // 区间统计换算
    // 总天数
    long days = duration.toDays();
    logger.info("相隔:[{}]天", days);
    // 小时数
    long hours = duration.toHours();
    logger.info("相隔:[{}]小时", hours);
    // 分钟数
    long minutes = duration.toMinutes();
    logger.info("相隔:[{}]分钟", minutes);
}
```

- `TemporalAdjusters`的更多方法

| 方法名称 | 描述 | 
| -- | -- |
| `dayOfWeekInMonth()` | 返回同一个月中每周的第几天 |
| `firstDayOfMonth()` | 返回当月的第一天 |
| `firstDayOfNextMonth()` | 返回下月的第一天 |
| `firstDayOfNextYear()` | 返回下一年的第一天 |
| `firstDayOfYear()` | 返回本年的第一天 |
| `firstInMonth()` | 返回同一个月中第一个星期几 |
| `lastDayOfMonth()` | 返回当月的最后一天 |
| `lastDayOfNextMonth()` | 返回下月的最后一天 |
| `lastDayOfNextYear()` | 返回下一年的最后一天 |
| `lastDayOfYear()` | 返回本年的最后一天 |
| `lastInMonth()` | 返回同一个月中最后一个星期几 |
| `next()` / `previous()` | 返回后一个/前一个给定的星期几 |
| `nextOrSame()` / `previousOrSame()` | 返回后一个/前一个给定的星期几，如果这个值满足条件，直接返回 |

## 四、时间日期格式化

### 4.1 格式化时间

> `DateTimeFormatter`默认提供了多种格式化方式，如果默认提供的不能满足要求，可以通过`DateTimeFormatter`的`ofPattern`方法创建自定义格式化方式。

```java
@Test
public void format() {
    LocalDate today = LocalDate.now();
    // 两种默认格式化时间方式
    String todayStr1 = today.format(DateTimeFormatter.BASIC_ISO_DATE);
    String todayStr2 = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
    logger.info("格式化时间：[{}]", todayStr1);
    logger.info("格式化时间：[{}]", todayStr2);
    //自定义格式化
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String todayStr3 = today.format(dateTimeFormatter);
    logger.info("自定义格式化时间：[{}]", todayStr3);
}
```

### 4.2 解析时间

> 4.1 中以何种方式格式化，这里需以同样方式解析。

```java
@Test
public void parse() {
    LocalDate date1 = LocalDate.parse("20080808", DateTimeFormatter.BASIC_ISO_DATE);
    LocalDate date2 = LocalDate.parse("2008-08-08", DateTimeFormatter.ISO_LOCAL_DATE);
    logger.info("result:[{}]", date1);
    logger.info("result:[{}]", date2);
}
```

## 五、周期性事件

### 5.1 `MonthDay` 

> 每年都会发生的事件(忽略年份),例如生日

```java
LocalDate createTime = LocalDate.of(2018, 2, 14);
MonthDay birthday = MonthDay.of(createTime.getMonthValue(), createTime.getDayOfMonth());

MonthDay todayMonthDay = MonthDay.from(LocalDate.now()); // 获取今天的月日
if(todayMonthDay.equals(birthday)){
    logger.info("Many Many happy today!!");
}else{
    logger.info("Sorry, today is not your birthday");
}
```

### 5.2 `YearMonth`

> 表示信用卡到期日等等周期性时间，这个类还可以返回当月的天数，在判断`2`月有`28`天还是`29`天时非常有用。

```java
YearMonth currentYearMonth = YearMonth.now();
    logger.info("Days in month year [{}]:[{}]", currentYearMonth, currentYearMonth.lengthOfMonth());
    YearMonth creditCardExpiry = YearMonth.of(2018, Month.FEBRUARY); // 指定时间
    logger.info("Your credit card expires on [{}]", creditCardExpiry);
```

## 六、总结

> `LocalDate` 相较于`Date`

1. `Instant` 的精确度更高，可以精确到纳秒级;
1. `Duration` 可以便捷得到时间段内的天数、小时数等;
1. `LocalDateTime` 能够快速地获取年、月、日、下一月等;
1. `TemporalAdjusters` 类中包含许多常用的静态方法，避免自己编写工具类;
1. 与`Date`的格式化方式`SimpleDateFormat`相比，`DateTimeFormatter`是线程安全的。


[Github 示例代码](https://github.com/vanDusty/jdk/blob/master/JDK-Date/src/test/java/cn/van/jdk/date/LocalDateTest.java)

更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
