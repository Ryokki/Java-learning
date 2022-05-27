package com.atguigu.boot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // @Controller + @ResponseBody (直接返回body给用户)
public class HelloContoller {

	@RequestMapping("/hello")
	public String handle01() {
		return "Hello,Spring Boot 2 你好";
	}

}
