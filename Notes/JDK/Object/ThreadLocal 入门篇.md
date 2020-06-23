# ThreadLocal 入门篇

## 一、定义

`ThreadLocal`是`JDK`包提供的，从名字来看，`ThreadLocal`意思就是本地线程的意思。

### 1.1 是什么？

要想知道他是个啥，我们看看`ThreadLocal`的源码（基于`JDK 1.8`）中对这个类的介绍：

```xml
This class provides thread-local variables.  These variables differ from
their normal counterparts in that each thread that accesses one (via its
{@code get} or {@code set} method) has its own, independently initialized
copy of the variable.  {@code ThreadLocal} instances are typically private
static fields in classes that wish to associate state with a thread (e.g.,
a user ID or Transaction ID).
```

大致能够总结出：

1. `TreadLocal`可以给我们提供一个线程内的局部变量，而且这个变量与一般的变量还不同，**它是每个线程独有的**，与其他线程互不干扰的；
1. `ThreadLocal` 与普通变量的区别在于：每个使用该变量的线程都会初始化一个完全独立的实例副本。`ThreadLocal` 变量通常被`private static`修饰。当一个线程结束时，它所使用的所有 `ThreadLocal` 相对的实例副本都会被回收；
1. 简单说`ThreadLocal`就是一种以空间换时间的做法，在每个`Thread`里面维护了一个`ThreadLocal.ThreadLocalMap`，**把数据进行隔离，每个线程的数据不共享**，自然就没有线程安全方面的问题了.

### 1.2 `ThreadLocal`的`API`

`ThreadLocal`定义了四个方法:

1. `get()`:返回此线程局部变量当前副本中的值；
1. `set(T value)`:将线程局部变量当前副本中的值设置为指定值；
1. `initialValue()`:返回此线程局部变量当前副本中的初始值；
1. `remove()`:移除此线程局部变量当前副本中的值。

- `set()`和`initialValue()`区别


| 名称 | `set()` | `initialValue()` |
| -- | -- | -- |
| 定义 | 为这个线程设置一个新值 | 该方法用于设置初始值，并且在调用`get()`方法时才会被触发，所以是懒加载。但是如果在`get()`之前进行了`set()`操作，这样就不会调用 |
| 区别  | 如果对象生成的时机不由我们控制的时候使用 `set()` 方式 | 对象初始化的时机由我们控制的时候使用`initialValue()` 方式  |


## 二、实现原理

`ThreadLocal`有一个特别重要的静态内部类`ThreadLocalMap`，该类才是实现线程隔离机制的关键。

- 每个线程的本地变量不是存放在`ThreadLocal`实例里面，而是存放在调用线程的`threadLocals`变量里面，也就是说：`ThreadLocal`类型的本地变量存放在具体的线程内存空间中。

```java
ThreadLocal.ThreadLocalMap threadLocals = null;
ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
```

- `Thread`类中有两个`ThreadLocalMap`类型的变量，分别是`threadLocals`和`inheritableThreadLocals`，而`ThreadLocalMap`是一个定制化的`Hashmap`，专门用来存储线程本地变量。在默认情况下，每个线程中的这两个变量都为`null`，只有当前线程第一次调用`ThreadLocal`的`set()`或者`get()`方法时才会创建它们。

	![风尘博客](/File/Imgs/article/ThreadLoal%20for%20Principle.png)

- `ThreadLocal`就是一个工具壳，它通过`set()`方法把`value`值放入调用线程的`threadLocals`里面并存放起来，当调用线程调用它的`get()`方法时，再从当前线程的`threadLocals`变量里面将其拿出来使用。

- 如果调用线程一直不终止，那么这个本地变量会一直存放在调用线程的`threadLocals`变量里面，所以当不需要使用本地变量时可以通过调用`ThreadLocal`变量的`remove()`方法，从当前线程的`threadLocals`里面删除该本地变量。

另外`Thread`里面的`threadLocals`被设计为`Map`结构是因为每个线程可以关联多个`ThreadLocal`变量。

### 原理小结

