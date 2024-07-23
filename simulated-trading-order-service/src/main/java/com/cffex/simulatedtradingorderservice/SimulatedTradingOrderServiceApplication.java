package com.cffex.simulatedtradingorderservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cffex.simulatedtradingorderservice.mapper")
@ComponentScan(basePackages = {"com.cffex"})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableFeignClients(basePackages = {"com.cffex.simulatedtradingserviceclient"})
public class SimulatedTradingOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingOrderServiceApplication.class, args);
    }

}
