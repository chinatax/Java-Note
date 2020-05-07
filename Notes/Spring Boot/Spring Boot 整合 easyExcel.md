# Spring Boot 整合 easyExcel
# 一、背景

通过`Java`读写`Excel`大概有以下几种：
`poi`/`csv`/`jxl`/`jxls`/`easyPoi`/`easyExcel`（本文重点）。

1. `easyExcel`基于注解的方式将以前`poi`的复杂的代码进模块抽离。我们基本上的需求只需要在`excelProperty`注解中就可以解决。
1. `easyExcel`最大的特点就是解决了内存泄漏的问题。以上几种`poi`在导出`Excel`的时候都受到了数据的影响.而且性能上还不是很好。`easyExcel`是`poi`系列产品的最佳之选

[easyExcel 官方文档](https://alibaba-easyexcel.github.io/quickstart/write.html)


# 二、读取 Excel 

## 2.1 项目准备

### 2.1.1 步骤

1. 创建`Excel`对应的实体对象(本文的`ImportModel.java`)；
2. 由于默认异步读取`Excel`，所以需要创建`Excel`一行一行的回调监听器（本文的`ExcelListener.java`）
3. 读取`Excel`。

### 2.1.2 项目依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- easy excel-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>easyexcel</artifactId>
        <version>1.1.1</version>
    </dependency>
    <!--Swagger-ui配置-->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>2.9.2</version>
    </dependency>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger-ui</artifactId>
        <version>2.9.2</version>
    </dependency>
    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>1.8.4</scope>
    </dependency>
    <!-- fastjson -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.60</version>
    </dependency>
</dependencies>
```
### 2.1.3 读取的`Excel`文本

本文使用的 [easyexcel-demo.xlsx](https://github.com/vanDusty/springboot-home/blob/master/springboot-demo-excel/file/easyexcel-demo.xlsx), 内容如下图

![](https://user-gold-cdn.xitu.io/2019/12/8/16ee5299f62dbcaf?w=638&h=1210&f=png&s=81731)

![](https://user-gold-cdn.xitu.io/2019/12/8/16ee527746b0010f?w=646&h=1230&f=png&s=81216)

### 2.1.4 读取对象模版

> 该类必须要继承自 `BaseRowModel.java`

```java
@Data
public class ImportModel extends BaseRowModel {

    /**
     * 通过 @ExcelProperty 的value 指定每个字段的列名称，index 为列的序号。
     */
    @ExcelProperty(value = "姓名", index = 0)
    private String studentName;

    @ExcelProperty(value = "年级", index = 1)
    private String grade;

    @ExcelProperty(value = "学科", index = 2)
    private String subject;

    @ExcelProperty(value = "分数", index = 3)
    private Integer fraction;

    public ImportModel() {

    }
}
```

## 2.2 核心实现

### 2.2.1 `Excel`监听器

默认异步读取`Excel`,`invoke` 方法逐行读取数据

```java
@Slf4j
public class ExcelListener extends AnalysisEventListener {

    /**
     * 自定义用于暂时存储data。
     */
    private List<Object> datas = new ArrayList<>();

    /**
     * 通过 AnalysisContext 对象还可以获取当前 sheet，当前行等数据
     */
    @Override
    public void invoke(Object data, AnalysisContext context) {
        log.info("解析到一条数据:{}", JSON.toJSONString(data));

        //数据存储到list，供批量处理，或后续自己业务逻辑处理。
        datas.add(data);
        //根据自己业务做处理（通用业务可以不需要该项）
        doSomething(data);
    }

    private void doSomething(Object object) {
        log.info("doSomething.....");
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！");
    }

    public List<Object> getDatas() {
        return datas;
    }

    public void setDatas(List<Object> datas) {
        this.datas = datas;
    }
}
```


### 2.2.2 读取 `Excel` 工具类`ImportExcelUtil.java`

- 返回 `ExcelReader`

> 检验`Excel`文件格式，支持`.xls`和`. xlsx`文件，其他文件格式会抛错。

```java
private static ExcelReader getReader(MultipartFile excel, ExcelListener excelListener) {
    String filename = excel.getOriginalFilename();
    if (filename == null) {
        throw new ExcelException("文件格式错误！");
    }
    if (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx")) {
        throw new ExcelException("文件格式错误！");
    }
    InputStream inputStream;
    try {
        inputStream = new BufferedInputStream(excel.getInputStream());
        return new ExcelReader(inputStream, null, excelListener, false);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}
```

- 读取整个`Excel`方法

> 如果`Excel` 内有多个 `sheet` 需要各个 `sheet` 字段相同

```java
public static List readExcel(MultipartFile excel, BaseRowModel baseRowModel) {
    ExcelListener excelListener = new ExcelListener();
    ExcelReader reader = getReader(excel, excelListener);
    if (reader == null) {
        return null;
    }
    for (Sheet sheet : reader.getSheets()) {
        if (baseRowModel != null) {
            sheet.setClazz(baseRowModel.getClass());
        }
        reader.read(sheet);
    }
    return excelListener.getDatas();
}
```

- 读取 `Excel` 的指定 `sheet` 指定数据方法

```java
public static List readExcel(MultipartFile excel, BaseRowModel baseRowModel,
                                         int sheetNo, int headLineNum) {
    ExcelListener excelListener = new ExcelListener();
    ExcelReader reader = getReader(excel, excelListener);
    if (reader == null) {
        return null;
    }
    reader.read(new Sheet(sheetNo, headLineNum, baseRowModel.getClass()));
    return excelListener.getDatas();
}
```

- 读取 `Excel` 的指定 `sheet` 全部数据

> 该方法其实为**读取 Excel 的指定 sheet 指定数据方法**中的指定行设为默认值`1`。

```java
public static List readExcel(MultipartFile excel, BaseRowModel baseRowModel, int sheetNo) {
    return readExcel(excel, baseRowModel, sheetNo, 1);
}
```

## 2.3 测试

> 这里只给出`Controller` 层接口代码，`Service`具体实现很简单，这里就略过了，详见文末地址代码示例。

### 2.3.1 读取整个`Excel`

- 接口

```java
@ApiOperation(value = "读取 整个Excel(多个 sheet 需要 各个 sheet 字段相同)")
@PostMapping(value = "/readAllExcel")
public List<ImportModel> readAllExcel(MultipartFile excel) {
    return readExcelService.readExcel(excel, new ImportModel());
}
```

- 返回结果

> 结果读取了全部数据

```xml
[
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "语文",
    "fraction": 130
  },
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "数学",
    "fraction": 140
  },
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "英语",
    "fraction": 125
  },
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "化学",
    "fraction": 90
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "语文",
    "fraction": 135
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "数学",
    "fraction": 125
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "英语",
    "fraction": 145
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "化学",
    "fraction": 88
  }
]
```

- 打印日志

```xml
2019-12-08 17:10:23.324  INFO 23855 --- [nio-8081-exec-1] o.a.c.c.C.[.[localhost].[/easyexcel]     : Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-12-08 17:10:23.324  INFO 23855 --- [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2019-12-08 17:10:23.335  INFO 23855 --- [nio-8081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 11 ms
2019-12-08 17:10:46.940  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":130,"grade":"高三","studentName":"张三","subject":"语文"}
2019-12-08 17:10:46.940  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.940  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":140,"grade":"高三","studentName":"张三","subject":"数学"}
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":125,"grade":"高三","studentName":"张三","subject":"英语"}
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":90,"grade":"高三","studentName":"张三","subject":"化学"}
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.941  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 所有数据解析完成！
2019-12-08 17:10:46.943  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":135,"grade":"高一","studentName":"李四","subject":"语文"}
2019-12-08 17:10:46.943  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.944  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":125,"grade":"高一","studentName":"李四","subject":"数学"}
2019-12-08 17:10:46.944  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.944  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":145,"grade":"高一","studentName":"李四","subject":"英语"}
2019-12-08 17:10:46.944  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.945  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":88,"grade":"高一","studentName":"李四","subject":"化学"}
2019-12-08 17:10:46.945  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:10:46.945  INFO 23855 --- [nio-8081-exec-1] c.v.easyexcel.export.util.ExcelListener  : 所有数据解析完成！
```

### 2.3.2 读取 `Excel` 的第二个 `sheet` 全部数据

- 接口

```java
@ApiImplicitParam( name = "sheetNo", value = "读第几个表单", required = true)
@ApiOperation(value = "读取 Excel 的指定 sheet 全部数据")
@PostMapping(value = "/readOneSheet")
public List<ImportModel> readOneSheet(MultipartFile excel,
                                      @RequestParam Integer sheetNo) {
    return readExcelService.readExcel(excel, new ImportModel(), sheetNo);
}
```

- 返回结果

> 只读取了第二页李四的数据

```xml
[
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "语文",
    "fraction": 135
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "数学",
    "fraction": 125
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "英语",
    "fraction": 145
  },
  {
    "cellStyleMap": {},
    "studentName": "李四",
    "grade": "高一",
    "subject": "化学",
    "fraction": 88
  }
]
```

- 打印日志

```xml
2019-12-08 17:13:00.384  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":135,"grade":"高一","studentName":"李四","subject":"语文"}
2019-12-08 17:13:00.384  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:13:00.384  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":125,"grade":"高一","studentName":"李四","subject":"数学"}
2019-12-08 17:13:00.384  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:13:00.385  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":145,"grade":"高一","studentName":"李四","subject":"英语"}
2019-12-08 17:13:00.385  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:13:00.385  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":88,"grade":"高一","studentName":"李四","subject":"化学"}
2019-12-08 17:13:00.385  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:13:00.386  INFO 23855 --- [nio-8081-exec-5] c.v.easyexcel.export.util.ExcelListener  : 所有数据解析完成！
```

### 2.3.3 读取`Excel`第一个`sheet`,从第二行开始读

- 接口

```java
@ApiImplicitParams({
        @ApiImplicitParam( name = "sheetNo", value = "读第几个表单", required = true),
        @ApiImplicitParam( name = "headLineNum", value = "从第几行数据开始读", required = true)
})
@ApiOperation(value = "读取 Excel 的指定 sheet 指定数据")
@PostMapping(value = "/readExcel")
public List<ImportModel> readExcel(MultipartFile excel,
                                   @RequestParam Integer sheetNo,
                                   @RequestParam Integer headLineNum) {
    return readExcelService.readExcel(excel, new ImportModel(), sheetNo,headLineNum);
}
```

- 返回结果

```xml
[
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "数学",
    "fraction": 140
  },
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "英语",
    "fraction": 125
  },
  {
    "cellStyleMap": {},
    "studentName": "张三",
    "grade": "高三",
    "subject": "化学",
    "fraction": 90
  }
]
```

- 打印日志

```xml
2019-12-08 17:16:12.337  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":140,"grade":"高三","studentName":"张三","subject":"数学"}
2019-12-08 17:16:12.339  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:16:12.342  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":125,"grade":"高三","studentName":"张三","subject":"英语"}
2019-12-08 17:16:12.345  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:16:12.346  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : 解析到一条数据:{"cellStyleMap":{},"fraction":90,"grade":"高三","studentName":"张三","subject":"化学"}
2019-12-08 17:16:12.346  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : doSomething.....
2019-12-08 17:16:12.346  INFO 23855 --- [nio-8081-exec-8] c.v.easyexcel.export.util.ExcelListener  : 所有数据解析完成！
```

# 三、导出 Excel

关于导出 `Excel` 文件，可以说是大多数服务中都需要集成的功能。那么，要如何优雅快速地去实现这个功能呢？


## 3.1 项目准备

### 3.1.1 步骤

1. 创建`Excel`导出对应的数据模型对象(本文的`ExportModel.java`)；
2. 将数据写入到数据模型对象中；
3. 导出`Excel`。

### 3.1.2 导出数据模型对象

> 该类必须也要继承自 `BaseRowModel.java`

```java
@Data
public class ExportModel extends BaseRowModel {

