package com.southwind.myspring.entity;

import com.southwind.myspring.Autowired;
import com.southwind.myspring.Component;
import com.southwind.myspring.Qualifier;
import com.southwind.myspring.Value;
import lombok.Data;
import lombok.ToString;

@Data
@Component("myOrder")
public class Order {
    @Value("xxx123")
    private String orderId;
    @Value("1000.5")
    private Float price;
}
