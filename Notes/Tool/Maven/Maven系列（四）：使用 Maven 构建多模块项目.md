# Maven系列（四）：使用 Maven 构建多模块项目

　　在平时的 Java web 项目开发中为了便于后期的维护，我们一般会进行分层开发，最常见的就是分为 domain（域模型层）、dao（数据库访问层）、service（业务逻辑层）、web（表现层），这样分层之后，各个层之间的职责会比较明确，后期维护起来也相对比较容易，今天我们就是使用 Maven 来构建以上的各个层。

　　项目结构如下：

```
　　system-parent  
    　　　　|----pom.xml  
    　　　　|----system-domain  
        　　　　　　　　|----pom.xml  
    　　　　|----system-dao  
        　　　　　　　　|----pom.xml  
    　　　　|----system-service  
        　　　　　　　　|----pom.xml  
    　　　　|----system-web  
        　　　　　　　　|----pom.xml
```

## 一、创建 system-parent 项目

 ****创建 system-parent，用来给各个子模块继承。****

　　进入命令行，输入以下命令：

```
mvn archetype:create -DgroupId=me.gacl -DartifactId=system-parent -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

```

　　如下图所示：


　　命令执行完成之后可以看到在当前目录 (C:\\Documents and Settings\\Administrator) 生成了 system-parent 目录，里面有一个 src 目录和一个 pom.xml 文件，如下图所示：


　　**将 src 文件夹删除**，然后修改 pom.xml 文件，将 **<packaging>jar</packaging>** 修改为 **<packaging>pom</packaging>**，pom 表示它是一个被继承的模块，修改后的内容如下：


```
 1 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 2   xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 3   <modelVersion>4.0.0</modelVersion>
 4 
 5   <groupId>me.gacl</groupId>
 6   <artifactId>system-parent</artifactId>
 7   <version>1.0-SNAPSHOT</version>
 8   <packaging>pom</packaging>
 9 
10   <name>system-parent</name>
11   <url>http://maven.apache.org</url>
12 
13   <properties>
14     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
15   </properties>
16 
17   <dependencies>
18     <dependency>
19       <groupId>junit</groupId>
20       <artifactId>junit</artifactId>
21       <version>3.8.1</version>
22       <scope>test</scope>
23     </dependency>
24   </dependencies>
25 </project>

```


## 二、**创建 sytem-domain 模块**


 在命令行进入创建好的 system-parent 目录，然后执行下列命令：

```
mvn archetype:create -DgroupId=me.gacl -DartifactId=system-domain -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

```

　　如下图所示：


　　命令执行完成之后可以看到在 system-parent 目录中生成了 system-domain，里面包含 src 目录和 pom.xml 文件。如下图所示：



　　同时，在 system-parent 目录中的 pom.xml 文件自动添加了如下内容：

```
<modules>
    <module>system-domain</module>
</modules>

```

　　这时，system-parent 的 pom.xml 文件如下：


```
 1 <?xml version="1.0" encoding="UTF-8"?>
 2 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 3   <modelVersion>4.0.0</modelVersion>
 4 
 5   <groupId>me.gacl</groupId>
 6   <artifactId>system-parent</artifactId>
 7   <version>1.0-SNAPSHOT</version>
 8   <packaging>pom</packaging>
 9 
10   <name>system-parent</name>
11   <url>http://maven.apache.org</url>
12 
13   <properties>
14     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
15   </properties>
16 
17   <dependencies>
18     <dependency>
19       <groupId>junit</groupId>
20       <artifactId>junit</artifactId>
21       <version>3.8.1</version>
22       <scope>test</scope>
23     </dependency>
24   </dependencies>
25   <modules>
26     <module>system-domain</module>
27   </modules>
28 </project>

```


　　修改 system-domain 目录中的 pom.xml 文件，把 **<groupId>me.gacl</groupId>** 和 **<version>1.0-SNAPSHOT</version>** 去掉，加上 **<packaging>jar</packaging>**，因为 groupId 和 version 会继承 system-parent 中的 groupId 和 version，packaging 设置打包方式为 jar

　　修改过后的 pom.xml 文件如下：


```
 1 <?xml version="1.0"?>
 2 <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
 3     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 4   <modelVersion>4.0.0</modelVersion>
 5   <parent>
 6     <groupId>me.gacl</groupId>
 7     <artifactId>system-parent</artifactId>
 8     <version>1.0-SNAPSHOT</version>
 9   </parent>
10   
11   <artifactId>system-domain</artifactId>
12   <packaging>jar</packaging>
13   
14   <name>system-domain</name>
15   <url>http://maven.apache.org</url>
16 </project>

```


**三、**创建 system-dao 模块****
-------------------------

 在命令行进入创建好的 system-parent 目录，然后执行下列命令：

```
mvn archetype:create -DgroupId=me.gacl -DartifactId=system-dao -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

```

　　如下图所示：


　　命令执行完成之后可以看到在 system-parent 目录中生成了 system-dao，里面包含 src 目录和 pom.xml 文件。如下图所示：


