package cn.memset.marketing.web.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MyApplication {
  public static void main(String[] args) {
	  ConfigurableApplicationContext context = SpringApplication.run(MyApplication.class, args);
    for (String beanDefinitionName : context.getBeanDefinitionNames()) {
      System.out.println(beanDefinitionName);
    }
  }
}
