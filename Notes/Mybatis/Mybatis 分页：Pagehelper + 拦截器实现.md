# Mybatis 分页：`Pagehelper` + 拦截器实现

## 一、分页插件 `Pagehelper`

`PageHelper`是`Mybatis`的一个分页插件，非常好用！

### 1.1 `Spring Boot` 依赖

```xml
<!-- pagehelper 分页插件-->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.2.12</version>
</dependency>
```

也可以这么引入

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latest version</version>
</dependency>
```

### 1.2 `PageHelper` 配置

配置文件增加`PageHelper`的配置，主要设置了分页方言和支持接口参数传递分页参数，如下：

```xml
pagehelper:
  # 指定数据库
  helper-dialect: mysql
  # 默认是false。启用合理化时，如果pageNum<1会查询第一页，如果pageNum>pages（最大页数）会查询最后一页。禁用合理化时，如果pageNum<1或pageNum>pages会返回空数据
  reasonable: false
  # 是否支持接口参数来传递分页参数，默认false
  support-methods-arguments: true
  # 为了支持startPage(Object params)方法，增加了该参数来配置参数映射，用于从对象中根据属性名取值， 可以配置 pageNum,pageSize,count,pageSizeZero,reasonable，不配置映射的用默认值， 默认值为pageNum=pageNum;pageSize=pageSize;count=countSql;reasonable=reasonable;pageSizeZero=pageSizeZero
  params: count=countSql
  row-bounds-with-count: true
```

> 项目完整配置文件详见文[mybatis-pagehelper](https://github.com/vanDusty/mybatis-Home/blob/master/mybatis-case/mybatis-pagehelper/src/main/resources/application.yml)。

### 1.3 如何分页

只有紧跟在`PageHelper.startPage`方法后的第一个`Mybatis`的查询（`Select`）方法会自动分页!!!!

```java
@Test
public void selectForPage() {
    // 第几页
    int currentPage = 2;
    // 每页数量
    int pageSize = 5;
    // 排序
    String orderBy = "id desc";
    PageHelper.startPage(currentPage, pageSize, orderBy);
    List<UserInfoPagehelperDO> users = userInfoPagehelperMapper.selectList();
    PageInfo<UserInfoPagehelperDO> userPageInfo = new PageInfo<>(users);
    log.info("userPageInfo:{}", userPageInfo);
}
```

```xml
...: userPageInfo:PageInfo{pageNum=2, pageSize=5, size=1, startRow=6, endRow=6, total=6, pages=2, list=Page{count=true, pageNum=2, pageSize=5, startRow=5, endRow=10, total=6, pages=2, reasonable=false, pageSizeZero=false}[UserInfoPagehelperDO{id=1, userName='null', age=22, createTime=null}], prePage=1, nextPage=0, isFirstPage=false, isLastPage=true, hasPreviousPage=true, hasNextPage=false, navigatePages=8, navigateFirstPage=1, navigateLastPage=2, navigatepageNums=[1, 2]}
```

> 这里的返回结果包括数据、是否为第一页/最后一页、总页数、总记录数，详见[Mybatis-PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)

[Pagehelper 分页完整示例](https://github.com/vanDusty/Mybatis-Home/tree/master/mybatis-case/mybatis-pagehelper)

## 二、`Mybatis` 拦截器实现分页

### 2.1 `Mybatis` 拦截器

[Mybatis 官网](https://mybatis.org/mybatis-3/zh/configuration.html#plugins)【插件】部分有以下描述：

1. 通过 `MyBatis` 提供的强大机制，使用插件是非常简单的，只需实现 `Interceptor` 接口，并指定想要拦截的方法签名即可。
1. `MyBatis` 允许你在已映射语句执行过程中的某一点进行拦截调用。默认情况下，`MyBatis` 允许使用插件来拦截的方法调用包括：

```java
Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
ParameterHandler (getParameterObject, setParameters)
ResultSetHandler (handleResultSets, handleOutputParameters)
StatementHandler (prepare, parameterize, batch, update, query)
```

即：我们可以通过拦截器的方式，实现`MyBatis`插件(数据分页)

> 接下来重点演示我如何使用了拦截器实现分页。

### 2.2 调用形式

> 在看如何实现之前，我们先看看如何使用：

照抄 `PageHelper` 的设计，先调用一个静态方法，对下面第一个方法的`sql`语句进行拦截，在`new`一个分页对象时自动处理。

```java
@Test
public void selectForPage() {
    // 该查询进行分页，指定第几页和每页数量
    PageInterceptor.startPage(1,2);
    List<UserInfoDO> all = dao.findAll();
    PageResult<UserInfoDO> result = new PageResult<>(all);
    // 分页结果打印
    System.out.println("总记录数：" + result.getTotal());
    System.out.println(result.getData().toString());
}
```

然后我们主要看看实现步骤。

### 2.3 数据库方言

> 定义好一个方言接口，不同的数据使用不同的方言实现

- `Dialect.java`

```java
public interface Dialect {
    /**
     * 获取count SQL语句
     *
     * @param targetSql
     * @return
     */
    default String getCountSql(String targetSql) {
        return String.format("select count(1) from (%s) tmp_count", targetSql);
    }

