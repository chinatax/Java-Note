# Java 泛型

## 一、概述

`Java` 泛型（`generics`）是 `JDK 1.5` 中引入的一个新特性, 泛型提供了**编译时类型安全检测机制**，该机制允许开发者在编译时检测到非法的类型。

### 1.1 什么是泛型？

- 泛型，即**参数化类型**。

一提到参数，最熟悉的就是定义方法时有形参，然后调用此方法时传递实参。那么参数化类型怎么理解呢？顾名思义，就是将类型由原来的具体的类型参数化，类似于方法中的变量参数，此时类型也定义成参数形式（可以称之为类型形参），然后在使用/调用时传入具体的类型（类型实参）。

- **泛型的本质是为了参数化类型**

在不创建新的类型的情况下，通过泛型指定的不同类型来控制形参具体限制的类型。也就是说在泛型使用过程中，操作的数据类型被指定为一个参数，这种参数类型可以用在类、接口和方法中，分别被称为泛型类、泛型接口、泛型方法。

### 1.2 举个栗子：

```java
@Test
public void genericDemo() {
    List list = new ArrayList();
    list.add("风尘博客");
    list.add(100);

    for(int i = 0; i< list.size();i++){
        String item = (String)list.get(i);
        log.info("item:{}", item);
    }
}
```

毫无疑问，程序的运行结果会以崩溃结束：

```xml
java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
```

`ArrayList`可以存放任意类型，例子中先添加了一个`String`类型，又添加了一个`Integer`类型。使用时都以`String`的方式使用，因此程序崩溃了。为了解决类似这样的问题（在编译阶段就可以解决），泛型应运而生。

### 1.3 特性

> 泛型只在编译阶段有效

1. 在编译的时候能够检查类型安全，并且所有的强制转换都是自动和隐式的；
2. 在逻辑上看以看成是多个不同的类型，实际上都是相同的基本类型。


## 二、泛型的使用

泛型有三种使用方式，分别为：泛型类、泛型接口、泛型方法。

### 2.1 泛型类

泛型类型用于类的定义中，被称为泛型类。通过泛型可以完成对一组类的操作对外开放相同的接口。最典型的就是各种容器类，如：`List`、`Set`、`Map`。

#### 2.1.1 一个最普通的泛型类

```java
public class GenericClass<T> {
    /**
     * key这个成员变量的类型为T,T的类型由外部指定
     */
    private T key;

    /**
     * 泛型构造方法形参key的类型也为T，T的类型由外部指定
     * @param key
     */
    public GenericClass(T key) {
        this.key = key;
    }

    /**
     * 泛型方法getKey()的返回值类型为T，T的类型由外部指定
     * @return
     */
    public T getKey(){
        return key;
    }
}
```

> 说明：
>  
>1. 此处`T`可以随便写为任意标识，常见的如`T`、`E`、`K`、`V`等形式的参数常用于表示泛型；
>2. 在实例化泛型类时，必须指定`T`的具体类型。

#### 2.1.2 泛型的使用

- 指定泛型类型

```java
@Test
public void genericDemoWithType() {
    //泛型的类型参数只能是类类型（包括自定义类），不能是简单类型，比如这里Integer改为int编译将不通过
    GenericClass<Integer> integerGeneric = new GenericClass<Integer>(123456);
    logger.info("integerGeneric key is:{}", integerGeneric.getKey());

    //传入的实参类型需与泛型的类型参数类型相同，即为String.
    GenericClass<String> stringGeneric = new GenericClass<String>("风尘博客");
    logger.info("stringGeneric key is:{}", stringGeneric.getKey());
}
```

- 不指定泛型类型

如果不传入泛型类型实参的话，在泛型类中使用泛型的方法或成员变量定义的类型可以为任何的类型。

```java
@Test
public void genericDemoWithOutType() {
    GenericClass generic = new GenericClass("111111");
    logger.info("generic key is:{}", generic.getKey());
    GenericClass generic1 = new GenericClass(4444);
    logger.info("generic1 key is:{}", generic1.getKey());
    GenericClass generic2 = new GenericClass(55.55);
    logger.info("generic2 key is:{}", generic2.getKey());
    GenericClass generic3 = new GenericClass(false);
    logger.info("generic3 key is:{}", generic3.getKey());
}
```