　　同时，在 system-parent 目录中的 pom.xml 文件自动变成如下内容：


```
 1 <?xml version="1.0" encoding="UTF-8"?>
 2 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 3   <modelVersion>4.0.0</modelVersion>
 4 
 5   <groupId>me.gacl</groupId>
 6   <artifactId>system-parent</artifactId>
 7   <version>1.0-SNAPSHOT</version>
 8   <packaging>pom</packaging>
 9 
10   <name>system-parent</name>
11   <url>http://maven.apache.org</url>
12 
13   <properties>
14     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
15   </properties>
16 
17   <dependencies>
18     <dependency>
19       <groupId>junit</groupId>
20       <artifactId>junit</artifactId>
21       <version>3.8.1</version>
22       <scope>test</scope>
23     </dependency>
24   </dependencies>
25   <modules>
26     <module>system-domain</module>
27     <module>system-dao</module>
28   </modules>
29 </project>

```


　　修改 system-dao 目录中的 pom.xml 文件，，把 **<groupId>me.gacl</groupId>** 和 **<version>1.0-SNAPSHOT</version>** 去掉，加上 **<packaging>jar</packaging>**，因为 groupId 和 version 会继承 system-parent 中的 groupId 和 version，**packaging 设置打包方式为 jar，同时****添加对 system-domain 模块的依赖**，修改后的内容如下：


```
 1 <?xml version="1.0"?>
 2 <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
 3     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 4   <modelVersion>4.0.0</modelVersion>
 5   <parent>
 6     <groupId>me.gacl</groupId>
 7     <artifactId>system-parent</artifactId>
 8     <version>1.0-SNAPSHOT</version>
 9   </parent>
10 
11   <artifactId>system-dao</artifactId>
12   <packaging>jar</packaging>
13 
14   <name>system-dao</name>
15   <url>http://maven.apache.org</url>
16   <properties>
17     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
18   </properties>
19   <dependencies>
20     <!--system-dao需要使用到system-domain中的类，所以需要添加对system-domain模块的依赖-->
21      <dependency>
22       <groupId>me.gacl</groupId>
23       <artifactId>system-domain</artifactId>
24       <version>${project.version}</version>
25     </dependency>
26   </dependencies>
27 </project>

```


四、创建 system-service 模块
----------------------

　　在命令行进入创建好的 system-parent 目录，然后执行下列命令：

```
mvn archetype:create -DgroupId=me.gacl -DartifactId=system-service -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

```

　　如下图所示：


　　命令执行完成之后可以看到在 system-parent 目录中生成了 system-service，里面包含 src 目录和 pom.xml 文件。如下图所示：


　　同时，在 system-parent 目录中的 pom.xml 文件自动变成如下内容：


```
 1 <?xml version="1.0" encoding="UTF-8"?>
 2 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 3   <modelVersion>4.0.0</modelVersion>
 4 
 5   <groupId>me.gacl</groupId>
 6   <artifactId>system-parent</artifactId>
 7   <version>1.0-SNAPSHOT</version>
 8   <packaging>pom</packaging>
 9 
10   <name>system-parent</name>
11   <url>http://maven.apache.org</url>
12 
13   <properties>
14     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
15   </properties>
16 
17   <dependencies>
18     <dependency>
19       <groupId>junit</groupId>
20       <artifactId>junit</artifactId>
21       <version>3.8.1</version>
22       <scope>test</scope>
23     </dependency>
24   </dependencies>
25   <modules>
26     <module>system-domain</module>
27     <module>system-dao</module>
28     <module>system-service</module>
29   </modules>
30 </project>

```


　　修改 system-service 目录中的 pom.xml 文件，，把 **<groupId>me.gacl</groupId>** 和 **<version>1.0-SNAPSHOT</version>** 去掉，加上 **<packaging>jar</packaging>**，因为 groupId 和 version 会继承 system-parent 中的 groupId 和 version，**packaging 设置打包方式为 jar，同时****添加对 system-dao 模块的依赖**，system-service 依赖 system-dao 和 system-domain，但是我们只需添加 system-dao 的依赖即可，因为 system-dao 已经依赖了 system-domain。修改后的内容如下：


```
 1 <?xml version="1.0"?>
 2 <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
 3     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 4   <modelVersion>4.0.0</modelVersion>
 5   <parent>
 6     <groupId>me.gacl</groupId>
 7     <artifactId>system-parent</artifactId>
 8     <version>1.0-SNAPSHOT</version>
 9   </parent>
10 
11   <artifactId>system-service</artifactId>
12   <packaging>jar</packaging>
13   
14   <name>system-service</name>
15   <url>http://maven.apache.org</url>
16   <properties>
17     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
18   </properties>
19   <dependencies>
20     <!--
21     system-service依赖system-dao和system-domain，
22     但是我们只需添加system-dao的依赖即可，因为system-dao已经依赖了system-domain
23     -->
24      <dependency>
25       <groupId>me.gacl</groupId>
26       <artifactId>system-dao</artifactId>
27       <version>${project.version}</version>
28     </dependency>
29   </dependencies>
30 </project>

```


