/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: DataToolsTest
 * Author:   zhangfan
 * Date:     2019-03-01 17:48
 * Description: 时间日期工具测试
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package cn.van;

import cn.van.Tools.DateTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 〈一句话功能简述〉<br> 
 * 〈时间日期工具测试〉
 *
 * @author zhangfan
 * @create 2019-03-01
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataToolsTest {

    @Test
    public void test() throws ParseException {


        //日期格式类型
        String pattern = "yyyy-MM-dd hh:mm:ss";
        Date toDay = new Date();
        // 日期转字符串
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("Date转字符串:" + DateTools.dateToString(toDay,pattern));
        pattern = "MMM dd yyyy  hh:mm a";
        System.out.println("LocalDateTime转成字符串" + DateTools.localDateTimeToString(localDateTime,pattern));

        // 字符串转时间

        String time = "2019-03-01 06:06:49";
        pattern = "yyyy-MM-dd HH:mm:ss";
        System.out.println("String转Date:" + DateTools.stringToDate(time,pattern));
        System.out.printf("String转DateLocalTime:" + DateTools.stringToLocalDateTime(time,pattern));
        pattern = "yyyy-MM-dd";
        String time1 = "2019-03-01";
        System.out.printf("String转DateLocal:" + DateTools.stringToLocalDate(time1,pattern));
        //获得今天日期
        System.out.println("---------获得今天日期---------");
        String today = DateTools.getToday(pattern);
        System.out.println("今天日期："+today);
//
//        //获得明天日期
//        System.out.println("---------获得明天日期---------");
//        String tomorrow = DateTools.getTomorrow(pattern);
//        System.out.println("明天日期："+tomorrow);
//
//        //获得昨天日期
//        System.out.println("---------获得昨天日期---------");
//        String yesterday = DateTools.getYesterday(pattern);
//        System.out.println("昨天日期："+yesterday);
//
//        //获得指定日期的后一天
//        System.out.println("---------获得指定日期的后一天---------");
//        String date_str = "2018-10-03";
//        System.out.println("指定日期："+date_str);
//        String after_date = DateTools.getAfterDay(date_str,pattern);
//        System.out.println("指定日期的后一天："+after_date);
//
//        //获得指定日期的后一天
//        System.out.println("---------获得指定日期的前一天---------");
//        System.out.println("指定日期："+date_str);
//        String before_date = DateTools.getBreforeDay(date_str,pattern);
//        System.out.println("指定日期的前一天："+before_date);
    }

}