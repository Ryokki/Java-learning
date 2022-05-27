package cn.memset.user.autoconfigure;

import cn.memset.user.api.feign.UserFeignClient;
import cn.memset.user.api.feign.UserFeignMarker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * MsUserConfiguration最核心的作用是EnableFeignClients，使得UserFeignClient被开启，如果没有导入这个autoconfig,那么就会报错
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(UserFeignMarker.class)
@EnableFeignClients(basePackageClasses = UserFeignClient.class) // 开启UserFeignClient
@Import({UserFeignClient.UserFallbackFactory.class })  // 注册
public class MsUserAutoConfiguration {

}