五、创建 system-web 模块
------------------

　　在命令行进入创建好的 system-parent 目录，然后执行下列命令：

```
mvn archetype:create -DgroupId=me.gacl -DartifactId=system-web -DarchetypeArtifactId=maven-archetype-webapp -DinteractiveMode=false

```

　　如下图所示：


　　命令执行完成之后可以看到在 system-parent 目录中生成了 system-web，里面包含 src 目录和 pom.xml 文件。如下图所示：


　　在 \\ system-web\\src\\main\\webapp 目录中还生成了一个简单的 index.jsp, 如下图所示：


　　里面的内容为

```
<html>
<body>
<h2>Hello World!</h2>
</body>
</html>

```

　　system-web\\src\\main\\webapp\\WEB-INF 目录中生成了 web.xml


　　同时，在 system-parent 目录中的 pom.xml 文件自动变成如下内容：


```
 1 <?xml version="1.0" encoding="UTF-8"?>
 2 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 3   <modelVersion>4.0.0</modelVersion>
 4 
 5   <groupId>me.gacl</groupId>
 6   <artifactId>system-parent</artifactId>
 7   <version>1.0-SNAPSHOT</version>
 8   <packaging>pom</packaging>
 9 
10   <name>system-parent</name>
11   <url>http://maven.apache.org</url>
12 
13   <properties>
14     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
15   </properties>
16 
17   <dependencies>
18     <dependency>
19       <groupId>junit</groupId>
20       <artifactId>junit</artifactId>
21       <version>3.8.1</version>
22       <scope>test</scope>
23     </dependency>
24   </dependencies>
25   <modules>
26     <module>system-domain</module>
27     <module>system-dao</module>
28     <module>system-service</module>
29     <module>system-web</module>
30   </modules>
31 </project>

```


　　修改 system-web 目录中的 pom.xml 文件，，把 **<groupId>me.gacl</groupId>** 和 **<version>1.0-SNAPSHOT</version>** 去掉，因为 groupId 和 version 会继承 system-parent 中的 groupId 和 version，**同时****添加对 system-service 模块的依赖**，修改后的内容如下：


```
 1 <?xml version="1.0"?>
 2 <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
 3     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 4   <modelVersion>4.0.0</modelVersion>
 5   <parent>
 6     <groupId>me.gacl</groupId>
 7     <artifactId>system-parent</artifactId>
 8     <version>1.0-SNAPSHOT</version>
 9   </parent>
10 
11   <artifactId>system-web</artifactId>
12   <packaging>war</packaging>
13   
14   <name>system-web Maven Webapp</name>
15   <url>http://maven.apache.org</url>
16   <dependencies>
17     <!--
18     system-web依赖system-service
19     -->
20      <dependency>
21       <groupId>me.gacl</groupId>
22       <artifactId>system-service</artifactId>
23       <version>${project.version}</version>
24     </dependency>
25   </dependencies>
26   <build>
27     <finalName>system-web</finalName>
28   </build>
29 </project>

```


　　 注意，**web 项目的打包方式是 war**。

六、编译运行项目
--------

 经过上面的五个步骤，相关的模块全部创建完成，怎么运行起来呢。由于最终运行的是 system-web 模块，所以我们对该模块添加 jetty 支持，方便测试运行。修改 system-web 项目的 pom.xml 如下：


```
 1 <?xml version="1.0"?>
 2 <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
 3     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 4   <modelVersion>4.0.0</modelVersion>
 5   <parent>
 6     <groupId>me.gacl</groupId>
 7     <artifactId>system-parent</artifactId>
 8     <version>1.0-SNAPSHOT</version>
 9   </parent>
10 
11   <artifactId>system-web</artifactId>
12   <packaging>war</packaging>
13   
14   <name>system-web Maven Webapp</name>
15   <url>http://maven.apache.org</url>
16   <dependencies>
17     <!--
18     system-web依赖system-service
19     -->
20      <dependency>
21       <groupId>me.gacl</groupId>
22       <artifactId>system-service</artifactId>
23       <version>${project.version}</version>
24     </dependency>
25   </dependencies>
26   <build>
27     <finalName>system-web</finalName>
28     <plugins>
29         <!--配置Jetty插件-->
30         <plugin>
31             <groupId>org.mortbay.jetty</groupId>
32             <artifactId>maven-jetty-plugin</artifactId>
33         </plugin>
34     </plugins>
35   </build>
36 </project>

```


　　在命令行进入 system-parent 目录，然后执行下列命令：

```
mvn clean install

```

　　如下图所示：


　　命令执行完后，在 system-web 目录下多出了 target 目录，里面有了 system-web.war，如下图所示：


　　命令行进入 sytem-web 目录，执行如下命令，启动 jetty

```
mvn jetty:run

```

　　如下图所示：



　　启动 jetty 服务器后，访问 http://localhost:8080/system-web/ 运行结果如下图所示：


七、导入 Eclipse 中进行开发
------------------

　　操作步骤如下所示：
