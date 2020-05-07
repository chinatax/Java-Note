# Spring Boot 发送邮件

发送邮件应该是网站的必备拓展功能之一，注册验证、忘记密码或者是给用户发送营销信息。

## 一、邮件协议

在收发邮件的过程中，需要遵守相关的协议，其中主要有：

1. 发送电子邮件的协议：`SMTP`；
1. 接收电子邮件的协议：`POP3`和`IMAP`。

### 1.1 什么是`SMTP`？

`SMTP`全称为`Simple Mail Transfer Protocol`（简单邮件传输协议），它是一组用于从源地址到目的地址传输邮件的规范，通过它来控制邮件的中转方式。`SMTP`认证要求必须提供账号和密码才能登陆服务器，其设计目的在于避免用户受到垃圾邮件的侵扰。

### 1.2 什么是`IMAP`？

`IMAP`全称为`Internet Message Access Protocol`（互联网邮件访问协议），`IMAP`允许从邮件服务器上获取邮件的信息、下载邮件等。`IMAP`与`POP`类似，都是一种邮件获取协议。

### 1.3 什么是`POP3`？

`POP3`全称为`Post Office Protocol 3`（邮局协议），`POP3`支持客户端远程管理服务器端的邮件。`POP3`常用于**离线**邮件处理，即允许客户端下载服务器邮件，然后服务器上的邮件将会被删除。目前很多`POP3`的邮件服务器只提供下载邮件功能，服务器本身并不删除邮件，这种属于改进版的`POP3`协议。

### 1.4 `IMAP`和`POP3`协议有什么不同呢？

两者最大的区别在于，`IMAP`允许双向通信，即在客户端的操作会反馈到服务器上，例如在客户端收取邮件、标记已读等操作，服务器会跟着同步这些操作。而对于`POP`协议虽然也允许客户端下载服务器邮件，但是在客户端的操作并不会同步到服务器上面的，例如在客户端收取或标记已读邮件，服务器不会同步这些操作。




本文主要演示了` Spring Boot`整合邮件功能，包括发送简单文本邮件、附件邮件、模板邮件。

## 二、初始化配置

### 2.1 开启邮件服务

> 本文仅以`QQ`邮箱和`163`邮箱为例。

