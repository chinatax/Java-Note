# Java 加密算法简介

> 没有根基也许可以建一座小屋，但绝对不能造一座坚固的大厦。

## 一、基本概念

编程概念中的密码，就是**对于要传递的信息按照某种规则进行转换，从而隐藏信息的内容**。这种方法可以使机密信息得以在公开的渠道传递而不泄密。使用这种方法，要经过加密过程。在加密过程中我们需要知道下面的这些概念：

* 原文：或者叫明文，就是被隐藏的文字；
* 加密法：指隐藏原文的法则；
* 密文：或者叫伪文，指对原文按照加密法处理过后生成的可公开传递的文字；
* 密钥：可能是数字、词汇，也可能是一些字母，或者这些东西的组合。

加密的结果生成了密文，要想让接受者能够读懂这些密文，那么就要把加密法以及密钥告诉接受者；否则接受者无法对密文解密，也就无法读懂原文。

## 二、加密算法

### 2.1 对称加密算法

> 对称加密指的就是加密和解密使用同一个秘钥，所以叫做对称加密。

对称加密只有一个秘钥，作为私钥。常见的有：`DES`、`AES` 等等。

- 加密过程

	1. 加密：原文 + 秘钥 = 密文
	1. 解密：密文 - 秘钥 = 原文

- 对称加密的优点

	1. 算法公开；
	1. 计算量小；
	1. 加密速度快；
	1. 加密效率高。

- 对称加密的缺点

	**秘钥的管理和分发是非常困难的，不够安全。**

	>1. 在数据传送前，发送方和接收方必须商定好秘钥，然后双方都必须要保存好秘钥，如果一方的秘钥被泄露了，那么加密的信息也就不安全了；
	> 2. 每对用户每次使用对称加密算法时，都需要使用其他人不知道的唯一秘钥，这会使得收、发双方所拥有的的钥匙数量巨大，秘钥管理也会成为双方的负担。


### 2.2 非对称加密算法

> 非对称加密算法中加密和解密用的不是同一个秘钥，所以叫作非对称加密算法。

- 常见的非对称加密算法

`RSA`算法。


在非对称加密算法每个用户都有两把钥匙，一把公钥一把私钥。**公钥是对外发布的，所有人都看的到；私钥是自己保存，每个人都只知道自己的私钥而不知道别人的**。在非对称加密算法中有加密和解密、加签和验签的概念。

- 加密和解密

用该用户的公钥加密后只能该用户的私钥才能解密。这种情况下，**公钥是用来加密信息的**，确保只有特定的人才能解密该信息。所以这种我们称之为**加密和解密**。

- 加签和验签

还有第二种情况，**公钥是用来解密信息的**，确保让别人知道这条信息是真的由我发布的，是完整正确的。接收者由此可知这条信息确实来自于拥有私钥的某人，这被称作数字签名，公钥的形式就是数字证书。所以这种我们称之为**加签和验签**。

### 2.3 摘要算法

数据摘要算法是密码学算法中非常重要的一个分支，它通过对所有数据提取指纹信息以实现数据签名、数据完整性校验等功能，由于其不可逆性，有时候会被用做敏感信息的加密。数据摘要算法也被称为哈希（`Hash`）算法或散列算法。

- 特征

消息摘要算法的主要特征是**加密过程不需要密钥**，并且经过加密的数据无法被解密，只有输入相同的明文数据经过相同的消息摘要算法才能得到相同的密文。（摘要可以比方为指纹，消息摘要算法就是要得到文件的唯一职位）

- 特点

**无论输入的消息有多长，计算出来的消息摘要的长度总是固定的。**一般地，只要输入的消息不同，对其进行摘要以后产生的摘要消息也必不相同；但相同的输入必会产生相同的输出。只能进行正向的信息摘要，而无法从摘要中恢复出任何的消息，甚至根本就找不到任何与原信息相关的信息（不可逆性）。

好的摘要算法，没有人能从中找到 “碰撞” 或者说极度难找到，虽然 “碰撞” 是肯定存在的（碰撞即不同的内容产生相同的摘要）。

