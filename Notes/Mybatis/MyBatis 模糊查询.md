# MyBatis 模糊查询

模糊查询也是数据库`SQL`中使用频率很高的`SQL`语句，使用`MyBatis`来进行更加灵活的模糊查询。

## 一、数据准备

### 1.1 `sql`

> 相见[mybatis-demo-like.sql](https://github.com/vanDusty/Mybatis-Home/blob/master/mybatis-case/mybatis-demo/file/mybatis-demo-like.sql)

```sql
DROP TABLE IF EXISTS `user_info_like`;
CREATE TABLE `user_info_like` (
                           `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                           `user_name` varchar(100) NOT NULL COMMENT '账户名称',
                           `pass_word` varchar(100) NOT NULL COMMENT '登录密码',
                           `nick_name` varchar(30) NOT NULL COMMENT '昵称',
                           `mobile` varchar(30) NOT NULL COMMENT '手机号',
                           `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址',
                           `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `gmt_update` timestamp NULL DEFAULT NULL COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT 'Mybatis like';

-- ----------------------------
-- Records of user_info_like
-- ----------------------------
BEGIN;
INSERT INTO `user_info_like` VALUES (1, 'Van', '123456', '风尘', '12580', NULL, '2020-02-01 14:52:12', NULL);
INSERT INTO `user_info_like` VALUES (2, 'zhangsan', '123456', '张三', '12580', NULL, '2020-01-02 14:52:12', NULL);
INSERT INTO `user_info_like` VALUES (3, 'lisi', '123456', '李四', '12580', NULL, '2020-02-01 14:52:12', NULL);
INSERT INTO `user_info_like` VALUES (4, 'wanger', '123456', '王二', '12580', NULL, '2020-02-02 15:48:34', NULL);
INSERT INTO `user_info_like` VALUES (5, 'wangwu', '123456', '王五', '12580', NULL, '2020-02-02 15:48:56', NULL);
COMMIT;
```


### 1.2 `UserInfoLikeDO.java`

```java
public class UserInfoLikeDO implements Serializable {
    private Long id;

    private String userName;

    private String passWord;

    private String nickName;

    private String mobile;

    private String email;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtUpdate;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtUpdate() {
        return gmtUpdate;
    }

    public void setGmtUpdate(LocalDateTime gmtUpdate) {
        this.gmtUpdate = gmtUpdate;
    }

    @Override
    public String toString() {
        return "UserInfoLikeDO{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", nickName='" + nickName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtUpdate=" + gmtUpdate +
                '}';
    }
}
```

## 二、模糊查询的四种方式

### 2.1 直接传参法

将要查询的关键字`keyword`,在代码中拼接好要查询的格式，如`%keyword%`,然后直接作为参数传入`mapper.xml`的映射文件中。

- 测试代码

```java
@Test
public void selectTest() {
    String nickName = "%" + "王" + "%";
    List<UserInfoLikeDO> list = userInfoLikeMapper.selectByKeyWord(nickName);
    log.info("UserList:{}", list);
}
```

- `Mapper` 接口

```java
List<UserInfoDO> selectByKeyWord(String nickName);
```

- `Mapper` 映射文件

```xml
  <select id="selectByKeyWord" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from user_info_like
    <where>
      <if test="nickName != '' and nickName != null">
        and nick_name like #{nickName}
      </if>
    </where>
  </select>
```

### 2.2 使用`${...}`代替`#{...}`

我们都知道，`${}`解析过来的参数值不带单引号，`#{}`解析传过来参数带单引号。


- 测试代码

```java
@Test
public void selectByWordTest() {
    String nickName ="王";
    List<UserInfoLikeDO> list = userInfoLikeMapper.selectByWord(nickName);
    log.info("UserList:{}", list);
}
```

- `Mapper` 接口

```java
List<UserInfoDO> selectByWord(String nickName);
```

- `Mapper` 映射文件

```xml
  <select id="selectByWord" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from user_info_like
    <where>
      <if test="nickName != '' and nickName != null">
        and nick_name like '%${nickName}%'
      </if>
    </where>
  </select>
```

### 2.3 `CONCAT（）`函数

`MySQL`的 `CONCAT()`函数用于将多个字符串连接成一个字符串，是最重要的`mysql`函数之一。


- 测试代码

```java
@Test
public void concatTest() {
    String nickName = "王";
    List<UserInfoLikeDO> list = userInfoLikeMapper.selectForConcat(nickName);
    log.info("UserList:{}", list);
}
```

- `Mapper` 接口

```java
List<UserInfoDO> selectForConcat(String nickName);
```

- `Mapper` 映射文件

```xml
  <select id="selectForConcat" resultMap="BaseResultMap">
    select
    <include refid="Base_Column_List" />
    from user_info_like
    <where>
      <if test="nickName != '' and nickName != null">
        and nick_name like CONCAT('%',#{nickName},'%')
      </if>
    </where>
  </select>
```

### 2.4 `Mybatis`的`bind`

`bind`:可以将`OGNL`表达式的值绑定到一个变量中，方便后来引用这个变量的值。

- 测试代码

```java
@Test
public void bindTest() {
    String nickName = "王";
    List<UserInfoLikeDO> list = userInfoLikeMapper.selectForBind(nickName);
    log.info("UserList:{}", list);
}
```

- `Mapper` 接口

```java
List<UserInfoDO> selectForBind(String nickName);
```

- `Mapper` 映射文件

```xml
  <select id="selectForBind" resultMap="BaseResultMap">
    <bind name="keyWord" value="'%' + nickName + '%'" />
    select
    <include refid="Base_Column_List" />
    from user_info_like
    <where>
      <if test="nickName != '' and nickName != null">
        and nick_name like #{keyWord}
      </if>
    </where>
  </select>
```

- 说明

文中省略了的`BaseResultMap`/`Base_Column_List`等，详见[UserInfoLikeMapper.xml](https://github.com/vanDusty/Mybatis-Home/blob/master/mybatis-case/mybatis-demo/src/main/resources/mapper/UserInfoLikeMapper.xml)

## 三、总结

1. 推荐使用`CONCAT（）`函数或者 `bind` 的方式；
1. 注意关键词中有`%`、`_`这些特殊字符如何处理。

[Github 示例代码](https://github.com/vanDusty/Frame-Home/blob/master/mybatis-case/mybatis-demo/src/test/java/cn/van/mybatis/demo/like/MybatisLikeTest.java)

> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。