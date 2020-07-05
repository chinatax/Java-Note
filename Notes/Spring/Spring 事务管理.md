# Spring 事务管理

`Spring` 事务的本质其实就是数据库对事务的支持，没有数据库的事务支持，`Spring` 是无法提供事务功能的。


## 一、`Spring` 事务传播机制

所谓 `Spring` 事务的传播机制，就是定义在存在多个事务同时存在的时候， `Spring` 应该如何处理这些事务的行为。

| 事务传播行为 | 值 | 含义 |
| -- | -- | -- |
| PROPAGATION_REQUIRED	| 0 | 	支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择，也是Spring默认的事务的传播。| 
| PROPAGATION_SUPPORTS	| 1 | 支持当前事务，如果当前没有事务，就以非事务方式执行。| 
| PROPAGATION_MANDATORY	 | 2 | 支持当前事务，如果当前没有事务，就抛出异常。| 
| PROPAGATION_REQUIRES_NEW	 | 3 | 新建事务，如果当前存在事务，把当前事务挂起。| 
| PROPAGATION_NOT_SUPPORTED| 4 | 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。| 
| PROPAGATION_NEVER | 5 | 以非事务方式执行，如果当前存在事务，则抛出异常。| 
| PROPAGATION_NESTED | 6 | 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则进行与PROPAGATION_REQUIRED类似的操作。| 

## 二、`Spring` 事务机制

Spring并不会直接管理事务，而是提供了事务管理器，将事务管理的职责委托给JPA JDBC JTA DataSourceTransaction JMSTransactionManager 等框架提供的事务来实现。

**`Spring`提供的事务管理器是 `PlatformTransactionManager.java`接口**

### 2.1 `PlatformTransactionManager.java`

```java
public interface PlatformTransactionManager{    
	// 通过Transation定义、获取Transation    
	TransactionStatus getTransaction(@Nullable TransactionDefinition var1) throws 	TransactionException;
	// 提交事务
	 void commit (TransactionStatus var1) throws TransactionException;
	// 回滚事务
	void rollback(TransactionStatus var1) throws TransactionException;
}
```

可以看到它里面引用到了 `TransactionDefinition` 和 `TransactionStatus`。

### 2.2 `TransactionDefinition.java`

它里面包含了事务的定义。

```java
public interface TransactionDefinition {
    // 传播机制
    int PROPAGATION_REQUIRED = 0;
    int PROPAGATION_SUPPORTS = 1;
    int PROPAGATION_MANDATORY = 2;
    int PROPAGATION_REQUIRES_NEW = 3;
    int PROPAGATION_NOT_SUPPORTED = 4;
    int PROPAGATION_NEVER = 5;
    int PROPAGATION_NESTED = 6;
    // 隔离级别
    int ISOLATION_DEFAULT = -1;
    int ISOLATION_READ_UNCOMMITTED = 1;
    int ISOLATION_READ_COMMITTED = 2;
    int  = 4;
    int ISOLATION_SERIALIZABLE = 8;
    int TIMEOUT_DEFAULT = -1;
    int getPropagationBehavior();
    // 获取隔离级别
    int getIsolationLevel();
    int getTimeout();
    boolean isReadOnly();
    @Nullable
    String getName();
}
```

### 2.3 `TransactionStatus.java`

事务的状态。

```java
public interface TransactionStatus extends SavepointManager, Flushable {
    boolean isNewTransaction();
    boolean hasSavepoint();
    void setRollbackOnly();
    boolean isRollbackOnly();
    void flush();
    boolean isCompleted();
}
```

## 三 `Spring` 事务使用实践

> 假设我要完成一个功能，当删除用户的时候，将与该用户有关的数据行都删除。即要么同时成功，要么同时失败。

### 3.1 项目准备

新建用户表 `user` 及用户信息表 `user_info`，并插入两条数据。

```sql
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_name` varchar(100) NOT NULL COMMENT '账户名称',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_update` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户表';

INSERT INTO `user` (`id`, `user_name`, `gmt_create`, `gmt_update`)
VALUES
	(1,'Van','2020-05-09 18:09:16',NULL),
	(2,'VanFan','2020-05-10 17:45:25',NULL);

DROP TABLE IF EXISTS `user_info`;

CREATE TABLE `user_info` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `pass_word` varchar(100) NOT NULL COMMENT '登录密码',
  `nick_name` varchar(30) NOT NULL COMMENT '昵称',
  `mobile` varchar(30) NOT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_update` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

INSERT INTO `user_info` (`id`, `user_id`, `pass_word`, `nick_name`, `mobile`, `email`, `gmt_create`, `gmt_update`)
VALUES
	(1,1,'password','风尘博客','12580','110@qq.com','2020-02-09 18:05:27',NULL),
	(2,2,'password','公众号','110','12580@qq.com','2020-02-10 17:45:45',NULL);
```	

`Mapper` 及其映射文件详见文末源码。

### 3.2 删除用户：不启用事物


```java
@Test
public void delUserWithoutTransaction() {
    userInfoMapper.deleteUser(1L);
    if (true) {
        throw new RuntimeException("自造一个异常");
    }
    userInfoMapper.deleteUserInfo(1L);
}
```

执行测试方法，我们发现：`user` 表 `id = 1` 的用户的数据删除了，但是在发生异常后，`user_info` 表的用户数据未删除，很明显是不符合要求的。

### 3.2 删除用户：事务注解

```java
@Test
@Transactional
public void delUserWithAnnotationTransaction() {
    userInfoMapper.deleteUser(2L);
    if (true) {
        throw new RuntimeException("自造一个异常");
    }
    userInfoMapper.deleteUserInfo(2L);
}
```

代码跟上面完全一样，只是加了事务注解 `@Transactional` ，开启注解方式的事务后，执行发现：两个表中的 `id = 2` 的用户数据都删除失败，说明事务生效。

### 3.3 删除用户：手动开启

除了注解方式，我们还可以手动启用事务，如下：

```java
@Test
public void delUserWithTransaction() {
    boolean executeRet = transactionTemplate.execute(transactionStatus -> {
        userInfoMapper.deleteUser(2L);
        if (true) {
            transactionStatus.setRollbackOnly();
            throw new RuntimeException("自造一个异常");
        }
        userInfoMapper.deleteUserInfo(2L);
        return true;
    });
}
```

## 四、总结

对于项目中需要使用到事务的地方，建议不要盲目使用 `Spring` 事务注解，尽量手动开启。

[Spring 事务完整示例代码](https://github.com/vanDusty/Frame-Home/tree/master/spring-case/transaction-demo)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。