- 应用

一般地，把对一个信息的摘要称为该消息的指纹或数字签名。数字签名是保证信息的完整性和不可否认性的方法。数据的完整性是指信宿接收到的消息一定是信源发送的信息，而中间绝无任何更改；信息的不可否认性是指信源不能否认曾经发送过的信息。其实，通过数字签名还能实现对信源的身份识别（认证），即确定 “信源” 是否是信宿意定的通信伙伴。 数字签名应该具有唯一性，即不同的消息的签名是不一样的；同时还应具有不可伪造性，即不可能找到另一个消息，使其签名与已有的消息的签名一样；还应具有不可逆性，即无法根据签名还原被签名的消息的任何信息。这些特征恰恰都是消息摘要算法的特征，所以消息摘要算法适合作为**数字签名算法**。

- 常见的消息摘要算法

`CRC`、`MD5`、`SHA`等等。

## 三、Java 实现

### 3.1 对称加密-`AES`

分享一下`AES`加密工具类

- `AESUtil.java`

```java
public class AESUtil {
    /**
     * 密钥长度: 128, 192 or 256
     */
    private static final int KEY_SIZE = 128;

    /**
     * 加密/解密算法名称
     */
    private static final String ALGORITHM = "AES";

    /**
     * 随机数生成器（RNG）算法名称
     */
    private static final String RNG_ALGORITHM = "SHA1PRNG";

    /**
     * 数据加密: 明文 -> 密文
     */

    public static String encrypt(String plainStr, String keyStr) throws Exception {
        byte[] plainBytes = plainStr.getBytes();
        byte[] keyBytes = keyStr.getBytes();
        // 生成密钥对象
        SecretKey secKey = generateKey(keyBytes);
        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 初始化密码器（加密模型）
        cipher.init(Cipher.ENCRYPT_MODE, secKey);
        // 加密数据, 返回密文
        byte[] cipherBytes = cipher.doFinal(plainBytes);
        return bytesToHex(cipherBytes);
    }

    /**
     * 数据解密: 密文 -> 明文
     */
    public static String decrypt(String cipherStr, String keyStr) throws Exception {
        byte[] cipherBytes = hexToByteArray(cipherStr);
        byte[] keyBytes = keyStr.getBytes();
        // 生成密钥对象
        SecretKey secKey = generateKey(keyBytes);
        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 初始化密码器（解密模型）
        cipher.init(Cipher.DECRYPT_MODE, secKey);
        // 解密数据, 返回明文
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes);
    }

    /**
     * 生成密钥对象
     *
     * @param key byte[] 类型参数
     * @return AES密钥对象
     */
    private static SecretKey generateKey(byte[] key) throws Exception {
        // 创建安全随机数生成器
        SecureRandom random = SecureRandom.getInstance(RNG_ALGORITHM);
        // 设置 密钥key的字节数组 作为安全随机数生成器的种子
        random.setSeed(key);

        // 创建 AES算法生成器
        KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM);
        // 初始化算法生成器
        gen.init(KEY_SIZE, random);

        // 生成 AES密钥对象
        return gen.generateKey();
    }

    /**
     * byte数组转16进制
     *
     * @param bytes byte数组
     * @return 返回16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 16进制转byte
     *
     * @param inHex 16进制字符串
     * @return byte
     */
    private static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * 16进制转byte数组
     *
     * @param inHex 16进制字符串
     * @return byte数组
     */
    private static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
}
```

- 测试

```java
@Test
public void aesTest() throws Exception {
    // 原文内容
    String content = "你好，我是要发送的数据";
    // AES加密/解密用的原始密码
    String key = "secret";

    // 输出加密后的密文
    String cipherString = AESUtil.encrypt(content, key);
    logger.info("AES 加密后的密文：[{}]", cipherString);

    // 输出解密后的明文
    String plainStr = AESUtil.decrypt(cipherString, key);
    logger.info("AES 解密后的明文：[{}]", plainStr);
}
```

