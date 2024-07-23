package com.cffex.simulatedtradinguserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cffex.simulatedtradinguserservice.mapper")
@EnableFeignClients(basePackages = {"com.cffex.simulatedtradingserviceclient"})
@ComponentScan(basePackages = {"com.cffex"})
public class SimulatedTradingUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingUserServiceApplication.class, args);
    }

}
