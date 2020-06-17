# Java 正则表达式详解

> 正则表达式定义了字符串的模式，可以用来搜索、编辑或处理文本。

## 一、正则基础知识点

### 1.1 元字符

元字符是构造正则表达式的一种基本元素。  

- 几个常用的元字符：

| 元字符 | 说明 |
| --- | --- |
| . | 匹配除换行符以外的任意字符 |
| \w | 匹配字母或数字或下划线或汉字 |
| \s | 匹配任意的空白符 |
| \d | 匹配数字 |
| \b | 匹配单词的开始或结束 |
| ^ | 匹配字符串的开始 |
| $ | 匹配字符串的结束 |

### 1.2 重复限定符

正则表达式中一些重复限定符，把重复部分用合适的限定符替代。

| 语法 | 说明 |
| --- | --- |
| * | 重复零次或更多次 |
| + | 重复一次或更多次 |
| ? | 重复零次或一次 |
| {n} | 重复 n 次 |
| {n,} | 重复 n 次或更多次 |
| {n,m} | 重复 n 到 m 次 |

### 1.3 分组

限定符是作用在与他左边最近的一个字符，如果我想要多个字符同时被限定那怎么办呢？

**正则表达式中用小括号 `()` 来做分组，也就是括号中的内容作为一个整体。**

### 1.4 转义

我们看到正则表达式用小括号来做分组，那么问题来了：

> 如果要匹配的字符串中本身就包含小括号，那是不是冲突？应该怎么办？

针对这种情况，正则提供了转义的方式，也就是要把这些元字符、限定符或者关键字转义成普通的字符，做法很简答，就是在要转义的字符前面加个斜杠，也就是 `\` 即可。

### 1.5 条件或

**正则用符号 `|` 来表示或，也叫做分支条件，当满足正则里的分支条件的任何一种条件时，都会当成是匹配成功。**

### 1.6 区间

正则提供一个元字符中括号 `[]` 来表示区间条件。

1.  限定 `0` 到 `9` 可以写成 `[0-9]`；
2.  限定 `A-Z` 写成 `[A-Z]`；
3.  限定某些数字 `[165]`。

### 1.7 反义

前面说到元字符的都是要匹配什么什么，当然如果你想反着来，不想匹配某些字符。

- 常用的反义元字符：

| 元字符 | 解释 |
| --- | --- |
| \W | 匹配任意不是字母，数字，下划线，汉字的字符 |
| \S | 匹配任意不是空白符的字符 |
| \D | 匹配任意非数字的字符 |
| \B | 匹配不是单词开头或结束的位置 |
| [^x] | 匹配除了 x 以外的任意字符 |
| [^aeiou] | 匹配除了 aeiou 这几个字母以外的任意字符 |


## 二、Java 正则基础知识
     
### 2.1 `Pattern` 类

- 定义

`Pattern` 对象是一个正则表达式的编译表示，它的构造方法是私有的,不可以直接创建，但可以通过 `Pattern.complie(String regex)` 简单工厂方法创建一个正则表达式。

- 常用方法

1. `complie(String regex)`：创建一个正则表达式对象；
1. `pattern()`：返回正则表达式的字符串形式；
2. `split(CharSequence input)`：用于分隔字符串,并返回一个`String[]` （`JDK` 中 `String.split(String regex)`就是通过`Pattern.split(CharSequence input)`实现的）；
3. `matcher(String regex,CharSequence input)`：用于快速匹配字符串,该方法适合用于只匹配一次,且匹配全部字符串；
4. `matcher(CharSequence input)`：返回一个 `Matcher` 对象。

> `Matcher` 类的构造方法也是私有的,不能随意创建,只能通过`Pattern.matcher(CharSequence input)`方法得到该类的实例.

- 小结

`Pattern` 类只能做一些简单的匹配操作,要想得到更强更便捷的正则匹配操作,那就需要将 `Pattern` 与 `Matcher` 一起使用。


### 2.2 `Matcher` 类

- 定义

`Matcher` 对象是对输入字符串进行解释和匹配操作的引擎。与 `Pattern` 类一样，`Matcher` 也没有公共构造方法。你需要调用 `Pattern` 对象的 `matcher()` 方法来获得一个 `Matcher` 对象。

- 常用方法

> `Matcher` 类提供三个匹配操作方法均返回 `boolean` 类型。

1. `matches()`：对整个字符串进行匹配,只有整个字符串都匹配了才返回`true`；
1. `lookingAt()`：对前面的字符串进行匹配,只有匹配到的字符串在最前面才返回 `true`；
1. `find()`：对字符串进行匹配,匹配到的字符串可以在任何位置。

### 2.3 `Matcher` 类拓展

当使用`matches()`、`lookingAt()`、`find()` 执行匹配操作后,就可以利用以下三个方法得到更详细的信息.

1. `start()`：返回匹配到的子字符串在字符串中的索引位置；
1. `end()`：返回匹配到的子字符串的最后一个字符在字符串中的索引位置；

1. `group()`：返回匹配到的子字符串。

- `Matcher` 类同时提供了四个将匹配子串替换成指定字符串的方法：

1. `replaceAll()` ;
1. `replaceFirst()` ;
1. `appendReplacement()` ;
1. `appendTail()`。



- 小结

`Matcher` 类提供了对正则表达式的分组支持,以及对正则表达式的多次匹配支持。


## 三、【推荐】正则工具类

### 3.1 正则表达式常量

为了便于管理正则表达式，专门用一个常量类存放正则表达式。

```java
public class RegexConstant {

