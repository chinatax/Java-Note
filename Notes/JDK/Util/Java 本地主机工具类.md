# Java 获取本地IP地址和主机名


> 拿到本机 `IP`不是一件困难的事，但是拿到正确的就比较难了。


### 一、一般方式

[【菜鸟教程】](https://www.runoob.com/java/net-localip.html)中直接通过 `java.net.InetAddress`类获取，如下：

```java
import java.net.InetAddress;
 
public class Main {
   public static void main(String[] args) 
   throws Exception {
      InetAddress addr = InetAddress.getLocalHost();
      System.out.println("Local HostAddress: 
      "+addr.getHostAddress());
      String hostname = addr.getHostName();
      System.out.println("Local host name: "+hostname);
   }
}
```

这种方式获取的主机名没啥问题，**但获取到的`IP`地址却有待考量**：如果一台机器有多个网卡，他获取的`IP`是谁的呢？

> 事实上，上面输出的`IP`是我虚拟机`IP`地址，既不是我有线网卡的地址，也不是我无线网卡的地址。


### 二、推荐使用

利用 `java.net.NetworkInterface` 获取，提供常用的静态方法如下：

1. `getLocalHostAddress()`：返回本机 `IP`
1. `getLocalHostName()`：返回主机名
1. `getLocalInetAddress`：返回 `InetAddress`
1. `isWindowsOS()`：判断操作系统是否是 `Windows`
1. `isMacOS()`：判断操作系统是否是 `MacOS`

```java
public class LocalHostUtil {

    private static InetAddress inetAddress;

    /**
     * 返回 InetAddress
     * @return
     */
    public static InetAddress getLocalInetAddress() {
        if (inetAddress == null) {
            load();
        }
        return inetAddress;
    }

    /**
     * 返回本机IP
     * @return
     */
    public static String getLocalHostAddress() {
        if (inetAddress == null) {
            load();
        }
        return inetAddress.getHostAddress();
    }

    /**
     * 返回主机名
     * @return
     */
    public static String getLocalHostName() {
        if (inetAddress == null) {
            load();
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return inetAddress.getHostName();
        }
    }

    /**
     * 判断操作系统是否是 Windows
     * @return
     */
    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    /**
     * 判断操作系统是否是 MacOS
     * @return
     */
    public static boolean isMacOS() {
        boolean isWindowsOS = false;
        String osName = getProperty("os.name");
        if (osName.toLowerCase().indexOf("mac") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    private static InetAddress findValidateIp(List<Address> addresses) {
        InetAddress local = null;
        int size = addresses.size();
        int maxWeight = -1;

        for (int i = 0; i < size; i++) {
            Address address = addresses.get(i);
            if (address.isInet4Address()) {
                int weight = 0;

                if (address.isSiteLocalAddress()) {
                    weight += 8;
                }

                if (address.isLinkLocalAddress()) {
                    weight += 4;
                }

                if (address.isLoopbackAddress()) {
                    weight += 2;
                }

                if (address.hasHostName()) {
                    weight += 1;
                }

                if (weight > maxWeight) {
                    maxWeight = weight;
                    local = address.getAddress();
                }
            }
        }

        return local;
    }

    private static String getProperty(String name) {
        String value = null;

        value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private static void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                inetAddress = InetAddress.getByName(ip);
                return;
            } catch (Exception e) {
                System.err.println(e);
                // ignore
            }
        }

        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            List<Address> addresses = new ArrayList<>();
            InetAddress local = null;

            try {
                // 遍历网络接口
                for (NetworkInterface ni : nis) {
                    if (ni.isUp() && !ni.isLoopback()) {
                        List<InetAddress> list = Collections.list(ni.getInetAddresses());
                        // 遍历网络地址
                        for (InetAddress address : list) {
                            addresses.add(new Address(address, ni));
                        }
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            inetAddress = local;
        } catch (SocketException e) {
            // ignore it
        }
    }

    static class Address {
        private InetAddress inetAddress;

        private boolean loopback;

        public Address(InetAddress address, NetworkInterface ni) {
            inetAddress = address;

            try {
                if (ni != null && ni.isLoopback()) {
                    loopback = true;
                }
            } catch (SocketException e) {
                // ignore it
            }
        }

        public InetAddress getAddress() {
            return inetAddress;
        }

        public boolean hasHostName() {
            return !inetAddress.getHostName().equals(inetAddress.getHostAddress());
        }

        public boolean isLinkLocalAddress() {
            return !loopback && inetAddress.isLinkLocalAddress();
        }

        public boolean isLoopbackAddress() {
            return loopback || inetAddress.isLoopbackAddress();
        }

        public boolean isSiteLocalAddress() {
            return !loopback && inetAddress.isSiteLocalAddress();
        }

        public boolean isInet4Address(){
            return inetAddress instanceof Inet4Address;
        }
    }
}
```
