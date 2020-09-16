# Maven系列（一）：Maven 入门

## 一、`Maven`

### 1.1 为什么使用 `Maven` 这样的构建工具?

* 一个项目就是一个工程

    如果项目非常庞大，就不适合使用 `package` 来划分模块，最好是每一个模块对应一个工程，利于分工协作。

> 借助于 `Maven` 就可以将一个项目拆分成多个工程

* 项目中使用 `jar` 包，需要 **复制**、**粘贴** 项目的 `lib` 中

    同样的 `jar` 包重复的出现在不同的项目工程中，你需要做不停的复制粘贴的重复工作。

> 借助于 `Maven`，可以将 `jar` 包保存在 **仓库** 中，不管在哪个项目只要使用引用即可就行。

* `jar` 包需要的时候每次都要自己准备好或到官网下载

> 借助于 `Maven` 我们可以使用统一的规范方式下载 `jar` 包，规范

* `jar` 包版本不一致的风险

    不同的项目在使用 `jar` 包的时候，有可能会导致各个项目的 `jar` 包版本不一致，导致未执行错误。

> 借助于 `Maven`，所有的 `jar` 包都放在 **仓库**  中，所有的项目都使用仓库的一份 `jar` 包。

* 一个 `jar` 包依赖其他的 `jar` 包需要自己手动的加入到项目中

　　`FileUpload` 组件 ->`IO` 组件，`commons-fileupload-1.3.jar` 依赖于 `commons-io-2.0.1.jar`

　　极大的浪费了我们导入包的时间成本，也极大的增加了学习成本。

> 借助于 `Maven`，它会自动的将依赖的 `jar` 包导入进来。

### 1.2 `Maven` 是什么？

`Maven`是跨平台的项目管理工具。主要服务于基于 `Java` 平台的**项目构建**、**依赖管理**和**项目信息管理**。

* 项目构建

　　项目构建过程包括

【清理项目】→【编译项目】→【测试项目】→【生成测试报告】→【打包项目】→【部署项目】，这六个骤就是一个项目的完整构建过程。

   ![](https://img.dusty.vip/Note/20200831170932.png)

　　理想的项目构建是高度自动化，跨平台，可重用的组件，标准化的，使用 `Maven` 就可以帮我们完成上述所说的项目构建过程。

* 依赖管理

　　依赖指的是 `jar` 包之间的相互依赖，比如我们搭建一个 `Spring` 的开发框架时，光光有 `spring-core-4.0.1.jar` 这个 `jar` 包是不行的，`spring-core-4.0.1.jar` 还依赖其它的 `jar` 包。依赖管理指的就是使用 `Maven` 来管理项目中使用到的 `jar` 包，`Maven` 管理的方式就是 *自动下载项目所需要的 `jar` 包，统一管理 `jar` 包之间的依赖关系*。

* 项目信息管理


## 二、`Maven` 下载及安装

### 2.1 `Maven` 下载

　　下载地址：[http://maven.apache.org/download.cgi](http://maven.apache.org/download.cgi)

### 2.2 `Maven` 安装

 * 首先要确保电脑上已经安装了 `JDK`，配置好 `JDK` 的环境变量，使用如下的两个命令检查**检查 `JDK` 安装的情况：

```linux
set java_home:查看JDK安装路径
java -version:查看JDK版本
```

　　![](https://img.dusty.vip/Note/20200831171711.png)

 * 解压

    下载完成后，得到一个压缩包如`apache-maven-3.6.3-bin.zip`，解压，放在一个非中文无空格的路径下，可以看到 `Maven` 的组成目录：

　　![](https://img.dusty.vip/Note/20200831173228.png)

　　`Maven` 目录分析

1. `bin`：含有 `mvn` 运行的脚本
1. `boot`：含有 `plexus-classworlds` 类加载器框架
1. `conf`：含有 `settings.xml` 配置文件
1. `lib`：含有 `Maven` 运行时所需要的 `Java` 类库
1. `LICENSE.txt`, `NOTICE.txt`, `README.txt` 针对 `Maven` 版本，第三方软件等简要介绍

 * 设置系统环境变量：`Maven_HOME`

　　![](https://img.dusty.vip/Note/20200831172338.png)

 * 设置环境变量 `Path`，将 `%Maven_HOME%\bin` 加入 `Path` 中

　　![](https://img.dusty.vip/Note/20200831172351.png)

 * 验证 `Maven` 安装是否成功**

　　打开终端，输入 `mvn  –v` 命令 查看 `Maven` 的相关信息，如下图所示：

　　![](https://img.dusty.vip/Note/20200831172507.png)

　　能够出现这样的信息就说明 `Maven` 的安装已经成功了。

入门篇暂且到这里结束吧，更多 `Maven` 相关，即将持续更新。