    /**
     * 正则：手机号（简单）
     */
    public static final String REGEX_MOBILE_SIMPLE = "^[1]\\d{10}$";
    /**
     * 正则：手机号（精确）
     * <p>移动：134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188</p>
     * <p>联通：130、131、132、145、155、156、175、176、185、186</p>
     * <p>电信：133、153、173、177、180、181、189</p>
     * <p>全球星：1349</p>
     * <p>虚拟运营商：170</p>
     */
    public static final String REGEX_MOBILE_EXACT = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|(147))\\d{8}$";
    /**
     * 正则：电话号码
     */
    public static final String REGEX_TEL = "^0\\d{2,3}[- ]?\\d{7,8}";
    /**
     * 正则：身份证号码15位
     */
    public static final String REGEX_ID_CARD15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
    /**
     * 正则：身份证号码18位
     */
    public static final String REGEX_ID_CARD18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";
    /**
     * 正则：邮箱
     */
    public static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    /**
     * 正则：URL
     */
    public static final String REGEX_URL = "[a-zA-z]+://[^\\s]*";
    /**
     * 正则：汉字
     */
    public static final String REGEX_ZH = "^[\\u4e00-\\u9fa5]+$";
    /**
     * 正则：用户名，取值范围为a-z,A-Z,0-9,"_",汉字，不能以"_"结尾,用户名必须是6-20位
     */
    public static final String REGEX_USERNAME = "^[\\w\\u4e00-\\u9fa5]{6,20}(?<!_)$";
    /**
     * 正则：yyyy-MM-dd格式的日期校验，已考虑平闰年
     */
    public static final String REGEX_DATE = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
    /**
     * 正则：IP地址
     */
    public static final String REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
    /**
     * 正则：双字节字符(包括汉字在内)
     */
    public static final String REGEX_DOUBLE_BYTE_CHAR = "[^\\x00-\\xff]";
    /**
     * 正则：空白行
     */
    public static final String REGEX_BLANK_LINE = "\\n\\s*\\r";
    /**
     * 正则：QQ号
     */
    public static final String REGEX_QQ = "[1-9][0-9]{4,}";
    /**
     * 正则：中国邮政编码
     */
    public static final String REGEX_ZIP_CODE = "[1-9]\\d{5}(?!\\d)";
    /**
     * 正则：正整数
     */
    public static final String REGEX_POSITIVE_INTEGER = "^[1-9]\\d*$";
    /**
     * 正则：负整数
     */
    public static final String REGEX_NEGATIVE_INTEGER = "^-[1-9]\\d*$";
    /**
     * 正则：整数
     */
    public static final String REGEX_INTEGER = "^-?[1-9]\\d*$";
    /**
     * 正则：非负整数(正整数 + 0)
     */
    public static final String REGEX_NOT_NEGATIVE_INTEGER = "^[1-9]\\d*|0$";
    /**
     * 正则：非正整数（负整数 + 0）
     */
    public static final String REGEX_NOT_POSITIVE_INTEGER = "^-[1-9]\\d*|0$";
    /**
     * 正则：正浮点数
     */
    public static final String REGEX_POSITIVE_FLOAT = "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";
    /**
     * 正则：负浮点数
     */
    public static final String REGEX_NEGATIVE_FLOAT = "^-[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$";
    /**
     * 正则：只有数字
     */
    public static final String REGEX_NUMBER = "[0-9]*";
    /**
     * 正则：字母、数字及下划线
     */
    public static final String REGEX_NUMBER_LETTER = "^[0-9a-zA-Z-][\\w-_]{1,}$";
    /**
     * 正则：只有字母
     */
    public static final String REGEX_LETTER = "^[A-Za-z]+$";
    /**
     * 正则：是否包含括号
     */
    public static final String REGEX_BRACKETS = ".*[()\\[\\]{}（）]+.*";
    /**
     * 正则中需要被转义的关键字
     */
    private final static Character[] KEYS_ARRAY = {'$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|'};

    public final static Set<Character> RE_KEYS = new HashSet<>(Arrays.asList(KEYS_ARRAY));

}
```

### 3.2 判断是否匹配正则

配合正则表达式进行判断是否匹配

```java
public static boolean isMatch(String regex, CharSequence input) {
    return input != null && input.length() > 0 && Pattern.matches(regex, input);
}
```

- 示例

```java
@Test
public void isMatch() {
    String input = "0571-69123456";
    boolean isTel =  RegexUtil.isMatch(RegexConstant.REGEX_TEL, input);
    logger.info("result:{}", isTel);
    
    boolean isPhone =  RegexUtil.isMatch(RegexConstant.REGEX_MOBILE_SIMPLE, input);
    logger.info("result:{}", isPhone);

}
```

### 3.3 获取正则匹配的部分

- 返回结果是字符串

```java
public static String getMatches(String regex, CharSequence input) {
    if (input == null || input.length() == 0) {
        return "";
    }
    StringBuffer matches = new StringBuffer();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
        matches.append(matcher.group());
    }
    return matches.toString();
}
```
- 返回结果是集合

```java
public static List<Object> getMatchesList(String regex, CharSequence input) {
    Pattern pattern = Pattern.compile(regex);
    if (input == null || input.length() == 0) {
        return null;
    }
    List<Object> matches = new ArrayList<>();
    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
        if (!matcher.group().isEmpty()) {
            matches.add(matcher.group());
        }
    }
    return matches;
}
```

- 示例

```java
@Test
public void getMatches() {
    String input = "0571-69123456";
    String result = RegexUtil.getMatches(RegexConstant.REGEX_NUMBER, input);
    logger.info("result:{}", result);
    List<Object> list = RegexUtil.getMatchesList(RegexConstant.REGEX_NUMBER, input);
    logger.info("result:{}", list);
}
```

### 3.4 计算指定字符串中，匹配 pattern 的个数

```java
public static int count(String regex, CharSequence input) {
    if (null == regex || input == null || input.length() == 0) {
        return 0;
    }
    Pattern pattern = Pattern.compile(regex);
    int count = 0;
    final Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
        if (!matcher.group().isEmpty()) {
            count++;
        }
    }
    return count;
}
```

- 示例

```java
@Test
public void count() {
    String input = "0571-69123456";
    int result = RegexUtil.count(RegexConstant.REGEX_NUMBER, input);
    logger.info("result:{}", result);
}
```

### 3.5 替换正则匹配

- 替换所有正则匹配的部分

```java
public static String replaceAll(String regex, String input, String replacement) {
    if (input == null || input.length() == 0) {
        return "";
    }
    return Pattern.compile(regex).matcher(input).replaceAll(replacement);
}
```

- 替换正则匹配的第一部分

```java
public static String replaceFirst(String regex, String input, String replacement) {
    if (input == null || input.length() == 0) {
        return "";
    }
    return Pattern.compile(regex).matcher(input).replaceFirst(replacement);
}
```

- 删除指定前缀，如果没有找到，则返回原文

```java
public static String delPre(String regex, CharSequence input) {
    if (regex == null || input == null ||input.length() == 0) {
        return "";
    }
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
        return input.toString().substring(matcher.end(), input.length());
    }
    return input.toString();
}
```

- 示例

```java
@Test
public void replace() {
    String input = "0571-69123456-hz";
    String result = RegexUtil.replaceAll("-",input,"_");
    logger.info("result:{}", result);
    result = RegexUtil.replaceFirst("-",input,"_");
    logger.info("result:{}", result);
    result = RegexUtil.delPre("-",input);
    logger.info("result:{}", result);
}
```

### 3.6 转义字符串

> 将 `Java` 中的正则的关键字转义。

```java
public static String escape(CharSequence input) {
    if (input == null ||input.length() == 0) {
        return "";
    }
    final StringBuilder builder = new StringBuilder();
    int len = input.length();
    char current;
    for (int i = 0; i < len; i++) {
        current = input.charAt(i);
        if (RegexConstant.RE_KEYS.contains(current)) {
            builder.append('\\');
        }
        builder.append(current);
    }
    return builder.toString();
}
```

- 示例

```java
@Test
public void escape() {
    String input = "$123.45";
    String result = RegexUtil.escape(input);
    logger.info("result:{}", result);
}
```

完整工具类详见 [【RegexUtil.java】](https://github.com/vanDusty/jdk/blob/master/JDK-Tool/src/main/java/cn/van/jdk/tool/verify/RegexUtil.java)

## 四、总结

正则表达式是操作字符串的有效手段，但是在方便我们开发的同时，我们必须意识到：过多使用正则表达式会造成代码可读性下降。


> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。

1. [【Github 示例代码】](https://github.com/vanDusty/jdk/blob/master/JDK-Tool/src/main/java/cn/van/jdk/tool/verify/RegexUtil.java)
1. [【Java 在线表达式工具】](http://www.regexplanet.com/advanced/java/index.html)
