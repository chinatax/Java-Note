/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: DataAndTimeUtils
 * Author:   zhangfan
 * Date:     2019-02-25 15:49
 * Description: Jdk 1.8 时间和日期Api
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package cn.van.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Jdk 1.8 时间和日期Api〉
 *
 * @author zhangfan
 * @create 2019-02-25
 * @since 1.0.0
 */
public class DataAndTimeUtils {

    public static void main(String[] args) {

        getNow();
        getFormatTime();
        formatTimeToString();
        operateTime();
        periodicEvent();
        equalTime();
    }


    /**
     * 获取日期和时间
     */
    public static void getNow() {
        //当前日期
        LocalDate today = LocalDate.now();
        System.out.println("今天是："+ today);
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        // 当前时间
        LocalTime now = LocalTime.now();
        System.out.println("年:"+ year + ",月:" + month + ",日:" + day + ",时间："+ now);

        // 创建指定日期
        LocalDate time = LocalDate.of(2008,9,27);
        System.out.println(time);

        // 获取当前的时间戳
        Instant timestamp = Instant.now();
        System.out.println("What is value of this instant " + timestamp);
        // 时间戳信息里同时包含了日期和时间，这和java.util.Date很像。实际上Instant类确实等同于 Java 8之前的Date类，你可以使用Date类和Instant类各自的转换方法互相转换，例如：Date.from(Instant) 将Instant转换成java.util.Date，Date.toInstant()则是将Date类转换成Instant类。

        // 判断是否是闰年
        if (today.isLeapYear()) {
            System.out.println("今年是闰年！");
        }
    }
    // 字符串转成时间
    public static void getFormatTime() {
        // 内置的格式化工具
        String createTime = "20140116";
        LocalDate formatted = LocalDate.parse(createTime,
                DateTimeFormatter.BASIC_ISO_DATE);
        System.out.printf("Date generated from String %s is %s %n",
                createTime, formatted);

        // 指定格式化工具
        String goodFriday = "2018-12-11";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate holiday = LocalDate.parse(goodFriday, formatter);
            System.out.printf("Successfully parsed String %s, date is %s%n", goodFriday, holiday);
        } catch (DateTimeParseException ex) {
            System.out.printf("%s is not parsable!%n", goodFriday);
            ex.printStackTrace();
        }

    }
    // 时间转成字符串
    public static void formatTimeToString() {
        LocalDateTime arrivalDate  = LocalDateTime.now();
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd yyyy  hh:mm a");
            String landing = arrivalDate.format(format);
            System.out.println("转成字符串成功，为:"+landing);
        } catch (DateTimeException ex) {
            System.out.printf("%s can't be formatted!%n", arrivalDate);
            ex.printStackTrace();
        }
    }

    /**
     * 创建指定时间/判断两个日期是否相等
     */
    public static void equalTime() {
        LocalDate time = LocalDate.of(1993,9,27);
        System.out.println(time);
        LocalDate now = LocalDate.now();
        if (now.equals(time)) {
            System.out.println("两个时间相等");
        }
        if (now.isAfter(time)) {
            System.out.println("当前日期晚于指定时间");
        }
        if (now.isBefore(time)) {
            System.out.println("当前时间早于指定时间");
        }
    }

    /**
     * 时间增减操作
     */
    public static void operateTime() {
        LocalTime now = LocalTime.now();
        // 两小时后的时间,分钟/秒钟同理
        LocalTime newTime = now.plusHours(2);
        System.out.println("两小时后的时间："+newTime);

        // 一周后的时间（ChronoUnit类声明了这些时间单位）
        LocalDate nextWeek = LocalDate.now().plus(1,ChronoUnit.WEEKS);
        System.out.println("一周后的日期："+nextWeek);

        // 获取一年前的日期
        LocalDate previousYear = LocalDate.now().minus(1, ChronoUnit.YEARS);
        System.out.println("一年前的日期：" + previousYear);
    }

    /**
     * 周期性事件
     */
    public static void periodicEvent() {

        // MonthDay 每年都会发生的事件(忽略年份),例如生日。
        LocalDate createTime = LocalDate.of(2018, 2, 14);
        MonthDay birthday = MonthDay.of(createTime.getMonthValue(), createTime.getDayOfMonth());

        MonthDay todayMonthDay = MonthDay.from(LocalDate.now()); // 获取今天的月日
        if(todayMonthDay.equals(birthday)){
            System.out.println("Many Many happy today!!");
        }else{
            System.out.println("Sorry, today is not your birthday");
        }

        // YearMonth,表示信用卡到期日等等周期性时间，这个类还可以返回当月的天数，在判断2月有28天还是29天时非常有用。
        YearMonth currentYearMonth = YearMonth.now();
        System.out.printf("Days in month year %s: %d%n", currentYearMonth, currentYearMonth.lengthOfMonth());
        YearMonth creditCardExpiry = YearMonth.of(2018, Month.FEBRUARY); // 指定时间
        System.out.printf("Your credit card expires on %s %n", creditCardExpiry);
    }



}