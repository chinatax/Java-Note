# 【JUC 并发】CyclicBarrier

`CyclicBarrier` 是一种多线程并发控制工具，与`CountDownLatch`非常类似。它可以让一组线程到达栅栏时被阻塞，直到最后一个线程到达，才放行通过。比如斗地主，需要等待所有玩家进度条 `100%` 了，才能进入游戏。

## 一、`CyclicBarrier`

### 1.1 构造函数

```java
public CyclicBarrier(int parties) {}
public CyclicBarrier(int parties, Runnable barrierAction) {}
```

1. `parties`：阻塞的线程数量，每个线程使用`await()`方法告诉`CyclicBarrier`我已经到达了屏障，然后当前线程被阻塞；
2. `barrierAction`：当最后一个线程到达是先执行这个任务，再去执行后面的流程(优先执行`barrierAction`，再执行主流程，方便处理更复杂的业务场景)。

### 1.2 主要方法

1. `int await()`：当前线程到达栅栏点等待，`parties`数目`-1`。注意：**调用次数要和入参数量一致，否则会一直阻塞的等待**；
1. `int getParties()`：返回设置屏障外的线程数；
1. `void reset()`：将屏障外的线程数重置，如果还有其他线程正在屏障等待，则会抛出`BrokenBarrierException`。



## 二、`CyclicBarrier`的使用

`CyclicBarrier`用于使线程彼此等待，当不同的线程分别处理计算中的一部分服务，并且当所有线程都完成执行的时候，使用它，也就是说当多个线程执行不同的子任务并且需要组合这些子任务的输出来形成最终的输出，可以使用`CyclicBarrier`。完成执行后，线程调用`await()`方法并且等待其他线程到达栅栏。一旦所有的线程到达，`CyclicBarrier`就会为线程提供方法。

### 2.1 `CyclicBarrier`应用场景例子

接下来模拟斗地主玩家的载入：地主玩家初始化游戏、贫民玩家1初始化游戏和贫民玩家2初始化游戏都进行完成后才能进入游戏。

- 【地主玩家】初始化游戏

```java
public class LandlordPlayer implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private CyclicBarrier cyclicBarrier;

    public LandlordPlayer(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            // 地主玩家初始化游戏需要2秒
            TimeUnit.SECONDS.sleep(2);
            logger.info("【地主玩家】进入游戏");
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            logger.info("【地主玩家】进入游戏失败！");
        }
    }
}
```

- 【贫民玩家1】初始化游戏

```java
public class FarmerPlayer1 implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private CyclicBarrier cyclicBarrier;

    public FarmerPlayer1(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            // 贫民玩家1玩家初始化游戏需要3秒
            TimeUnit.SECONDS.sleep(3);
            logger.info("【贫民玩家1】进入游戏");
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            logger.info("【贫民玩家1】进入游戏失败");
        }
    }
}
```

- 【贫民玩家2】初始化游戏


```java
public class FarmerPlayer2 implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private CyclicBarrier cyclicBarrier;

    public FarmerPlayer2(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            // 贫民玩家2玩家初始化游戏需要3秒
            TimeUnit.SECONDS.sleep(3);
            logger.info("【贫民玩家2】进入游戏");
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            logger.info("【贫民玩家2】进入游戏失败！");
        }
    }
}
```

- 游戏加载流程

```java
public class CyclicBarrierTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void cyclicBarrierDemo() throws BrokenBarrierException, InterruptedException {
        Long startTime = System.currentTimeMillis();
        // 4个是因为还有一个主线程也在等待
        logger.info("所有玩家就绪，准备进入游戏");
        CyclicBarrier cyclicBarrier = new CyclicBarrier(4, new Runnable() {
            @Override
            public void run() {
                logger.info("所有玩家进度条100%，游戏读取中。。。。");
            }
        });
        // 注意：初始化线程数量（如果线程数少于所需线程，则会阻塞）
        Executor executor = Executors.newFixedThreadPool(3);
        executor.execute(new LandlordPlayer(cyclicBarrier));
        executor.execute(new FarmerPlayer2(cyclicBarrier));
        executor.execute(new FarmerPlayer1(cyclicBarrier));
        cyclicBarrier.await();
        // 模拟游戏读取需要 1 秒
        TimeUnit.SECONDS.sleep(1);
        logger.info("游戏加载成功，耗时[{}]毫秒，开始游戏....", System.currentTimeMillis() - startTime);
    }
}
```

- 结果打印如下

```xml
17:12:01.324 [main] ... - 所有玩家就绪，准备进入游戏
17:12:03.337 [pool-1-thread-1] ... - 【地主玩家】进入游戏
17:12:04.338 [pool-1-thread-3] ... - 【贫民玩家1】进入游戏
17:12:04.338 [pool-1-thread-2] ... - 【贫民玩家2】进入游戏
17:12:04.338 [pool-1-thread-2] ... - 所有玩家进度条100%，游戏读取中。。。。
17:12:05.342 [main] ... - 游戏加载成功，耗时[4020]毫秒，开始游戏....
```

由于多线程并发使各个玩家同时加载，整个游戏加载耗时 `4020` 毫秒，效率比较高。

### 2.2 使用注意点

1. 相较于`CountDownLatch`，`CyclicBarrier` 可以重复使用，因为会去刷新 count 的数量；
1. `await()`调用次数要和入参数量一致，否则会一直阻塞的等待。


### 2.3 `CyclicBarrier`和`CountDownLatch`的区别

1. `CountDownLatch`的计数器只能使用一次，而`CyclicBarrier`的计数器可以使用`reset()`方法重置，可以使用多次，所以`CyclicBarrier`能够处理更为复杂的场景；
2. `CyclicBarrier`还提供了一些其他有用的方法，比如`getNumberWaiting()`方法可以获得`CyclicBarrier`阻塞的线程数量，`isBroken()`方法用来了解阻塞的线程是否被中断；
1. `CountDownLatch`是只阻塞主线程，所有子线程全部执行完毕之后唤醒主线程去执行；而`CyclicBarrier`则是所有线程到达栅栏点都会阻塞等待，直到后一个到达才唤醒所有的阻塞线程。

[【Github 示例源码】](https://github.com/vanDusty/JDK/tree/master/JDK-JUC/src/main/java/cn/van/jdk/juc/cyclicbarrier)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
