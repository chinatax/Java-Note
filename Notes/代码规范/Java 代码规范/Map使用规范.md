<h1 align="center"><a href="#" target="_blank">Map 使用规范</a></h1>


## 二、 迭代 entrySet() 获取 Map 的 key 和 value

当循环中只需要获取 Map 的主键 key 时，迭代 keySet() 是正确的；但是，当需要主键 key 和取值 value 时，迭代 entrySet() 才是更高效的做法，其比先迭代 keySet() 后再去通过 get 取值性能更佳。

### Map 获取key & value 反例：

```java
 HashMap<String, String> map = new HashMap<>();
 for (String key : map.keySet()){
     String value = map.get(key);
 }
```

### Map 获取key & value 正例：

```java
 HashMap<String, String> map = new HashMap<>();
 for (Map.Entry<String,String> entry : map.entrySet()){
    String key = entry.getKey();
    String value = entry.getValue();
}
```