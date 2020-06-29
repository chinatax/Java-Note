# 【JUC 并发】Semaphore

`Semaphore` 意思的信号量，它的作用是控制访问特定资源的线程数量。

## 一、`Semaphore`

信号量是对锁的扩展，不管是同步`synchronized`还是`ReentrantLock`，一次只能允许一个线程访问一个资源，但是信号量可以使得**多个线程，同时访问一个资源**。

### 1.1 构造函数

```java
public Semaphore(int permits){}
public Semaphore(int permits, boolean fair){}
```

1. `permits`：能同时申请多少个信号量，主要用来控制线程的并发数量，即同时有多少个线程可以访问资源；
1. `fair`：是否公平，若`true`的话，下次执行的会是先进去等待的线程（先入先出）;


### 1.2 主要方法

1. `acquire()`：获取许可执行，若无法获得，会持续等待，直到线程释放一个许可；
1.  `tryAcquire()`：尝试获得准入许可，得到返回`true`，没有返回`false`，不会持续等待；
1. `tryAcquire(long timeout, TimeUnit unit)`：与`tryAcquire()`类似，但是会有一个等待的时间，超过时间立即返回；
1. `release()`：释放许可，让其他线程获取许可执行。

显然这个功能可以用于**资源访问控制**或者是**限流**的操作。

## 二、`Semaphore`的使用

首先，我们考虑下`Semaphore`是用来解决哪类问题的？

### 2.1 `CyclicBarrier`和`CountDownLatch`无法解决的场景

前面我们已经介绍了`CyclicBarrier`和`CountDownLatch`，有哪些场景是这两个还无法解决的。

- 场景一：抢车位

无论是景区、商场还是住宅小区，停车场中的车位数量是一定的。当车位满了以后，想要进入停车场停车的车辆只能等待。等到其他车辆出来之后，才可以进入，即每个车辆必须只能停在其中一个位置上(互斥使用的)。

- 场景二：海底捞吃火锅

去海底捞吃火锅的时候，餐厅餐桌数量是固定的，假设有`3`桌。现在来了`10`桌客人，那么其他客人就需要在门口候餐区等待。当有其他桌吃完离开之后，进去一个。

- `CyclicBarrier`和`CountDownLatch` 能否解决当前问题？

	1. 根据`CountDownLatch`的特性：只能使用一次，这两种场景当然不能使用；
	1. `CyclicBarrier`：虽然可以使用多次，但是需要`reset()`之后才可以多次使用。意思就是：**只有等餐厅里面`3`个桌的客人都吃完之后，才可以让其他人进来就餐的**，显然也是不符合业务逻辑的。


### 2.2 代码实战

> 以下代码以海底捞等位吃饭为例，即吃完一桌放一桌客人进来。

```java
public class SemaphoreDemo {
    /**
     * 假设有十桌客人准备就餐
     */
    private final static int tableCount = 10;

    public static void main(String[] args) throws InterruptedException {
        // 信号量，即餐厅只有3桌，仅能提供3桌客人同时就餐
        Semaphore semaphore = new Semaphore(3, true);
        Long startTime = System.currentTimeMillis();
        // 计数器，计算什么时候全部就餐结束
        CountDownLatch countDownLatch = new CountDownLatch(tableCount);
        for (int i = 1; i <= tableCount; i++) {
            final int count = i;
            Thread thread = new Thread(() -> {
                try {
                    // 获取资源，开始处理
                    semaphore.acquire();
                    System.out.println("第" + count + "批桌客人开始就餐");
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                    System.out.println("第" + count + "批桌客人就餐结束");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 处理结束，资源释放
                    semaphore.release();
                    countDownLatch.countDown();
                }
            }, "线程" + i);
            thread.start();
        }
        // 等待全部客人就餐结束
        countDownLatch.await();
        System.out.println("所有客人就餐完毕，总耗时" + (System.currentTimeMillis() - startTime) + "毫秒");
    }
}
```

1. 首先我们申明了信号量为`3`，那么同时就可以有`3`个线程进入到临界区中;
1. 当后续线程使用`acquire()`时候申请信号量，也必须有线程通过`release()`释放信号量。如果不释放，就意没有线程能进入。


- 结果打印如下

```xml
第1批桌客人开始就餐
第3批桌客人开始就餐
第2批桌客人开始就餐
第1批桌客人就餐结束
第4批桌客人开始就餐
第2批桌客人就餐结束
第5批桌客人开始就餐
第3批桌客人就餐结束
第6批桌客人开始就餐
第4批桌客人就餐结束
第7批桌客人开始就餐
第7批桌客人就餐结束
第8批桌客人开始就餐
第8批桌客人就餐结束
第5批桌客人就餐结束
第6批桌客人就餐结束
第10批桌客人开始就餐
第10批桌客人就餐结束
第9批桌客人开始就餐
第9批桌客人就餐结束
所有客人就餐完毕，总耗时7060毫秒
```

从运行结果中，仅当有一个离开餐桌下一位才能进入餐厅就餐，达到我们预期。


### 2.3 使用注意点

在使用`acquire()`获取资源，并处理结束后，需要在`finally`里面调用`release()`方法进行释放资源，如果不能正常释放资源，会导致**信号量泄漏**。



[【Github 示例源码】](https://github.com/vanDusty/JDK/tree/master/JDK-JUC/src/main/java/cn/van/jdk/juc/semaphore)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
