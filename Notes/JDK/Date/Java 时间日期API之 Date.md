# Java 时间日期 API 之 Date

> 虽然 `JDK 1.8` 已经出来好多年了，`LocalDate`、`LocalDateTime`、`LocalTime` 不知道要比 `Date` 好用多少倍，但是没办法，很多老项目还是用 `Date`。

本文分享博主日常用的比较多的几个方法，仅供参考。

### 1、计算两个时间的间隔天数

```java
public static int calcIntervalDays(Date date1, Date date2) {
    if (date2.after(date1)) {
        return Long.valueOf((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24)).intValue();
    } else if (date2.before(date1)) {
        return Long.valueOf((date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24)).intValue();
    } else {
        return 0;
    }
}
```

根据此方法，我们可以**判断两个时间是否是同一天**

```java
public static boolean isSameDay(Date date1, Date date2) {
    return calcIntervalDays(date1, date2) == 0;
}
```

### 2、计算两个时间的间隔小时，只会整除

```java
public static int calcIntervalOurs(Date date1, Date date2) {
    if (date2.after(date1)) {
        return Long.valueOf((date2.getTime() - date1.getTime()) / (1000 * 60 * 60)).intValue();
    } else if (date2.before(date1)) {
        return Long.valueOf((date1.getTime() - date2.getTime()) / (1000 * 60 * 60)).intValue();
    } else {
        return 0;
    }
}
```

### 3、计算两个时间的间隔分钟，只会整除

```java
public static int calcIntervalDays(Date date1, Date date2) {
    if (date2.after(date1)) {
        return Long.valueOf((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24)).intValue();
    } else if (date2.before(date1)) {
        return Long.valueOf((date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24)).intValue();
    } else {
        return 0;
    }
}
```

### 4、返回日期对应的是星期几

```java
public static int dayOfWeek(Date date) {
    Calendar ca = Calendar.getInstance();
    ca.setTime(date);
    int dayOfWeek;
    if (ca.get(Calendar.DAY_OF_WEEK) == 1) {
        dayOfWeek = 7;
    } else {
        dayOfWeek = ca.get(Calendar.DAY_OF_WEEK) - 1;
    }
    return dayOfWeek;
}
```

### 5、获取今天当前的总分钟数，如今天16:54，则返回1014

```java
public static int getTodayMinutes() {
    Calendar ca = Calendar.getInstance();
    int hours = ca.get(Calendar.HOUR_OF_DAY);
    int minutes = ca.get(Calendar.MINUTE);
    return hours * 60 + minutes;
}
```

### 6、将 `String` 转成 `Date`将 `Date`转成`String`

```java
public static Date stringParseDate(String dateStr) {
    SimpleDateFormat format = null;
    if (dateStr.contains("/")) {
        if (dateStr.contains(":") && dateStr.contains(" ")) {
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        } else {
            format = new SimpleDateFormat("yyyy/MM/dd");
        }
    } else if (dateStr.contains("-")) {
        if (dateStr.contains(":") && dateStr.contains(" ")) {
            format = new SimpleDateFormat(YYYYMMDDHHMMSS);
        } else {
            format = new SimpleDateFormat(YYYYMMDD);
        }
    } else if (dateStr.contains("年") && dateStr.contains("月") && dateStr.contains("日") && dateStr.contains("时") && dateStr.contains("分") && dateStr.contains("秒")) {
        format = new SimpleDateFormat(YYYYMMDDHHMMSS_CHINESE);
    } else if (dateStr.contains("年") && dateStr.contains("月") && dateStr.contains("日")) {
        format = new SimpleDateFormat(YYYYMMDD_CHINESE);
    } else if (!dateStr.contains("年") && dateStr.contains("月") && dateStr.contains("日")) {
        format = new SimpleDateFormat(MMDD_CHINESE);
    }
    if (format == null) {
        return null;
    }
    format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    try {
        return format.parse(dateStr);
    } catch (ParseException e) {
        e.printStackTrace();
        return null;
    }
}
```

```java
public static String dateToString(Date date, String format) {
    DateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    try {
        return dateFormat.format(date);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

### 7、获取指定日期偏移指定时间后的时间


```java
public static Date offsiteDate(Date date, int calendarField, int offsite){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(calendarField, offsite);
    return cal.getTime();
}
```

- 参数说明：

1. `date`：基准日期
1. `calendarField`：偏移的粒度大小（小时、天、月等）使用`Calendar`中的常数
1. `offsite`：偏移量，正数为向后偏移，负数为向前偏移

- 例如常用的：

```java
/**
 * 获取昨天
 * @return
 */
public static Date getYesterday() {
    return offsiteDate(new Date(), Calendar.DAY_OF_YEAR, -1);
}

/**
 * 获取上周今天
 * @return
 */
public static Date getLastWeek() {
    return offsiteDate(new Date(), Calendar.WEEK_OF_YEAR, -1);
}

/**
 * 获取上个月今天
 * @return
 */
public static Date getLastMouth() {
    return offsiteDate(new Date(), Calendar.MONTH, -1);
}
```
 
 
### 8、获取某月的最后一天

```java
public static int getMonthLastDay(int year, int month) {
    Calendar a = Calendar.getInstance();
    a.set(Calendar.YEAR, year);
    a.set(Calendar.MONTH, month - 1);
    //把日期设置为当月第一天
    a.set(Calendar.DATE, 1);
    //日期回滚一天，也就是最后一天
    a.roll(Calendar.DATE, -1);
    int maxDate = a.get(Calendar.DATE);
    return maxDate;
}
```

### 9、判断是否是闰年

```java
public static boolean isLeap(int year) {
    return ((year % 100 == 0) && year % 400 == 0) || ((year % 100 != 0) && year % 4 == 0);
}
```

### 10、由出生日期获得年龄

```java
public static int getAge(Date birthDay) {
    Calendar cal = Calendar.getInstance();
    if (cal.before(birthDay)) {
        throw new IllegalArgumentException(
                "The birthDay is before Now.It's unbelievable!");
    }
    int yearNow = cal.get(Calendar.YEAR);
    int monthNow = cal.get(Calendar.MONTH);
    int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
    cal.setTime(birthDay);

    int yearBirth = cal.get(Calendar.YEAR);
    int monthBirth = cal.get(Calendar.MONTH);
    int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

    int age = yearNow - yearBirth;

    if (monthNow <= monthBirth) {
        if (monthNow == monthBirth) {
            if (dayOfMonthNow < dayOfMonthBirth){
                age--;
            }
        }else{
            age--;
        }
    }
    return age;
}
```

博主将这些方法封装成一个工具类，详见[DateUtil.java](https://github.com/vanDusty/jdk/blob/master/JDK-Date/src/main/java/cn/van/jdk/date/DateUtil.java)

更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
