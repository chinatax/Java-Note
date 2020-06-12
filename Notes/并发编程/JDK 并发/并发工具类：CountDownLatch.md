# 并发工具类：CountDownLatch

> 当 `N` 个线程同时完成某项任务时，如何知道他们都已经执行完毕了。

## 一、`CountDownLatch` 分析


`CountDownLatch` 是 `JDK 1.5`开始`concurrent`包里提供的，并发编程工具类。
可以把 `CountDownLatch` 看作一个多线程控制工具类，用于协调多个线程的同步，能让一个线程在等待其他线程执行完任务后，再继续执行。内部是通过一个计数器去完成实现。

### 1.1 构造函数

```java
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```
`count`代表需要执行的线程数量,也是同步计数器初始化的数量，只有当同步计数值为`0`，主线程才会向下执行。

### 主要方法

1. `void await()`：调用改方法的线程会被挂起，直到`count`值为`0`;
1. `boolean await(long timeout, TimeUnit unit)`：与上面的类似，只不过等待一定的时间后`count`值还没变为`0`的话就会继续执行;
1. `void countDown()`：在计数值 `> 0`的情况下，每当一个线程完成任务，计数减去`1`;
1. `long getCount()`：获取计数器的值。

> 当计数器应该为`0`，所有的线程执行完自己的任务。在`CountDownLatch`等待的线程，可以继续执行的任务。

## 二、`CountDownLatch`的使用

适用于一个任务的执行需要等待其他任务执行完毕，方可执行的场景。

> `CountDownLatch`是一次性的，只能通过构造方法设置初始计数量，计数完了无法进行复位，不能达到复用。

### 2.1 `CountDownLatch`应用场景例子

丈夫和妻子一块儿去买菜，假设买蔬菜需要`5`秒，买肉需要`3`秒，如果丈夫和妻子分开去买，然后一块儿回家。

- 妻子去蔬菜

```java
@Slf4j
public class WifeTask implements Runnable {

    private CountDownLatch countDownLatch;

    public WifeTask(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(5);
            log.info("妻子蔬菜买好了");
        } catch (InterruptedException e) {
            log.info("妻子蔬菜没买到");
        } finally {
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
    }
}
```

- 丈夫去买肉

```java
@Slf4j
public class HusbandTask implements Runnable{

    private CountDownLatch countDownLatch;

    public HusbandTask(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(3);
            log.info("丈夫肉买好了");
        } catch (InterruptedException e) {
            log.info("丈夫肉没买到");
        } finally {
            if(countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
    }
}
```

- 买好菜，在指定地点集合回家（回到主线程），计算耗时

```java
@Slf4j
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        Long startTime = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        // 夫妻分头去买菜，夫妻买蔬菜(需要5秒)，丈夫卖肉(需要3秒)
        Executor executor = Executors.newFixedThreadPool(2);
        executor.execute(new HusbandTask(countDownLatch));
        executor.execute(new WifeTask(countDownLatch));
        // 挂起任务，等菜买好了再一起回家
        countDownLatch.await();
        log.info("菜买好了，夫妻双双把家还，买菜合计耗时:{}毫秒",(System.currentTimeMillis()-startTime));
    }

}
```

- 结果打印如下

```xml
21:06:02.221 [pool-1-thread-1] INFO cn.van.jdk.five.countdownlatch.HusbandTask - 丈夫肉买好了
21:06:04.219 [pool-1-thread-2] INFO cn.van.jdk.five.countdownlatch.WifeTask - 妻子蔬菜买好了
21:06:04.220 [main] INFO cn.van.jdk.five.countdownlatch.CountDownLatchDemo - 菜买好了，夫妻双双把家还，买菜合计耗时:5005毫秒
```

通过`CountDownLatch`初始化计数器值为`2`，让夫妻两人各自去买菜，都买好后，即计数器值为`0`，回到主线程，一起回家。

### 2.2 `CountDownLatch`使用以及注意点

1. 一旦`count`达到零，不能再用`CountDownLatch`(即不可复用)。
1. 主线程通过调用`CountDownLatch.await()`方法等待`Latch`，而其他线程调用`CountDownLatch.countDown()`以通知它们已完成。
1. 计数器必须和要执行的线程数匹配，如果计数器大于了要执行的线程数目，那么`count`最后不会为`0`，调用的主线程也不会继续执行，主线程将会一直处于等待状态。

## 三、源码

[Github 示例源码](https://github.com/vanDusty/JDK/tree/master/JDK-5/src/main/java/cn/van/jdk/five/countdownlatch)