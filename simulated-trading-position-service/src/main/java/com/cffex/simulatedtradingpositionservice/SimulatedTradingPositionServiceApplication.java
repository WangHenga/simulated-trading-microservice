package com.cffex.simulatedtradingpositionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cffex.simulatedtradingpositionservice.mapper")
@EnableFeignClients(basePackages = {"com.cffex.simulatedtradingserviceclient"})
@ComponentScan(basePackages = {"com.cffex"})
public class SimulatedTradingPositionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingPositionServiceApplication.class, args);
    }

}
