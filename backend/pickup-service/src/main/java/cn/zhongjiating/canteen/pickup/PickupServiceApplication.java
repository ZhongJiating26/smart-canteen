package cn.zhongjiating.canteen.pickup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("cn.zhongjiating.canteen.pickup.mapper")
public class PickupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickupServiceApplication.class, args);
    }

}
