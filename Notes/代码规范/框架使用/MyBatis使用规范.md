<h1 align="center"><a href="#" target="_blank">MyBatis 使用规范</a></h1>


## 一、`MyBatis` 不要为了多个查询条件而写 `1 = 1`

    当遇到多个查询条件，使用 `where 1=1` 可以很方便的解决我们的问题，但是这样很可能会造成非常大的性能损失，因为添加了 `where 1=1` 的过滤条件之后，数据库系统就无法使用索引等查询优化策略，数据库系统将会被迫对每行数据进行扫描（即全表扫描） 以比较此行是否满足过滤条件，当表中的数据量较大时查询速度会非常慢；此外，还会存在 `SQL` 注入的风险。

### 反例：

```xml
    <select parameterType="com.tjt.platform.entity.BookInfo" resultType="java.lang.Integer">
         select count(*) from t_rule_BookInfo t where 1=1
        <if test="title !=null and title !='' ">
            AND title = #{title}
        </if>
        <if test="author !=null and author !='' ">
            AND author = #{author}
        </if>
    </select>
```

### 正例：


```xml
    <select parameterType="com.tjt.platform.entity.BookInfo" resultType="java.lang.Integer">
         select count(*) from t_rule_BookInfo t
        <where>
            <if test="title !=null and title !='' ">
                title = #{title}
            </if>
            <if test="author !=null and author !='' ">
                AND author = #{author}
            </if>
        </where>
    </select>

```

`UPDATE` 操作也一样，可以用 `<set>` 标记代替 `1=1`。