### 3.2 非对称加密-`RSA`

分享一下`RSA`加密工具类。

- `RSAUtil.java`

```java
public class RSAUtil {
    /**
     * 密钥长度 于原文长度对应 以及越长速度越慢
     */
    private final static int KEY_SIZE = 1024;
    /**
     * 用于封装随机产生的公钥与私钥
     */
    private static Map<Integer, String> keyMap = new HashMap<>();

    /**
     * 随机生成密钥对
     */
    public static Map genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator 用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        // 将公钥和私钥保存到Map
        //0表示公钥
        keyMap.put(0, publicKeyString);
        //1表示私钥
        keyMap.put(1, privateKeyString);
        return keyMap;
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.getDecoder().decode(str);
        //base64编码的私钥
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }
}
```

- 测试

```java
@Test
public void rsaTest() throws Exception {
    //生成公钥和私钥
    Map<Integer, String> keyMap = RSAUtil.genKeyPair();
    logger.info("公钥:[{}]", keyMap.get(0));
    logger.info("私钥:[{}]", keyMap.get(1));

    // 要加密的数据
    String message = "你好，我是要发送的数据";

    // 使用公钥加密
    String secret0 = RSAUtil.encrypt(message, keyMap.get(0));
    logger.info("RSA 加密后的密文：[{}]", secret0);

    // 使用私钥解密
    String secret1 = RSAUtil.decrypt(secret0, keyMap.get(1));
    logger.info("RSA 解密后的明文：[{}]",secret1);
}
```

### 3.3 摘要算法加密

这里列举了一个工具类，其中包含`MD5`、`SHA1`、`SHA256` 三种加密算法。

- `SummaryEncryptionUtil.java`

```java
public class SummaryEncryptionUtil {
    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA1";
    private static final String SHA_256 = "SHA-256";
    /**
     * 用来将字节转换成 16 进制表示的字符
     */
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * MD5 加密
     *
     * @param str 待加密字符串
     * @return 加密后的字符串
     */
    public static String encodeWithMD5(String str) {
        return encode(MD5, str);
    }

    /**
     * SHA1 加密
     *
     * @param str 待加密字符串
     * @return 加密后的字符串
     */
    public static String encodeWithSHA1(String str) {
        return encode(SHA1, str);
    }

    /**
     * SHA-256 加密
     *
     * @param str 待加密字符串
     * @return 加密后的字符串
     */
    public static String encodeWithSHA256(String str) {
        return encode(SHA_256, str);
    }

    /**
     * 通过加密算法加密字符串
     */
    private static String encode(String algorithm, String str) {
        if (str == null) {
            return null;
        }
        try {
            // 生成一个指定算法加密计算摘要
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(str.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }
}
```

> 需要注意的是：摘要算法都是单向加密，所以不存在解密操作。

- 测试

```java
@Test
    public void summaryTest() {
        // 要加密的数据
        String message = "你好，我是要发送的数据";
        // 使用公钥加密
        String secret0 = SummaryEncryptionUtil.encodeWithMD5(message);
        logger.info("MD5 加密后的密文：[{}]", secret0);
        // 使用私钥解密
        String secret1 = SummaryEncryptionUtil.encodeWithSHA1(message);
        logger.info("SHA1 解密后的明文：[{}]",secret1);
        // 使用私钥解密
        String secret2 = SummaryEncryptionUtil.encodeWithSHA256(message);
        logger.info("SHA256 解密后的明文：[{}]",secret2);
    }
```


> 更多 `Java` 笔记，详见[【Java 知识笔记本】](https://github.com/vanDusty/Java-Note)，欢迎提供想法建议。

[【Github 示例代码】](https://github.com/vanDusty/JDK/tree/master/JDK-Secret)

参考文章

1. [关于加解密、加签验签的那些事](https://juejin.im/post/5eba78e95188256d9f09656b)
1. [共享密钥加密与公开密钥加密](https://juejin.im/post/5eb04c836fb9a0435e2c3581)