#### 2.1.3 泛型类小结

1. 泛型的类型参数只能是类类型，不能是简单类型;
1. 不能对确切的泛型类型使用`instanceof`操作。

### 2.2 泛型接口

泛型接口与泛型类的定义及使用基本相同。泛型接口常被用在各种类的生产器中，例如：

```java
public interface GeneratorInterface<T> {
    public T next();
}
```

- 当实现泛型接口的类，未传入泛型实参时

> 未传入泛型实参时，与泛型类的定义相同，在声明类的时候，需将泛型的声明也一起加到类中。

```java
public class FruitGenerator<T> implements GeneratorInterface<T> {

    @Override
    public T next() {
        return null;
    }
}
```

- 当实现泛型接口的类，传入泛型实参

> 在实现类实现泛型接口时，如已将泛型类型传入实参类型，则所有使用泛型的地方都要替换成传入的实参类型。

```java
public class VegetablesGenerator implements GeneratorInterface<String> {

    private String[] vegetables = new String[]{"Potato", "Tomato"};

    @Override
    public String next() {
        Random rand = new Random();
        return vegetables[rand.nextInt(2)];
    }
}
```

### 2.3 泛型方法

在`java`中,泛型类的定义非常简单，但是泛型方法就比较复杂了。

> 我们见到的大多数泛型类中的成员方法也都使用了泛型，有的甚至泛型类中也包含着泛型方法。

- 泛型类和泛型方法的区别

| 名称 | 泛型类 | 泛型方法 |
| -- |  -- | -- |
| 区别 | 是在实例化类的时候指明泛型的具体类型 | 是在调用方法的时候指明泛型的具体类型 |

#### 2.3.1 定义


```java
public <T> T keyName(GenericMethod<T> container){
    T test = container.getKey();
    return test;        
}
```

1. 首先在`public`与返回值之间的`<T>`必不可少，这表明这是一个泛型方法，并且声明了一个泛型`T`；
2. 这个`T`可以出现在这个泛型方法的任意位置；
3. 泛型的数量也可以为任意多个。

```java
public class GenericMethod<T> {
    private T key;

    public GenericMethod(T key) {
        this.key = key;
    }
    /**
     * 这里虽然在方法中使用了泛型，但是这并不是一个泛型方法，
     * 这只是类中一个普通的成员方法，只不过他的返回值是在声明泛型类已经声明过的泛型，
     * 所以在这个方法中才可以继续使用 T 这个泛型。
     * @return
     */
    public T getKey() {
        return key;
    }

    /**
     * 这才是一个真正的泛型方法
     * @param container
     * @param <T>
     * @return
     */
    public <T> T keyName(GenericMethod<T> container){
        T test = container.getKey();
        return test;        
    }

    /**
     * 这也不是一个泛型方法，这就是一个普通的方法，只是使用了Generic<Number>这个泛型类做形参而已。
     * @param obj
     */
    public void showKeyValue1(GenericMethod<Number> obj){

    }

    /**
     * 这也不是一个泛型方法，这也是一个普通的方法，只不过使用了泛型通配符?
     * @param obj
     */
    public void showKeyValue2(GenericMethod<?> obj){

    }


    /**
     * 该方法编译器会报错
     * 虽然我们声明了<T>,也表明了这是一个可以处理泛型的类型的泛型方法。
     * 但是只声明了泛型类型T，并未声明泛型类型E，因此编译器并不知道该如何处理E这个类型。
     * @param container
     * @param <T>
     * @return
     */
//    public <T> T showKeyName(GenericMethod<E> container){
//        return null;
//    }
}
```

#### 2.3.2 泛型方法的使用

泛型方法可以出现杂任何地方和任何场景中使用，但是有一种情况是非常特殊的，**泛型方法出现在泛型类中**。