    /**
     * 通过 @ExcelProperty 的value 指定每个字段的列名称，index 为列的序号。
     */
    @ExcelProperty(value = "姓名", index = 0)
    private String studentName;

    @ExcelProperty(value = "年级", index = 1)
    private String grade;

    @ExcelProperty(value = "学科", index = 2)
    private String subject;

    @ExcelProperty(value = "分数", index = 3)
    private Integer fraction;

    public ExportModel() {

    }

    public ExportModel(String studentName, String grade, String subject, Integer fraction) {
        this.studentName = studentName;
        this.grade = grade;
        this.subject = subject;
        this.fraction = fraction;
    }
}
```

### 3.1.3 根据时间戳生产文件名

```java
private static String createFileName() {
    Long time = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    return sdf.format(new Date()) + time;
}
```

### 3.1.4 导出文件时为`Writer`生成`OutputStream`

```java
private static OutputStream getOutputStream(String fileName, HttpServletResponse response) {
    response.reset();
    response.setContentType("application/vnd.ms-excel");
    try {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName  + ".xlsx");
        return response.getOutputStream();
    } catch (IOException e) {
        throw new ExcelException("创建文件失败！");
    }
}
```

### 3.1.5 导出文件时为`File`生成`OutputStream`

```java
private static OutputStream getFileOutputStream(String fileName) {
    try {
        String filePath = fileName + ".xlsx";
        File dbfFile = new File(filePath);
        if (!dbfFile.exists() || dbfFile.isDirectory()) {
            dbfFile.createNewFile();
        }
        OutputStream out = new FileOutputStream(filePath);
        return out;
    } catch (Exception e) {
        throw new RuntimeException("创建文件失败！");
    }
}
```

### 3.1.6 造数据的方法

> 实际情况：从数据库查询动态数据。

```java
public static List<ExportModel> getList() {
    List<ExportModel> list = new ArrayList<>();
    ExportModel model1 = new ExportModel("张三", "高三", "语文", 130);
    ExportModel model2 = new ExportModel("张三", "高三", "数学", 140);
    ExportModel model3 = new ExportModel("张三", "高三", "英语", 125);
    ExportModel model4 = new ExportModel("张三", "高三", "化学", 90);
    list.add(model1);
    list.add(model2);
    list.add(model3);
    list.add(model4);
    return list;
}
public static List<ExportModel> getAnotherList() {
    List<ExportModel> list = new ArrayList<>();
    ExportModel model1 = new ExportModel("李四", "高二", "语文", 120);
    ExportModel model2 = new ExportModel("李四", "高二", "数学", 125);
    ExportModel model3 = new ExportModel("李四", "高二", "英语", 140);
    ExportModel model4 = new ExportModel("李四", "高二", "化学", 85);
    list.add(model1);
    list.add(model2);
    list.add(model3);
    list.add(model4);
    return list;
}
```

## 3.2 核心实现


### 3.2.1 导出的到一个 `sheet` 的 `Excel`

```java
public static void writeExcel(HttpServletResponse response, List<? extends BaseRowModel> list,
                                  String fileName, String sheetName, BaseRowModel object) {
    // WriteModel 是 写入 Excel 的数据模型对象
    ExcelWriter writer = new ExcelWriter(getOutputStream(fileName, response), ExcelTypeEnum.XLSX);
    Sheet sheet = new Sheet(1, 0, object.getClass());
    sheet.setSheetName(sheetName);
    // 异常处理
    writer.write(list, sheet);
    writer.finish();
}
```

### 3.2.2 导出的到多个 `sheet` 的 `Excel`

- 重写导出方法

```java
public class ExcelWriterFactory extends ExcelWriter {
    private OutputStream outputStream;
    private int sheetNo = 1;

