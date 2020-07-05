# Spring 计时器 StopWatch

> 如果你也觉得`System.currentTimeMillis()`用起来太麻烦了，欢迎加入`StopWatch`的怀抱。

## 一、背景

在我们平时开发中，多多少少会遇到统计一段代码片段的耗时的情况，最简单的方法就是打印当前时间与执行完时间的差值。还可能会想到`AOP`，使用切面可以无侵入的实现，但是该方法统计粒度为方法级别，方法内部的各个任务耗时无法统计。

## 二、`Spring`的`StopWatch`

> `spring-framework`提供了一个`StopWatch`类可以做类似任务执行时间控制，也就是封装了一个对开始时间，结束时间记录操作的`Java`类。

`StopWatch`对于秒、毫秒为单位方便计时的程序，假如我们手里面有几个在顺序上前后执行的几个任务，而且我们比较关心几个任务分别执行的时间占用状况，希望能够形成一个不太复杂的日志输出，`StopWatch`提供了这样的功能。

### 2.1 开箱即用

```java
@Test
public void stopWatchDemo() throws InterruptedException {

    StopWatch sw = new StopWatch();

    sw.start("起床");
    Thread.sleep(1000);
    sw.stop();

    sw.start("洗漱");
    Thread.sleep(2000);
    sw.stop();

    sw.start("锁门");
    Thread.sleep(500);
    sw.stop();

    log.info("all cost info:{}",sw.prettyPrint());
}
```

1. 先`new`一个`StopWatch`然后将这个实例`start("任务名称")`；
2. 一个任务执行完毕则执行`stop()`；
3. 下一个任务开启前`start(“下一任务名称‘)`；
4. 最后可以调用 `prettyPrint()`方法返回一个小型的报表，输出代码执行耗时，以及执行时间百分比。


```xml
20:00:42.981 [main] INFO cn.van.spring.demo.StopWatchTest - all cost info:StopWatch '': running time (millis) = 3506
-----------------------------------------
ms     %     Task name
-----------------------------------------
01000  029%  起床
02002  057%  洗漱
00504  014%  锁门
```

### 2.2 更多用法

```java
@Test
public void stopWatchDemo() throws InterruptedException {

    StopWatch sw = new StopWatch();
    // start 开始记录指定任务
    sw.start("起床");
    Thread.sleep(1000);
    // stop 停止记录
    sw.stop();

    sw.start("洗漱");
    Thread.sleep(2000);
    sw.stop();

    sw.start("锁门");
    Thread.sleep(500);
    sw.stop();
    // 输出代码执行耗时，以及执行时间百分比。
    log.info("all cost info:{}",sw.prettyPrint());
    // 统计输出总耗时
    log.info("all costTime:{}",sw.getTotalTimeMillis());
    // 返回简短的总耗时描述
    log.info("fff:{}",sw.shortSummary());
    // 最后一个任务的名称/耗时
    log.info("last taskName:{}",sw.getLastTaskName());
    log.info("last taskInfo:{}",sw.getLastTaskTimeMillis());
    // 返回统计时间任务的数量
    log.info("last taskCount:{}",sw.getTaskCount());
}
```

1. `getTotalTimeSeconds()`:获取总耗时秒，同时也有获取毫秒的方法；
1. `prettyPrint()`: 优雅的格式打印结果，表格形式；
1. `shortSummary()`:返回简短的总耗时描述；
1. `getTaskCount()`:返回统计时间任务的数量；
1. `getLastTaskName()`/`getLastTaskTimeMillis()`: 返回最后一个任务对象的名称/耗时。

[官方文档](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/StopWatch.html)

### 2.3 优缺点

- 优点

	1. `spring`自带工具类，可直接使用；
	1. 代码实现简单，使用更简单；
	1. 统一归纳，展示每项任务耗时与占用总时间的百分比，展示结果直观；
	1. 性能消耗相对较小，并且最大程度的保证了`start`与`stop`之间的时间记录的准确性；
	1. 可在`start`时直接指定任务名字，从而更加直观的显示记录结果。

- 缺点

	1. 一个`StopWatch`实例一次只能开启一个`task`，不能同时`start`多个`task`，并且在该`task`未`stop`之前不能`start`一个新的`task`；
	2. 相较于`AOP`，代码入侵性强。

## 三、总结

使用`Spring`提供的这个监视器，不仅可以省略大量的`System.currentTimeMillis()`及运算，而且它提供的`prettyPrint()`打印在日志里进行分析可以非常的直观。

[Github 示例代码](https://github.com/vanDusty/Frame-Home/blob/master/spring-case/spring-demo/src/test/java/cn/van/spring/demo/StopWatchTest.java)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。