1. [`QQ`邮箱 开启邮件服务文档](https://service.mail.qq.com/cgi-bin/help?subtype=1&&no=1001256&&id=28)
1. [`163`邮箱 开启邮件服务文档](http://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac2cda80145a1742516)

### 2.2 `pom.xml`

> 正常我们会用`JavaMail`相关`api`来写发送邮件的相关代码，但现在`Spring Boot`提供了一套更简易使用的封装。

1. `spring-boot-starter-mail`:`Spring Boot` 邮件服务；
2. `spring-boot-starter-thymeleaf`:使用 `Thymeleaf`制作邮件模版。

```xml
<!-- test 包-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>
<!--mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<!--使用 Thymeleaf 制作邮件模板 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>1.8.4</scope>
</dependency>
```

### 2.3 `application.yml`

`spring-boot-starter-mail` 的配置由 `MailProperties` 配置类提供。

针对不同的邮箱的配置略有不同，以下是`QQ`邮箱和`163`邮箱的配置。

```xml
server:
  port: 8081
#spring:
#  mail:
#    # QQ 邮箱 https://service.mail.qq.com/cgi-bin/help?subtype=1&&no=1001256&&id=28
#    host: smtp.qq.com
#    # 邮箱账号
#    username: van93@qq.com
#    # 邮箱授权码（不是密码）
#    password: password
#    default-encoding: UTF-8
#    properties:
#      mail:
#        smtp:
#          auth: true
#          starttls:
#            enable: true
#            required: true
spring:
  mail:
    # 163 邮箱 http://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac2cda80145a1742516
    host: smtp.163.com
    # 邮箱账号
    username: 17098705205@163.com
    # 邮箱授权码（不是密码）
    password: password
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 2.4 邮件信息类

> 来保存发送邮件时的邮件主题、邮件内容等信息

```java
@Data
public class Mail {
    /**
     * 邮件id
     */
    private String id;
    /**
     * 邮件发送人
     */
    private String sender;
    /**
     * 邮件接收人 （多个邮箱则用逗号","隔开）
     */
    private String receiver;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private String text;
    /**
     * 附件/文件地址
     */
    private String filePath;
    /**
     * 附件/文件名称
     */
    private String fileName;
    /**
     * 是否有附件（默认没有）
     */
    private Boolean isTemplate = false;
    /**
     * 模版名称
     */
    private String emailTemplateName;
    /**
     * 模版内容
     */
    private Context emailTemplateContext;

}
```

## 三、发送邮件的实现

### 3.1 检查输入的邮件配置

> 校验邮件收信人、邮件主题和邮件内容这些必填项

```java
private void checkMail(Mail mail) {
    if (StringUtils.isEmpty(mail.getReceiver())) {
        throw new RuntimeException("邮件收信人不能为空");
    }
    if (StringUtils.isEmpty(mail.getSubject())) {
        throw new RuntimeException("邮件主题不能为空");
    }
    if (StringUtils.isEmpty(mail.getText()) && null == mail.getEmailTemplateContext()) {
        throw new RuntimeException("邮件内容不能为空");
    }
}
```

### 3.2 将邮件保存到数据库

发送结束后将邮件保存到数据库，便于统计和追查邮件问题。

```java
private Mail saveMail(Mail mail) {
    // todo 发送成功/失败将邮件信息同步到数据库
    return mail;
}
```

### 3.3 发送邮件

- 发送纯文本邮件

```java
public void sendSimpleMail(Mail mail){
    checkMail(mail);
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(sender);
    mailMessage.setTo(mail.getReceiver());
    mailMessage.setSubject(mail.getSubject());
    mailMessage.setText(mail.getText());
    mailSender.send(mailMessage);
    saveMail(mail);
}
```


- 发送邮件并携带附件

```java
public void sendAttachmentsMail(Mail mail) throws MessagingException {
    checkMail(mail);
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
    helper.setFrom(sender);
    helper.setTo(mail.getReceiver());
    helper.setSubject(mail.getSubject());
    helper.setText(mail.getText());
    File file = new File(mail.getFilePath());
    helper.addAttachment(file.getName(), file);
    mailSender.send(mimeMessage);
    saveMail(mail);
}
```

- 发送模版邮件

```java
public void sendTemplateMail(Mail mail) throws MessagingException {
    checkMail(mail);
    // templateEngine 替换掉动态参数，生产出最后的html
    String emailContent = templateEngine.process(mail.getEmailTemplateName(), mail.getEmailTemplateContext());

    MimeMessage mimeMessage = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
    helper.setFrom(sender);
    helper.setTo(mail.getReceiver());
    helper.setSubject(mail.getSubject());
    helper.setText(emailContent, true);
    mailSender.send(mimeMessage);
    saveMail(mail);
}
```

## 四、测试及优化

### 4.1 单元测试

1. 测试附件邮件时，附件放在`static`文件夹下；
2. 测试模版邮件时，模版放在`file`文件夹下。

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class MailServiceTest {

    @Resource
    MailService mailService;
    
    /**
     * 发送纯文本邮件
     */
    @Test
    public void sendSimpleMail() {
        Mail mail = new Mail();
//        mail.setReceiver("17098705205@163.com");
        mail.setReceiver("van93@qq.com");
        mail.setSubject("测试简单邮件");
        mail.setText("测试简单内容");
        mailService.sendSimpleMail(mail);
    }

    /**
     * 发送邮件并携带附件
     */
    @Test
    public void sendAttachmentsMail() throws MessagingException {
        Mail mail = new Mail();
//        mail.setReceiver("17098705205@163.com");
        mail.setReceiver("van93@qq.com");
        mail.setSubject("测试附件邮件");
        mail.setText("附件邮件内容");
        mail.setFilePath("file/dusty_blog.jpg");
        mailService.sendAttachmentsMail(mail);
    }

    /**
     * 测试模版邮件邮件
     */
    @Test
    public void sendTemplateMail() throws MessagingException {
        Mail mail = new Mail();
//        mail.setReceiver("17098705205@163.com");
        mail.setReceiver("van93@qq.com");
        mail.setSubject("测试模版邮件邮件");
        //创建模版正文
        Context context = new Context();
        // 设置模版需要更换的参数
        context.setVariable("verifyCode", "6666");
        mail.setEmailTemplateContext(context);
        // 模版名称(模版位置位于templates目录下)
        mail.setEmailTemplateName("emailTemplate");
        mailService.sendTemplateMail(mail);
    }
    
}
```

### 4.2 优化

因为平时发送邮件还有抄送/密送等需求，这里，封装一个实体和工具类，便于直接调用邮件服务。

- 邮件信息类

```java
@Data
public class MailDomain {
    /**
     * 邮件id
     */
    private String id;
    /**
     * 邮件发送人
     */
    private String sender;
    /**
     * 邮件接收人 （多个邮箱则用逗号","隔开）
     */
    private String receiver;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private String text;

    /**
     * 抄送（多个邮箱则用逗号","隔开）
     */
    private String cc;
    /**
     * 密送（多个邮箱则用逗号","隔开）
     */
    private String bcc;
    /**
     * 附件/文件地址
     */
    private String filePath;
    /**
     * 附件/文件名称
     */
    private String fileName;
    /**
     * 是否有附件（默认没有）
     */
    private Boolean isTemplate = false;
    /**
     * 模版名称
     */
    private String emailTemplateName;
    /**
     * 模版内容
     */
    private Context emailTemplateContext;
    /**
     * 发送时间(可指定未来发送时间)
     */
    private Date sentDate;
}
```

- 邮件工具类

```java
@Component
public class EmailUtil {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * 构建复杂邮件信息类
     * @param mail
     * @throws MessagingException
     */
    public void sendMail(MailDomain mail) throws MessagingException {

        //true表示支持复杂类型
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
        //邮件发信人从配置项读取
        mail.setSender(sender);
        //邮件发信人
        messageHelper.setFrom(mail.getSender());
        //邮件收信人
        messageHelper.setTo(mail.getReceiver().split(","));
        //邮件主题
        messageHelper.setSubject(mail.getSubject());
        //邮件内容
        if (mail.getIsTemplate()) {
            // templateEngine 替换掉动态参数，生产出最后的html
            String emailContent = templateEngine.process(mail.getEmailTemplateName(), mail.getEmailTemplateContext());
            messageHelper.setText(emailContent, true);
        }else {
            messageHelper.setText(mail.getText());
        }
        //抄送
        if (!StringUtils.isEmpty(mail.getCc())) {
            messageHelper.setCc(mail.getCc().split(","));
        }
        //密送
        if (!StringUtils.isEmpty(mail.getBcc())) {
            messageHelper.setCc(mail.getBcc().split(","));
        }
        //添加邮件附件
        if (mail.getFilePath() != null) {
            File file = new File(mail.getFilePath());
            messageHelper.addAttachment(file.getName(), file);
        }
        //发送时间
        if (StringUtils.isEmpty(mail.getSentDate())) {
            messageHelper.setSentDate(mail.getSentDate());
        }
        //正式发送邮件
        mailSender.send(messageHelper.getMimeMessage());
    }

    /**
     * 检测邮件信息类
     * @param mail
     */
    private void checkMail(MailDomain mail) {
        if (StringUtils.isEmpty(mail.getReceiver())) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(mail.getSubject())) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(mail.getText()) && null == mail.getEmailTemplateContext()) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    /**
     * 将邮件保存到数据库
     * @param mail
     * @return
     */
    private MailDomain saveMail(MailDomain mail) {
        // todo 发送成功/失败将邮件信息同步到数据库
        return mail;
    }
}
```

> 具体的测试详见[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-list/send-mail)，这里就不贴出来了。

## 五、 总结及延伸

### 5.1 异步发送

很多时候邮件发送并不是我们主业务必须关注的结果，比如通知类、提醒类的业务可以允许延时或者失败。这个时候可以采用异步的方式来发送邮件，加快主交易执行速度，在实际项目中可以采用`MQ`发送邮件相关参数，监听到消息队列之后启动发送邮件。

### 5.2 发送失败情况

因为各种原因，总会有邮件发送失败的情况，比如：邮件发送过于频繁、网络异常等。在出现这种情况的时候，我们一般会考虑重新重试发送邮件，会分为以下几个步骤来实现：

1. 接收到发送邮件请求，首先记录请求并且入库；
1. 调用邮件发送接口发送邮件，并且将发送结果记录入库；
1. 启动定时系统扫描时间段内，未发送成功并且重试次数小于`3`次的邮件，进行再次发送。

### 5.3 其他问题

邮件端口问题和附件大小问题。


### 5.4 示例代码地址

[Github 示例代码](https://github.com/vanDusty/SpringBoot-Home/tree/master/springboot-demo-list/send-mail)