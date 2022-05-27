[硬核干货！SpringBoot自动配置实战项目，从0开始手撸Starter，_bilibili](https://www.bilibili.com/video/BV1Zu4116714?share_source=copy_pc)
# 什么是starer
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525132348.png)

这是web的starer，web项目需要导入这个starter

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525132431.png)

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525132458.png)

# starter的结构
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525140432.png)

???? 为什么我没有看到可选依赖

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525140852.png)
这是redis的三个配置类

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525141218.png)
在配置文件spring.factories中有这些

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525141305.png)
在我们的配置文件中可以配置redis的连接信息

# 手撸一个starter
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525142204.png)

有一个用户微服务ms-user，向外提供了各种和user有关的接口
另一个营销微服务ms-marketing需要调用ms-user，通过userId来获得用户的各种信息

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525142231.png)

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525142300.png)

ms-user做成一个starter，ms-markeing引入这个starter


## OpenFeign
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525143411.png)
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525143430.png)
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525143441.png)
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525143457.png)

## ms-user-web
这是user模块去查询用户的

```java
@RestController  
public class UserController implements UserFeignClient {  
    private static final Map<Long, User> users = new HashMap<>();  
  
    static {  
        users.put(10000L, new User(10000L, "张三"));  
        users.put(10001L, new User(10001L, "李四"));  
    }  
  
    @Override  
    @GetMapping("/get/{userId}")  
    public User getUserById(@PathVariable("userId") long userId) {  
        return users.get(userId);  
    }  
}
```

这里实现了UserFeignClient这个微服务接口，所以访问的url就是这个微服务接口定义的那个。之后我们让这个web起来

访问http://localhost:9926/get/10000，得到{"id":10000,"name":"张三"}

## ms-user-api
这里首先定义了User的pojo
```java
public class User {  
    private long id;  
    private String name;
    // getter setter allargConstructor
```

然后有一个重要的微服务Feign接口：
```java
@FeignClient(name = "ms-user",  
        url = "${cnmemset.ms-user.url:127.0.0.1:9926}",  
        fallbackFactory = UserFeignClient.UserFallbackFactory.class)   // 用于服务降级  
public interface UserFeignClient {  
    @GetMapping("/get/{userId}")  
    @ResponseBody  
    public User getUserById(@PathVariable("userId") long userId);  
  
    @Component // 用于服务降级,当对应的微服务不可用时，就用这个接口来保证微服务的可用  
    public static class UserFallbackFactory implements FallbackFactory<UserFeignClient> {  
        private final Logger logger = LoggerFactory.getLogger(UserFallbackFactory.class);  
  
        @Override  
        public UserFeignClient create(Throwable cause) {  
            return new UserFeignClient() {  
                @Override  
                public User getUserById(long userId) {  
                    logger.error("调用ms-user发生异常：", cause);  
                    return null;                }  
            };  
        }  
    }}
```

在这里，我们定义了这个微服务的port为9926

## ms-user-autoconfig
```java
/**  
 * MsUserConfiguration最核心的作用是EnableFeignClients，使得UserFeignClient被开启，如果没有导入这个autoconfig,那么就会报错  
 */  
@Configuration(proxyBeanMethods = false)  
@ConditionalOnClass(UserFeignMarker.class)  
@EnableFeignClients(basePackageClasses = UserFeignClient.class) // 开启UserFeignClient  
@Import({UserFeignClient.UserFallbackFactory.class})  // 注册  
public class MsUserAutoConfiguration {  
  
}
```
在这里，会把market需要的包准备好，market需要调用UserFeignClient这个微服务，所以开启它，而且需要UserFallbackFactory的微服务降级，所以把它也准备好。

另外别忘了，这里的resources里的META-INF/spring.factories如下:
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525164730.png)

这样，之后market导入这个自动配置的包后，就可以去生成这些bean啦！！！ (注意！！！如果没有这个factories，由于是第三方jar包并不会将@Configuration导入到IOC里面，也就不会将需要的各个class准备好)



## ms-user-spring-boot-starter
这里没有类，只有一个pom.xml，里面把market需要jar包准备好：

```xml
    <artifactId>ms-user-spring-boot-starter</artifactId>
    <name>ms-user-spring-boot-starter</name>
    <description>ms-user的自动配置Starter模块</description>

    <dependencies>
        <!-- 必需的基础的 spring-boot 的 starter 依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <!-- 自动配置模块的依赖 -->
        <dependency>
            <groupId>cn.memset.user</groupId>
            <artifactId>ms-user-autoconfigure</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- API模块的依赖 -->
        <dependency>
            <groupId>cn.memset.user</groupId>
            <artifactId>ms-user-api</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- 单元测试的依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
```
由于需要User类和注入UserFeignClient及其服务降级类UserFallbackFactory，所以要导入ms-user-api
由于需要UserFeignClient这个微服务BEAN，所以要导入ms-user-autoconfigure

## ms-marketing-web
在这里，我们需要导入刚才做好的starter：
```java
        <dependency>
            <groupId>cn.memset.user</groupId>
            <artifactId>ms-user-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
```
然后就可以直接去用了

```java
@RestController  
public class GreetingController {  
    private final UserFeignClient userFeignClient;  
  
    @Autowired  
    public GreetingController(UserFeignClient userFeignClient) {  
        this.userFeignClient = userFeignClient;  
    }  
  
    @GetMapping("/hello/{userId}")  
    public String sayHi(@PathVariable("userId") long userId) {  
        User user = userFeignClient.getUserById(userId);  
  
        if (user == null || !StringUtils.hasLength(user.getName())) {  
            return "Hello, 匿名者";  
        }  
  
        return "Hello, " + user.getName();  
    }  
}
```

这里注入了UserFeignClient对象，然后有一个新的路径/hello/userId。
这里实际上是去调用userFeignClient.getUserById(userId)拿到这个User，然后告诉用户这个user的信息。

这里的工作原理是，如果user-web的那个UserController当前可用，那么就调用那个的getUserById函数来获得User，如果不可用，那么就调用服务降级的函数UserFallbackFactory去抛异常。

***
在marketing中，如果那个AutoConfig包没有加上spring.factories，就不会注入那个AutoConfig，及其导入的Bean(UserFallbackFactory、MsUserAutoConfiguration、UserFeignClient、MsUserConfiguration)，所以运行的时候会报错，所以别忘了spring.factories

# 总结
![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525170526.png)

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525170554.png)

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525170652.png)
这里我没看，先不用管


# 需要starter的核心原因
我发现需要starter的一个核心原因在于，引入的jar包中的@Component注解的Bean并不会注入到我们的IOC中，因此，我们需要starter + auto config来把需要的Bean进行注册！！！

举个例子：
我们在web中导入userapi的jar包，在userapi里面做一个bean：
```java
@Component  
public class TestBean {  
 public TestBean() {  
    System.out.println("TestBean is created");  
 }  
}
```

然后在web中执行SpringApplication.run，发现并没有生成这个TestBean.即便我们autowired去尝试注入TestBean，也还是没有。 这是因为ComponentScan并不会扫描Jar包里的，因此，要想让jar包里的component生效，我们要用autoconfig去@Bean，@EnableConfiguration，@Import等来注入...


![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525193810.png)

![](https://kkbabe-picgo.oss-cn-hangzhou.aliyuncs.com/img/20220525193912.png)