```java
public class GenericMethodTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    class  Fruit{
        @Override
        public String toString() {
            return "fruit";
        }
    }

    class Apple extends Fruit{
        @Override
        public String toString() {
            return "apple";
        }
    }

    class Person{
        @Override
        public String toString() {
            return "Person";
        }
    }

    class GenerateTest<T>{

        public void show_1(T t){
            logger.info(t.toString());
        }

        //在泛型类中声明了一个泛型方法，使用泛型E，这种泛型E可以为任意类型。可以类型与T相同，也可以不同。
        //由于泛型方法在声明的时候会声明泛型<E>，因此即使在泛型类中并未声明泛型，编译器也能够正确识别泛型方法中识别的泛型。
        public <E> void show_3(E t){
            logger.info(t.toString());
        }

        //在泛型类中声明了一个泛型方法，使用泛型T，注意这个T是一种全新的类型，可以与泛型类中声明的T不是同一种类型。
        public <T> void show_2(T t){
            logger.info(t.toString());
        }
    }

    @Test
    public void execute() {
        Apple apple = new Apple();
        Person person = new Person();

        GenerateTest<Fruit> generateTest = new GenerateTest<>();
        //apple是Fruit的子类，所以这里可以
        generateTest.show_1(apple);
        //编译器会报错，因为泛型类型实参指定的是Fruit，而传入的实参类是Person
        //generateTest.show_1(person);
        //使用这两个方法都可以成功
        generateTest.show_2(apple);
        generateTest.show_2(person);
        //使用这两个方法也都可以成功
        generateTest.show_3(apple);
        generateTest.show_3(person);
    }
}
```

#### 2.3.3 静态方法与泛型

**静态方法无法访问类上定义的泛型；如果静态方法操作的引用数据类型不确定的时候，必须要将泛型定义在方法上。**

如果写成如下,编译器会报错

```java
public static void show(T t){
    
}
```

- 正确写法：

```java
public static <T> void show(T t){
    
}
```

#### 2.3.4 泛型方法小结

泛型方法能使方法独立于类而产生变化，以下是一个基本的指导原则：

> 无论何时，如果你能做到，你就该尽量使用泛型方法。也就是说，如果使用泛型方法将整个类泛型化，那么就应该使用泛型方法。另外对于一个`static`的方法而已，无法访问泛型类型的参数。所以如果`static`方法要使用泛型能力，就必须使其成为泛型方法。


## 三、泛型通配符

我们在定义泛型类，泛型方法，泛型接口的时候经常会碰见很多不同的通配符，比如 `T`、`E`、`K`、`V` 等等，这些通配符又都是什么意思呢？

### 3.1 常用的 `T`、`E`、`K`、`V`、`？`

本质上这些都是通配符，没啥区别，只不过是编码时的一种约定俗成的东西。比如上述代码中的 `T` ，我们可以换成 `A-Z` 之间的任何一个字母都可以，并不会影响程序的正常运行，但是如果换成其他的字母代替 `T` ，在可读性上可能会弱一些。通常情况下，`T`，`E`，`K`，`V`，`？` 是这样约定的：

1. `？`：表示不确定的 `java` 类型；
1. `T (type)`：表示具体的一个`java`类型；
1. `K V (key value)`：分别代表`java`键值中的`Key`/`Value`；
1. `E (element)`：代表`Element`。

### 3.2 `?`无界通配符

对于不确定或者不关心实际要操作的类型，可以使用无限制通配符（尖括号里一个问号，即 `<?>` ），表示可以持有任何类型。

### 3.3 上界通配符 `<? extends E>`

> 上界：用 `extends` 关键字声明，表示参数化的类型可能是所指定的类型，或者是此类型的子类。

```java
public void showKeyValue(GenericClass<? extends Number> obj){
    log.info("value is {}", obj.getKey());
}

@Test
public void testForUp() {
    GenericClass<String> generic1 = new GenericClass<String>("11111");
    GenericClass<Integer> generic2 = new GenericClass<Integer>(2222);
    GenericClass<Float> generic3 = new GenericClass<Float>(2.4f);
    GenericClass<Double> generic4 = new GenericClass<Double>(2.56);

    /*// 这一行代码编译器会提示错误，因为String类型并不是Number类型的子类
    showKeyValue(generic1);*/

    showKeyValue(generic2);
    showKeyValue(generic3);
    showKeyValue(generic4);
}
```