    /**
     * 获取limit SQL语句
     * @param targetSql
     * @param offset
     * @param limit
     * @return
     */
    String getLimitSql(String targetSql, int offset, int limit);
}
```

- `Mysql` 分页方言

```java
@Component
public class MysqlDialect implements Dialect{

    private static final String PATTERN = "%s limit %s, %s";

    private static final String PATTERN_FIRST = "%s limit %s";

    @Override
    public String getLimitSql(String targetSql, int offset, int limit) {
        if (offset == 0) {
            return String.format(PATTERN_FIRST, targetSql, limit);
        }

        return String.format(PATTERN, targetSql, offset, limit);
    }
}
```

### 2.4 拦截器核心逻辑

> 该部分完整代码见 [PageInterceptor.java](https://github.com/vanDusty/mybatis-Home/blob/master/mybatis-case/mybatis-pageable/src/main/java/cn/van/mybatis/pageable/page/PageInterceptor.java)

- 分页辅助参数内部类 `PageParam.java`

```java
public static class PageParam {
    // 当前页
    int pageNum;

    // 分页开始位置
    int offset;

    // 分页数量
    int limit;

    // 总数
    public int totalSize;

    // 总页数
    public int totalPage;
}
```

- 查询总记录数

```java
private long queryTotal(MappedStatement mappedStatement, BoundSql boundSql) throws SQLException {

    Connection connection = null;
    PreparedStatement countStmt = null;
    ResultSet rs = null;
    try {

        connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();

        String countSql = this.dialect.getCountSql(boundSql.getSql());

        countStmt = connection.prepareStatement(countSql);
        BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql,
                boundSql.getParameterMappings(), boundSql.getParameterObject());

        setParameters(countStmt, mappedStatement, countBoundSql, boundSql.getParameterObject());

        rs = countStmt.executeQuery();
        long totalCount = 0;
        if (rs.next()) {
            totalCount = rs.getLong(1);
        }

        return totalCount;
    } catch (SQLException e) {
        log.error("查询总记录数出错", e);
        throw e;
    } finally {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("exception happens when doing: ResultSet.close()", e);
            }
        }

        if (countStmt != null) {
            try {
                countStmt.close();
            } catch (SQLException e) {
                log.error("exception happens when doing: PreparedStatement.close()", e);
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("exception happens when doing: Connection.close()", e);
            }
        }
    }
}
```

- 对分页`SQL`参数`?`设值

```java
private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql,
                               Object parameterObject) throws SQLException {
    ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler.setParameters(ps);
}
```

- 利用方言接口替换原始的`SQL`语句

```java
private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
    MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());

    builder.resource(ms.getResource());
    builder.fetchSize(ms.getFetchSize());
    builder.statementType(ms.getStatementType());
    builder.keyGenerator(ms.getKeyGenerator());
    if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
        StringBuffer keyProperties = new StringBuffer();
        for (String keyProperty : ms.getKeyProperties()) {
            keyProperties.append(keyProperty).append(",");
        }
        keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
        builder.keyProperty(keyProperties.toString());
    }

    //setStatementTimeout()
    builder.timeout(ms.getTimeout());

    //setStatementResultMap()
    builder.parameterMap(ms.getParameterMap());

    //setStatementResultMap()
    builder.resultMaps(ms.getResultMaps());
    builder.resultSetType(ms.getResultSetType());

    //setStatementCache()
    builder.cache(ms.getCache());
    builder.flushCacheRequired(ms.isFlushCacheRequired());
    builder.useCache(ms.isUseCache());

    return builder.build();
}
```

- 计算总页数

```java
public int countPage(int totalSize, int offset) {
    int totalPageTemp = totalSize / offset;
    int plus = (totalSize % offset) == 0 ? 0 : 1;
    totalPageTemp = totalPageTemp + plus;
    if (totalPageTemp <= 0) {
        totalPageTemp = 1;
    }
    return totalPageTemp;
}
```

- 供调用的静态分页方法

> 我这里设计的，页数是从`1`开始的，如果习惯用`0`开始，可以自己修改。

```java
public static void startPage(int pageNum, int pageSize) {
    int offset = (pageNum-1) * pageSize;
    int limit = pageSize;
    PageInterceptor.PageParam pageParam = new PageInterceptor.PageParam();
    pageParam.offset = offset;
    pageParam.limit = limit;
    pageParam.pageNum = pageNum;
    PARAM_THREAD_LOCAL.set(pageParam);
}
```


### 2.5 分页结果集

> 为了便于结果封装，我这里自己封装了一个比较全的分页结果集，包含太多的东西了，自己慢慢看下面的属性吧（自认为比较全了，欢迎打脸）

```java
public class PageResult<T> implements Serializable {
    /**
     * 是否为第一页
     */
    private Boolean isFirstPage = false;
    /**
     * 是否为最后一页
     */
    private Boolean isLastPage = false;
    /**
     * 当前页
     */
    private Integer pageNum;
    /**
     * 每页的数量
     */
    private Integer pageSize;
    /**
     * 总记录数
     */
    private Integer totalSize;
    /**
     * 总页数
     */
    private Integer totalPage;
    /**
     * 结果集
     */
    private List<T> data;

