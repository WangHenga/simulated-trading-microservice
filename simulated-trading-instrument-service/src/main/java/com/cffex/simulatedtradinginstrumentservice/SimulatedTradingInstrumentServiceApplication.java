package com.cffex.simulatedtradinginstrumentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cffex.simulatedtradinginstrumentservice.mapper")
@EnableFeignClients(basePackages = {"com.cffex.simulatedtradingserviceclient"})
@ComponentScan(basePackages = {"com.cffex"})
public class SimulatedTradingInstrumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingInstrumentServiceApplication.class, args);
   }

}
