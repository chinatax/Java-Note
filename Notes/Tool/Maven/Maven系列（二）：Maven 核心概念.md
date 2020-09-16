# Maven系列（二）：Maven 核心概念

## 一、仓库

### 1.1 `Maven` 仓库

　　仓库是一个位置(`place`)，可以存储所有的工程 `jar` 文件、`library jar` 文 件、插件或任何其他的工程指定的文件。

严格意义上说，`Maven` 只有两种类型的仓库:

- 本地(`local`)
- 远程(`remote`)


### 1.2 本地仓库

> `Maven` 的本地仓库是机器上的一个文件夹。它在你第一次运行任何 `Maven` 命令的时候创建。

`Maven` 的本地仓库保存你的工程的所有依赖(`library jar`、`plugin jar` 等)。当你运行一次 `Maven` 构建时，`Maven` 会自动下载所有依赖的 `jar` 文件到本地仓库中。它避免了每次构建时都引用存放在远程仓库上的依赖文件。

`Maven` 的本地仓库默认被创建在 `${user.home}/.m2/repository`目录下。要修改默认位置，只要在 `settings.xml` 文件中定义另一个路径即可，例如：

```xml
<localRepository>
	/anotherDirectory/.m2/respository
</localRepository>
```


### 1.3 远程仓库

`Maven` 的远程仓库可以是任何其他类型的存储库，可通过各种协议，例如 `file://`和 `http://`来访问。

这些存储库可以是由第三方提供的可供下载的远程仓库，也可以是在公司内的 `FTP` 服务器或 `HTTP` 服务器上设置的内部存储库，用于在开发团队和发布之间共享私有的 `artifacts`。