    public PageResult() {
    }

    public PageResult(List<T> data) {
        this.data = data;
        PageInterceptor.PageParam pageParam = PageInterceptor.PARAM_THREAD_LOCAL.get();
        if (pageParam != null) {
            pageNum = pageParam.pageNum;
            pageSize = pageParam.limit;
            totalSize = pageParam.totalSize;
            totalPage = pageParam.totalPage;
            isFirstPage = (pageNum == 1);
            isLastPage = (pageNum == totalPage);
            PageInterceptor.PARAM_THREAD_LOCAL.remove();
        }
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Boolean getFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(Boolean firstPage) {
        isFirstPage = firstPage;
    }

    public Boolean getLastPage() {
        return isLastPage;
    }

    public void setLastPage(Boolean lastPage) {
        isLastPage = lastPage;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "isFirstPage=" + isFirstPage +
                ", isLastPage=" + isLastPage +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", totalSize=" + totalSize +
                ", totalPage=" + totalPage +
                ", data=" + data +
                '}';
    }
}
```


### 2.6 简单测试下

```java
@Test
public void selectForPage() {
    // 该查询进行分页，指定第几页和每页数量
    PageInterceptor.startPage(1,4);
    List<UserInfoDO> all = userMapper.findAll();
    PageResult<UserInfoDO> result = new PageResult<>(all);
    // 分页结果打印
    log.info("总记录数：{}", result.getTotalSize());
    log.info("list：{}", result.getData());
    log.info("result:{}", result);
}
```

> 使用方法基本`1.3`完全一致吧，只是封装成了我自己的分页结果集。

- 日志如下：

```xml
....: ==>  Preparing: SELECT id, user_name, age, create_time FROM user_info_pageable limit 4 
....: ==> Parameters: 
....: <==      Total: 4
....: 总记录数：6
....: list：[UserInfoDO(id=1, userName=张三, age=22, createTime=2019-10-08T20:52:46), UserInfoDO(id=2, userName=李四, age=21, createTime=2019-12-23T20:22:54), UserInfoDO(id=3, userName=王二, age=22, createTime=2019-12-23T20:23:15), UserInfoDO(id=4, userName=马五, age=20, createTime=2019-12-23T20:23:15)]
....: result:PageResult{isFirstPage=true, isLastPage=false, pageNum=1, pageSize=4, totalSize=6, totalPage=2, data=[UserInfoDO(id=1, userName=张三, age=22, createTime=2019-10-08T20:52:46), UserInfoDO(id=2, userName=李四, age=21, createTime=2019-12-23T20:22:54), UserInfoDO(id=3, userName=王二, age=22, createTime=2019-12-23T20:23:15), UserInfoDO(id=4, userName=马五, age=20, createTime=2019-12-23T20:23:15)]}
```

通过日志分析，发现普通的`SELECT * FROM user_info_pageable` 被重新组装成`SELECT * FROM user_info_pageable limit 4`,说明拦截器实现的分页成功。


## 三、总结

两种方式：`Pagehelper` 分页和自己实现，根据实际情况自己选用吧。

1. [`Pagehelper` 分页示例代码](https://github.com/vanDusty/Mybatis-Home/tree/master/mybatis-case/mybatis-pagehelper)
1. [`Mybatis` 拦截器实现分页示例代码](https://github.com/vanDusty/Mybatis-Home/tree/master/mybatis-case/mybatis-pageable)