1. 每个`Thread`维护着一个`ThreadLocalMap`的引用；
1. `ThreadLocalMap`是`ThreadLocal`的内部类，用`Entry`来进行存储；
1. 调用`ThreadLocal`的`set()`方法时，实际上就是往`ThreadLocalMap`设置值，`key`是`ThreadLocal`对象，值是传递进来的对象；
1. 调用`ThreadLocal`的`get()`方法时，实际上就是往`ThreadLocalMap`获取值，`key`是`ThreadLocal`对象；
1. `ThreadLocal`本身并不存储值，它只是作为一个`key`来让线程从`ThreadLocalMa`p获取`value`。

## 三、使用场景

### 3.1 `ThreadLocal`的作用

- 保存线程上下文信息，在任意需要的地方可以获取.

由于`ThreadLocal`的特性，同一线程在某地方进行设置，在随后的任意地方都可以获取到。从而可以用来保存线程上下文信息。

- 线程安全的，避免某些情况需要考虑线程安全必须同步带来的性能损失.

### 3.2 场景一：独享对象

每个线程需要一个独享对象（通常是工具类，典型需要使用的类有`SimpleDateFormat`和`Random`）

这类场景阿里规范里面也提到了：

![风尘博客](/File/Imgs/article/ThreaLocal%20for%20Alibaba.png)

### 3.3 场景二：当前信息需要被线程内的所有方法共享

每个线程内需要保存全局变量（例如在拦截器中获取用户信息），可以让不同方法直接使用，避免参数传递的麻烦。

![风尘博客](/File/Imgs/article/ThreadLocal%20for%20Params.png)


### 3.4 使用`ThreadLocal`的好处

1. 达到线程安全的目的；
1. 不需要加锁，执行效率高；
1. 更加节省内存，节省开销；
1. 免去传参的繁琐，降低代码耦合度。

## 四、问题

### 4.1 内存泄漏问题

> 内存泄露：某个对象不会再被使用，但是该对象的内存却无法被收回

- 正常情况

当`Thread`运行结束后，`ThreadLocal`中的`value`会被回收，因为没有任何强引用了。

- 非正常情况

当`Thread`一直在运行始终不结束，强引用就不会被回收，存在以下调用链

```
Thread-->ThreadLocalMap-->Entry(key为null)-->value
```

因为调用链中的 `value` 和 `Thread` 存在强引用，所以`value`无法被回收，就有可能出现`OOM`。

**如何避免内存泄漏(阿里规范)**

调用`remove()`方法，就会删除对应的`Entry`对象，可以避免内存泄漏，所以使用完`ThreadLocal`后，要调用`remove()`方法。

![风尘博客](/File/Imgs/article/ThreadLocal%20for%20OOM.png)

### 4.2 `ThreadLocal`的空指针问题

- `ThreadLocalNPE.java`

```java
public class ThreadLocalNPE {

    ThreadLocal<Long> longThreadLocal = new ThreadLocal<>();

    public void set() {
        longThreadLocal.set(Thread.currentThread().getId());
    }

    /**
     * 当前返回值为基本类型，会报空指针异常，如果改成包装类型Long就不会出错
     * @return
     */
    public long get() {
        return longThreadLocal.get();
    }
}
```

- 空指针测试

```java
@Test
public void threadLocalNPE() {
    ThreadLocalNPE threadLocalNPE = new ThreadLocalNPE();
    //如果get方法返回值为基本类型，则会报空指针异常，如果是包装类型就不会出错
    System.out.println(threadLocalNPE.get());
}
```

如果`get()`方法返回值为基本类型，则会报空指针异常；如果是包装类型就不会出错。这是因为基本类型和包装类型存在装箱和拆箱的关系，所以，我们必须将`get()`方法返回值使用包装类型。

### 参考文章

1. [再也不学Threadlocal了，看这一篇就忘不掉了（万字总结）](https://www.cnblogs.com/ithuangqing/p/12114581.html)
2. [使用 ThreadLocal 一次解决老大难问题](https://juejin.im/post/5e0d8765f265da5d332cde44#comment)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