* 中央仓库

	`Maven` 的中央仓库是 `Maven` 社区维护的，里面包含了大量常用的库，我们可以直接引用，是一个远程公用仓库，`URL` 地址：[http://search.maven.org/](http://search.maven.org/)

- 第三方仓库

	也叫私服，是指公司自己内部搭建的公共类库站点，只提供给公司内部共享服务所使用，通常都是搭建在局域网内部使用，而且对于内部私服的连接，通常公司都会有相关的账号密码进行控制。

![](https://img.dusty.vip/Note/20200903132318.png)

### 1.4 仓库之间的关系

> 前面介绍了三种仓库，那这些仓库之间的关系是怎样的呢？或者说一个 `Maven` 项目想要获取一个`jar`包的话，他该从哪个仓库中去获取呢？

![](https://img.dusty.vip/Note/20200904172245.png)

1. 首先 `Maven`  会到本地仓库中去寻找所需要的`jar`包；
1. 如果找不到就会到配置的私有仓库中去找；
1. 如果私有仓库中也找不到的话，就会到配置的中央仓库中去找；
1. 如果还是找不到就会报错。

但是这中间只要在某一个仓库中找到了就会返回了，除非仓库中有更新的版本，或者是`snapshot`版本。

### 1.5 仓库配置

假设我们要配置一个中央仓库，可以像下面这样配置：

```xml
<project>
	<profiles>
		<profile>
			<id>central</id>
  			<repositories>
  				<repository>
  					<id>Central</id>
					<name>Central</name>
					<url>http://repo.maven.apache.org/maven2/</url>
				</repository>
			</repositories>
		</profile>
	</profiles>

	<activeProfiles>
		<activeProfile>central</activeProfile>
	</activeProfiles>
	...
</project>
```

### 1.6 仓库管理器

> 如果每个开发者都单独配置一个中央仓库，那每个人都到中央仓库中去下载所需的 `jar`，这就退化成最原始的模式，并且是一个巨大的资源浪费。

仓库管理器是一种专用服务器应用程序，目的是用来管理二进制组件的存储库。对于任何使用 `Maven` 的项目，仓库管理器的使用被认为是必不可少的最佳实践。

仓库管理器提供了以下基本用途：

1. 充当中央 `Maven` 存储库的专用代理服务器；
1. 提供存储库作为 `Maven` 项目输出的部署目标；

使用仓库管理器可以获得以下优点和功能：

1. 显著减少了远程存储库的下载次数，节省了时间和带宽，从而提高了构建性能；
1. 由于减少了对外部存储库的依赖，提高了构建稳定性；
1. 与远程 `SNAPSHOT` 存储库交互的性能提高；
1. 提供了一个有效的平台，用于在组织内外交换二进制工件，而无需从源代码中构建工件。



### 二、镜像

   镜像(`Mirror`) 相当于一个代理，它会拦截去指定的远程仓库下载构件的请求，然后从自己这里找出构件回送给客户端。配置镜像的目的一般是出于网速考虑。

> 仓库和镜像是两个不同的概念：前者本身是一个仓库，可以对外提供服务，而后者本身并不是一个仓库，它只是远程仓库的网络加速器。

如果仓库`X` 可以提供 仓库`Y` 存储的所有内容，那么就可以认为 `X`是`Y`的一个镜像。这也意味着，任何一个可以从某个仓库中获得的构件，都可以从它的镜像中获取。

> 举个例子：[http://maven.net.cn/content/groups/public/](http://maven.net.cn/content/groups/public/) 是中央仓库 [http://repo1.maven.org/maven2/](http://repo1.maven.org/maven2/) 在中国的镜像，由于地理位置的因素，该镜像往往能够提供比中央仓库更快的服务。

因此，可以在 `Maven` 中配置该镜像来替代中央仓库。在`settings.xml`中配置如下代码：

```xml
<settings>
      ...
    <mirrors>
        <mirror>
            <id>maven.net.cn</id>
            <mirrorOf>central</mirrorOf>
            <name>one of the central mirrors in china</name>
            <url>http://maven.net.cn/content/groups/public/</url>
        </mirror>
    </mirrors>
      ...
</settings>
```

![](https://img.dusty.vip/Note/20200904173454.jpg)

对于镜像的最佳实践是结合私服。由于私服可以代理任何外部的公共仓库(包括中央仓库)，因此，对于组织内部的 `Maven`用户来说，使用一个私服地址就等于使用了所有需要的外部仓库，这可以将配置集中到私服，从而简化 `Maven` 本身的配置。在这种情况下，任何需要的构件都可以从私服获得，私服就是所有仓库的镜像。

## 三、`Maven` 坐标

### 3.1 `pom.xml`

　　`Project Object Model`：项目对象模型。它是 `Maven` 的核心配置文件，所有的构建的配置都在这里设置。

### 3.2 `Maven` 坐标

使用`groupId`、`artifactId`、`version` 三个向量在仓库中唯一的定位一个 `Maven` 工程。

*   `groupId`：组织标识(包名)
*   `artifactId`：项目名称
*   `version`：项目的当前版本

示例：

![](https://img.dusty.vip/Note/20200904154357.png)

### 3.3 `Maven` 为什么使用坐标？

*   `Maven` 世界拥有大量构建，我们需要找一个用来唯一标识一个构建的统一规范；
*   拥有了统一规范，就可以把查找工作交给机器。


## 四、依赖管理

`Maven` 核心特点之一是依赖管理。一旦我们开始处理多模块工程(包含数百个子模块或者子工程)的时候，模块间的依赖关系就变得非常复杂，管理也变得很困难。针对此种情形，`Maven` 提供了一种高度控制的方法。

### 4.1 依赖配置

　　依赖配置主要包含如下元素：

```xml
    <!--添加依赖配置-->
    <dependencies>
        <!--项目要使用到junit的jar包，所以在这里添加junit的jar包的依赖-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.9</version>
            <scope>test</scope>
        </dependency>
        <!--项目要使用到maven的jar包，所以在这里添加maven的jar包的依赖-->
        <dependency>
            <groupId>cn.van</groupId>
            <artifactId>maven</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```

* `Maven` 解析依赖信息时会到本地仓库中取查找被依赖的 `jar` 包；

    1. 对于本地仓库中没有的会去中央仓库去查找 `Maven` 坐标来获取 `jar` 包，获取到 `jar` 之后会下载到本地仓库；
    2. 对于中央仓库也找不到依赖的 `jar` 包的时候，就会编译失败了。

* 如果依赖的是自己或者团队开发的 `Maven` 工程，需要先使用 `install` 命令把被依赖的 `Maven` 工程的 `jar` 包导入到本地仓库中

    举例：现在我再创建第二个 `Maven` 工程 `hello-word`，其中用到了第一个 `maven-demo` 工程里类的 `sayHello(String name)` 方法，我们在给 `hello-word` 项目使用 `mvn compile` 命令进行编译的时候，会提示缺少依赖 `maven-demo` 的 `jar` 包。怎么办呢？

　　到第一个 `Maven` 工程中执行 `mvn install` 后，你再去看一下本地仓库，你会发现有了 `maven-demo` 项目的 `jar` 包，一旦本地仓库有了`maven-demo` 工程的 `jar` 包后，你再到 `hello-word` 项目中使用 `mvn compile` 命令的时候，可以成功编译

### 4.2 依赖范围

　　**依赖范围 `scope` 用来控制依赖和编译，测试，运行的 `classpath` 的关系**. 主要的是三种依赖关系如下：
　　　　
1.`compile`： **默认编译依赖范围**。适用于所有阶段（开发、测试、部署、运行），本`jar`会一直存在所有阶段；
1.`provided`：只在开发、测试阶段使用，但对于运行无效；
1.`runtime`: 只在运行时使用。例如: `jdbc` 驱动；
1.`test`：只在测试时使用，用于编译和运行测试代码。不会随项目发布。

### 4.3 传递性依赖

　　`Apple.jar` 直接依赖于 `Vegetables.jar`，而 `Vegetables.jar` 又直接依赖于 `Food.jar`，那么 `Apple.jar` 也依赖于 `Food.jar`，这就是传递性依赖，只不过这种依赖是间接依赖，如下图所示：

   ![](https://img.dusty.vip/Note/20200903113048.png)

### 4.4 依赖冲突

   当同一个项目中由于不同的`jar`包依赖了相同的`jar`包，此时就会发生依赖冲突的情况，如图所示：

![](https://img.dusty.vip/Note/20200904173711.png)

   当项目中依赖了`a`和`c`，而`a`和`c`都依赖了`b`，这时就造成了冲突。为了避免冲突的产生，`Maven` 使用了两种策略来解决冲突，分别是**路径最短者优先原则**和**路径相同先声明优先原则** 。

![](https://img.dusty.vip/Note/20200904173711.png)

* 路径最短者优先原则

![](https://img.dusty.vip/Note/20200904174427.jpg)

   从项目一直到最终依赖的`jar`的距离，哪个距离短就依赖哪个，距离长的将被忽略掉。

*  路径相同先声明优先原则

![](https://img.dusty.vip/Note/20200904174442.jpg)

   通过`jar`包声明的顺序来决定使用哪个，最先声明的`jar`包总是被选中，后声明的`jar`包则会被忽略。

### 4.5 依赖排除

   如果我们只想引用我们直接依赖的`jar`包，而不想把间接依赖的`jar`包也引入的话，那可以使用依赖排除的方式，将间接引用的`jar`包排除掉。

```xml
<exclusions>
    <exclusion>
        <groupId>excluded.groupId</groupId>
        <artifactId>excluded-artifactId</artifactId>
    </exclusion>
</exclusions>
```

### 4.6 聚合


```xml
    <modules>
        <module>模块一</module>
        <module>模块二</module>
        <module>模块三</module>
    </modules>
```

将多个项目同时运行就称为聚合，如下就是 `apple`、`orange`、`peal` 这三个模块聚合。

```xml
    <modules>
        <module>apple</module>
        <module>orange</module>
        <module>peal</module>
    </modules>
```

聚合的优势在于可以在一个地方编译多个 `pom.xml` 文件。

**聚合时 `packaging` 必须要是 `pom`**

### 4.7 继承

　　继承为了消除重复，我们把很多相同的配置提取出来。

* 继承配置代码(父`pom`中的依赖)

```
    <groupId>cn.van</groupId>
    <artifactId>maven-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>${junit.version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

* 继承代码中定义属性(在子 `pom` 中引入这个父 `pom`)

```
    <!-- 指定parent，说明是从哪个pom继承 -->
    <parent>
        <groupId>cn.van</groupId>
        <artifactId>maven-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <!-- 只需要指明groupId + artifactId，就可以到父pom找到了，无需指明版本 -->
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
    </dependencies>
```


* 用 `dependencyManagement` ，可对依赖进行管理

这样的好处是子模块可以有选择行的继承，而不需要全部继承。

> 子类只要不引用这个里面写的`groupId + artifactId`，则不会添加依赖。

- 聚合与继承的关系

　　聚合主要为了快速构建项目，继承主要为了消除重复
　　
　　
## 五、生命周期

　　`Maven` 生命周期就是为了对所有的构建过程进行抽象和统一，包括项目清理，初始化，编译，打包，测试，部署等几乎所有构建步骤。

### 5.1 `Maven` 三大生命周期

　　`Maven` 有三套相互独立的生命周期，请注意这里说的是 **三套**，而且 **相互独立**，这三套生命周期分别是：

1. `Clean Lifecycle`：在进行真正的构建之前进行一些清理工作；
2. `Default Lifecycle`：构建的核心部分，编译，测试，打包，部署等等；
3. `Site Lifecycle`：生成项目报告，站点，发布站点。

　　再次强调一下它们是相互独立的，你可以仅仅调用 `clean` 来清理工作目录，仅仅调用 `site` 来生成站点。当然你也可以直接运行 `mvn clean install site` 运行所有这三套生命周期。
　　
### 5.2 `Clean Lifecycle` 生命周期

    每套生命周期都由一组阶段 (`Phase`) 组成，我们平时在命令行输入的命令总会对应于一个特定的阶段。`Clean Lifecycle` 生命周期一共包含了三个阶段：

    1. `pre-clean`：执行一些需要在 `clean` 之前完成的工作；
    2. `clean`：移除所有上一次构建生成的文件；
    3. `post-clean`：执行一些需要在 `clean` 之后立刻完成的工作。

　　`mvn clean` 中的 `clean` 就是上面的 `clean`，在一个生命周期中，运行某个阶段的时候，它之前的所有阶段都会被运行，也就是说，`mvn clean` 等同于 `mvn pre-clean clean` ，如果我们运行 `mvn post-clean` ，那么 `pre-clean`，`clean` 都会被运行。这是 `Maven` 很重要的一个规则，可以大大简化命令行的输入。

　　
### 5.3 `Default Lifecycle` 生命周期

    `Default Lifecycle` 生命周期是 `Maven` 生命周期中最重要的一个，绝大部分工作都发生在这个生命周期中：编译，测试，打包，部署等等。这里，只解释一些比较重要和常用的阶段：

    *   `validate`
    *   `generate-sources`
    *   `process-sources`
    *   `generate-resources`
    *   `process-resources`：复制并处理资源文件，至目标目录，准备打包。
    *   `compile`：编译项目的源代码。
    *   `process-classes`
    *   `generate-test-sources`
    *   `process-test-sources`
    *   `generate-test-resources`
    *   `process-test-resources`：复制并处理资源文件，至目标测试目录。
    *   `test-compile`：编译测试源代码。
    *   `process-test-classes`
    *   `test`：使用合适的单元测试框架运行测试。这些测试代码不会被打包或部署。
    *   `prepare-package`
    *   `package`：接受编译好的代码，打包成可发布的格式，如 JAR 。
    *   `pre-integration-test`
    *   `integration-test`
    *   `post-integration-test`
    *   `verify`
    *   `install`：将包安装至本地仓库，以让其它项目依赖。
    *   `deploy`：将最终的包复制到远程的仓库，以让其它开发人员与项目共享。

　　运行任何一个阶段的时候，它前面的所有阶段都会被运行，这也就是为什么我们运行 `mvn install` 的时候，代码会被编译，测试，打包。此外，`Maven` 的插件机制是完全依赖 `Maven` 的生命周期的，因此理解生命周期至关重要。

### 5.4 `Site Lifecycle` 生命周期

    该周期生成项目报告，站点，发布站点。

    1. `pre-site` 执行一些需要在生成站点文档之前完成的工作；
    1. `site`：生成项目的站点文档；
    1. `post-site`：执行一些需要在生成站点文档之后完成的工作，并且为部署做准备；
    1. `site-deploy`：将生成的站点文档部署到特定的服务器上。

　　这里经常用到的是 `site` 阶段和 `site-deploy` 阶段，用以生成和发布 `Maven` 站点，这是 `Maven` 相当强大的功能。

## 六 `Maven` 插件

插件是 `Maven` 的核心，所有执行的操作都是基于插件来完成的。

为了让一个插件中可以实现众多的相类似的功能，`Maven` 为插件设定了目标，一个插件中有可能有多个目标。其实生命周期中的每个阶段都是由插件的一个具体目标来执行的。

`Maven` 的生命周期与插件目标相互绑定，以完成某个具体的构建任务，比如通过 `mybatis-generator ` 我们可以生成很多`DAO`层的代码。

## 七、指令

### 7.1 构建 `Maven` 项目

　　`Maven`作为一个高度自动化构建工具，本身提供了构建项目的功能，下面就来体验一下使用 `Maven` 构建项目的过程。

- `Maven` 项目的目录约定

```xml
MavenProjectRoot(项目根目录)
   |----src
   |     |----main
   |     |         |----java ——存放项目的. java 文件
   |     |         |----resources ——存放项目资源文件，如 spring, mybatis 配置文件
   |     |----test
   |     |         |----java ——存放所有测试. java 文件，如 JUnit 测试类
   |     |         |----resources ——存放项目资源文件，如 spring, mybatis 配置文件
   |----target ——项目输出位置
   |----pom.xml ---- 用于标识该项目是一个 Maven 项目

```

* 使用`mvn archetype:generate`命令

`mvn archetype:generate -DgroupId=cn.van -DartifactId=maven-demo -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`

* 使用`mvn archetype:create`命令

`mvn archetype:create -DgroupId=cn.van -DartifactId=maven-demo -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`

使用`mvn archetype:generate`命令和`mvn archetype:create`都可以创建项目，区别就是使用使用`mvn archetype:create`命令创建项目更快。

![](https://img.dusty.vip/Note/20200901175628.png)

- `Maven` 创建项目的命令说明

> `mvn archetype:create`或者`mvn archetype:generate`是固定写法

```
　　-DgroupId　　　　　　　　 组织标识（包名）
　　-DartifactId　　　　　　  项目名称
　　-DarchetypeArtifactId　  指定ArchetypeId：maven-archetype-quickstart，创建一个Java Project；maven-archetype-webapp，创建一个Web Project
　　-DinteractiveMode　　　　是否使用交互模式
```

　　1. `archetype`是`mvn`内置的一个插件；
    1. `create`任务可以创建一个`java`项目骨架；
    1. `DgroupId`是软件包的名称；
    1. `DartifactId`是项目名；
    1. `DarchetypeArtifactId`是可用的`mvn`项目骨架，目前可以使用的骨架有：

   * maven-archetype-archetype
   * maven-archetype-j2ee-simple
   * maven-archetype-mojo
   * maven-archetype-portlet
   * maven-archetype-profiles (currently under development)
   * maven-archetype-quickstart
   * maven-archetype-simple (currently under development)
   * maven-archetype-site
   * maven-archetype-site-simple
   * maven-archetype-webapp

　　每一个骨架都会建相应的目录结构和一些通用文件，最常用的是`maven-archetype-quickstart`(创建一个`Java Project`)和`maven-archetype-webapp`（创建一个`JavaWeb Project`）骨架。

### 7.2 使用 `Maven` 编译项目

　　在项目创建完成后，一般来说，接着我们就可以将创建好的项目导入`IDEA`/`Eclipse`中进行开发了，我们这里直接跳过，直接假设开发完成，进行编译/打包等流程。

> 编译项目的命令是：`mvn compile`

　　* 进入项目根目录执行`mvn compile` 命令编译项目；

　　* 编译成功之后，可以看到项目的根目录下多了一个`target`文件夹，这个文件夹就是编译成功之后 `Maven` 帮我们生成的文件夹；

　　* 打开`target`文件夹，可以看到里面有一个`classes`文件夹，`classes`文件夹中存放的就是 `Maven` 我们编译好的字节码文件。
　　
![](https://img.dusty.vip/Note/20200901191230.png)

这就是使用 `Maven` 自动编译项目的过程。编译完成，你需要的的依赖的包已经自动导入到本地仓库了。

### 7.3 使用 `Maven` 清理项目

> 清理项目的命令是：`mvn clean`

 进入项目根目录执行`mvn clean`命令清理项目，清理项目的过程就是把执行`mvn compile`命令编译项目时生成的 `target` 文件夹删掉。

### 7.4 使用 `Maven` 测试项目

> 测试项目的命令是：`mvn test`

　　* 进入项目根目录执行 `mvn test` 命令测试项目；

　　* 测试成功之后，可以看到项目的根目录下多了一个`target`文件夹，这个文件夹就是测试成功之后 `Maven` 帮我们生成的文件夹；

　　* 打开`target`文件夹，可以看到里面有一个`classes`和`test-classes`文件夹，如下图所示：

![](https://img.dusty.vip/Note/20200901191438.png)

　　也就是说，我们执行执行 `mvn test`命令测试项目时，`Maven` 先帮我们编译项目，然后再执行测试代码。

### 7.5 使用 `Maven` 打包项目

> 打包项目的命令是：`mvn package`

   * 进入项目根目录执行 `mvn package` 命令测试项目；

   * 打包成功之后，可以看到项目的根目录下的`target`文件夹中多了一个 `maven-demo-1.0-SNAPSHOT.jar`，这个 `maven-demo-1.0-SNAPSHOT.jar` 就是打包成功之后 `Maven` 帮我们生成的 `jar` 文件，如下图所示：

![](https://img.dusty.vip/Note/20200901191708.png)

### 7.6 使用 `Maven` 安装项目

> 安装项目的命令是：`mvn install`

   * 进入项目根目录执行 `mvn install` 命令测试项目；

   * 安装成功之后，首先会在项目的根目录下生成`target`文件夹，打开`target`文件夹，可以看到里面会有 `maven-demo-1.0-SNAPSHOT.jar`，这个 `maven-demo-1.0-SNAPSHOT.jar` 就是安装成功之后 `Maven` 帮我们生成的 `jar` 文件，如下图所示：

   * 除此之外，在我们存放 `Maven` 下载下来的 `jar` 包的仓库也会有一个 `maven-demo-1.0-SNAPSHOT.jar`，所以 `Maven` 安装项目的过程，实际上就是把项目进行【清理】→【编译】→【测试】→【打包】，再把打包好的 `jar` 放到我们指定的存放 `jar` 包的 `Maven` 仓库中

　　所以使用 `mvn install` 命令，就把 `Maven` 构建项目的 **【清理】→【编译】→【测试】→【打包】** 的这几个过程都做了，同时将打包好的 `jar` 包发布到本地的 `Maven` 仓库中，所以 `Maven` 最常用的命令还是 `mvn install`，这个命令能够做的事情最多。