    public ExcelWriterFactory(OutputStream outputStream, ExcelTypeEnum typeEnum) {
        super(outputStream, typeEnum);
        this.outputStream = outputStream;
    }

    public ExcelWriterFactory write(List<? extends BaseRowModel> list, String sheetName,
                                    BaseRowModel object) {
        this.sheetNo++;
        try {
            Sheet sheet = new Sheet(sheetNo, 0, object.getClass());
            sheet.setSheetName(sheetName);
            this.write(list, sheet);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public void finish() {
        super.finish();
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

- 导出两个 `sheet` 示例

```java
public static ExcelWriterFactory writeExcelWithSheets(HttpServletResponse response, List<? extends BaseRowModel> list,
                                                      String fileName, String sheetName, BaseRowModel object) {
    ExcelWriterFactory writer = new ExcelWriterFactory(getOutputStream(fileName, response), ExcelTypeEnum.XLSX);
    Sheet sheet = new Sheet(1, 0, object.getClass());
    sheet.setSheetName(sheetName);
    writer.write(list, sheet);
    return writer;
}
```

### 3.2.3 异步导出

1. 如果数据量大，可能导出等待时间较长；
2. 同步导出必须实时下载，异步导出可以保存到系统，需要时再进行下载。

```java
public static String asyWriteExcel(List<? extends BaseRowModel> list,
                                    String sheetName, BaseRowModel object) {
    // 先将数据导出excel到本地
    try {
        String fileName = URLEncoder.encode(createFileName(), "UTF-8");
        ExcelWriter writer = new ExcelWriter(getFileOutputStream(fileName), ExcelTypeEnum.XLSX);
        Sheet sheet = new Sheet(1, 0, object.getClass());
        sheet.setSheetName(sheetName);
        writer.write(list, sheet);
        writer.finish();
        // 读取该excel,并上传到oss，返回下载链接
        // File file = readFileByLines(fileName + ".xlsx");
        // return FileUploadUtil.upload(file, fileName + ".xlsx");
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("创建excel失败!");
    }
    return null;
}
```

## 3.3 测试

> 导出的测试比较简单，这里直接放出接口，具体业务实现见文末源码

```java
@RestController
@Api(tags = "EasyExcel 导出")
@RequestMapping("/export")
public class ExportExcelController {

    @Resource
    ExportExcelService exportExcelService;

    /**
     * 导出 Excel（一个 sheet）
     * @param response
     * @throws IOException
     */
    @ApiOperation(value = "导出 Excel", httpMethod = "GET")
    @GetMapping(value = "/exportWithOneSheet")
    public void exportWithOneSheet(HttpServletResponse response) {
        exportExcelService.exportWithOneSheet(response);
    }

    /**
     * 导出 Excel（多个 sheet）
     */
    @ApiOperation(value = "导出 Excel（多个 sheet）", httpMethod = "GET")
    @GetMapping(value = "/exportWithSheets")
    public void exportWithSheets(HttpServletResponse response) {
        exportExcelService.exportWithSheets(response);
    }

    /**
     * 异步导出 Excel（一个 sheet）
     * @param
     * @throws IOException
     */
    @ApiOperation(value = "异步导出 Excel", httpMethod = "GET")
    @GetMapping(value = "/asyExportWithOneSheet")
    public void asyExportWithOneSheet() {
        exportExcelService.asyExportWithOneSheet();
    }
}
```

## 3.4、待优化

> 本次分享的读取和导出只是基本的使用，面对复杂需求的时候还需要继续加工，更多内容，下次分享，大概包括：

1. 导出合并单元格，单元格格式自定义；
2. 读取和导出自定义转换器；
3. 读取时指定表头行数、读取表头数据；
4. 转换异常处理。

# 四、总结

[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-excel/easyexcel-demo)