在类型参数中使用 `extends` 表示这个泛型中的参数必须是 `E` 或者 `E` 的子类，这样有两个好处：

1. 如果传入的类型不是 `E` 或者 `E` 的子类，编译不成功；
1. 泛型中可以使用 `E` 的方法，要不然还得强转成 `E` 才能使用。

### 3.4 下界通配符 `< ? super E>`

> 下界: 用 `super` 进行声明，表示参数化的类型可能是所指定的类型，或者是此类型的父类型，直至 `Object`

在类型参数中使用 `super` 表示这个泛型中的参数必须是 `E` 或者 `E` 的父类。

**泛型的上下边界添加，必须与泛型的声明在一起**

### 3.5 `?` 和 `T` 的区别

`?`和 `T` 都表示不确定的类型，区别在于我们可以对 `T` 进行操作，但是对 `?` 不行，比如如下这种 ：

```java
// 可以
T t = operate();

// 不可以
？ car = operate();
```

即：`T` 是一个确定的类型，通常用于泛型类和泛型方法的定义，`?`是一个不确定的类型，通常用于泛型方法的调用代码和形参，不能用于定义类和泛型方法。

### 3.6 `Class<T>` 和 `Class<?>` 区别

`Class<T>` 在实例化的时候，`T` 要替换成具体类。`Class<?>` 它是个通配泛型，`?`可以代表任何类型，所以主要用于声明时的限制情况。比如，我们可以这样做申明：

```java
// 可以
public Class<?> clazz;

// 不可以，因为 T 需要指定类型
public Class<T> clazzT;
```

所以当不知道定声明什么类型的 `Class` 的时候可以定义一 个`Class<?>`。
那如果也想 `public Class<T> clazzT`; 这样的话，就必须让当前的类也指定 `T` ，

```java
public class Wildcard<T> {

    public Class<?> clazz;

    public Class<T> clazzT;
}
```

## 四、泛型中值得注意的地方

### 4.1 类型擦除

> 泛型信息只存在于代码编译阶段，在进入 `JVM` 之前，与泛型相关的信息会被擦除掉，专业术语叫做类型擦除。

```java
public class GenericTypeErase {

    public static void main(String[] args) {
        List<String> l1 = new ArrayList<>();
        List<Integer> l2 = new ArrayList<>();
        System.out.println(l1.getClass() == l2.getClass());
    }
}
```

打印的结果为 `true`；是因为 `List<String>`和 `List<Integer>`在 `jvm` 中的 `Class` 都是 `List.class`，泛型信息被擦除了。

### 4.2 泛型类或者泛型方法中，不接受 `8` 种基本数据类型

需要使用它们对应的包装类。

### 4.3 `Java` 不能创建具体类型的泛型数组

```java
List<Integer>[] li2 = new ArrayList<Integer>[];
List<Boolean> li3 = new ArrayList<Boolean>[];
```

`List<Integer>`和 `List<Boolean>`在 `jvm` 中等同于`List<Object>`，所有的类型信息都被擦除，程序也无法分辨一个数组中的元素类型具体是 `List<Integer>`类型还是 `List<Boolean>`类型。

### 4.4 强烈建议大家使用泛型

它抽离了数据类型与代码逻辑，本意是提高程序代码的简洁性和可读性，并提供可能的编译时类型转换安全检测功能。

## 五、总结

[Githu 示例代码](https://github.com/vanDusty/JDK/tree/master/JDK-Generic)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。

#### 参考文章

1. [java 泛型详解-绝对是对泛型方法讲解最详细的，没有之一](https://blog.csdn.net/s10461/article/details/53941091)
1. [聊一聊-JAVA 泛型中的通配符 T，E，K，V，？](https://juejin.im/post/5d5789d26fb9a06ad0056bd9#comment)
