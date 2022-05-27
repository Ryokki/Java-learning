https://www.bilibili.com/video/BV1AV411i7VH?share_source=copy_web

学习Spring ioc的简单实现.

> 这个简单的spring也是实现了循环依赖的，因为IOC会先把所有bean实例化，再Autowire填充，填充的时候需要的bean肯定准备好了



```java
@Data
@Component("myOrder")
public class Order {
    @Value("xxx123")
    private String orderId;
    @Value("1000.5")
    private Float price;
}
```

```java
@Data
@Component
public class Account {
    @Value("1")
    private Integer id;
    @Value("张三")
    private String name;
    @Value("22")
    private Integer age;
    @Autowired
    @Qualifier("myOrder")
    private Order order;
}
```



程序最重要的是这段：

```java
    public MyAnnotationConfigApplicationContext(String pack) {
        //遍历包，找到@Component目标类(原材料)
        Set<BeanDefinition> beanDefinitions = findBeanDefinitions(pack);
        //根据原材料创建bean
        createObject(beanDefinitions);
        //自动装载
        autowireObject(beanDefinitions);
    }
```

1. 找到所有的@Component的类，生成他们的beanDefinition，即:

   ```java
   public class BeanDefinition {
       private String beanName;	// 类名首字母小写
       private Class beanClass;	// 类的信息
   }
   ```

2. 根据beanDefinition中的Class信息来用无参构造函数创建这个bean，并将@Value的数据用set函数初始化好，并存入ioc中

3. 



## findBeanDefinitions 

```java
public Set<BeanDefinition> findBeanDefinitions(String pack){
    //1、获取包下的所有类
    Set<Class<?>> classes = MyTools.getClasses(pack);   // 根据包名获取下面的所有class
    Iterator<Class<?>> iterator = classes.iterator();
    Set<BeanDefinition> beanDefinitions = new HashSet<>();
    while (iterator.hasNext()) {
        //2、遍历这些类，找到添加了注解的类
        Class<?> clazz = iterator.next();
        Component componentAnnotation = clazz.getAnnotation(Component.class);
        if(componentAnnotation!=null){
            //获取Component注解的值
            String beanName = componentAnnotation.value();
            if("".equals(beanName)){
                //获取类名首字母小写
                String className = clazz.getName().replaceAll(clazz.getPackage().getName() + ".", "");
                beanName = className.substring(0, 1).toLowerCase()+className.substring(1);
            }
            //3、将这些类封装成BeanDefinition，装载到集合中
            beanDefinitions.add(new BeanDefinition(beanName, clazz));
            beanNames.add(beanName);
        }
    }
    return beanDefinitions;
}
```

## createObject

```java
public void createObject(Set<BeanDefinition> beanDefinitions){
    Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
    while (iterator.hasNext()) {
        BeanDefinition beanDefinition = iterator.next();
        Class clazz = beanDefinition.getBeanClass();
        String beanName = beanDefinition.getBeanName();
        try {
            //创建的对象
            Object object = clazz.getConstructor().newInstance();
            //完成属性的赋值
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Value valueAnnotation = declaredField.getAnnotation(Value.class);
                if(valueAnnotation!=null){
                    String value = valueAnnotation.value();
                    String fieldName = declaredField.getName();
                    String methodName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
                    Method method = clazz.getMethod(methodName,declaredField.getType());
                    //完成数据类型转换
                    Object val = null;
                    switch (declaredField.getType().getName()){
                        case "java.lang.Integer":
                            val = Integer.parseInt(value);
                            break;
                        case "java.lang.String":
                            val = value;
                            break;
                        case "java.lang.Float":
                            val = Float.parseFloat(value);
                            break;
                    }
                    method.invoke(object, val);
                }
            }
            //存入缓存
            ioc.put(beanName, object);
        }
}
```

这里做的事很简单：

1. 根据无参构造函数来创建这个bean实例
2. getDeclaredFields()来获得所有的属性，找到属性中有@Value注解的，把@Value的值用set函数传进去
3. 存入ioc

## autowireObject

```java
public void autowireObject(Set<BeanDefinition> beanDefinitions){
    Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
    while (iterator.hasNext()) {
        BeanDefinition beanDefinition = iterator.next();
        Class clazz = beanDefinition.getBeanClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Autowired annotation = declaredField.getAnnotation(Autowired.class);
            if(annotation!=null){
                Qualifier qualifier = declaredField.getAnnotation(Qualifier.class);
                if(qualifier!=null){
                    //byName
                    String beanName = qualifier.value();
                    Object bean = getBean(beanName);
                    String fieldName = declaredField.getName();
                    String methodName = "set"+fieldName.firstToLower();
                    Method method = clazz.getMethod(methodName, declaredField.getType());
                    Object object = getBean(beanDefinition.getBeanName());
                    method.invoke(object, bean);
                }else{
                    //byType
                    //todo
                }
            }
        }
    }
}

```



这里就是对于有autowired注解的，看看它有没有qualifier注解，去从ioc中拿这个bean，注入.

> 这里和真实的qualifier不一样，我记得真实的是根据这个属性名来拿...



