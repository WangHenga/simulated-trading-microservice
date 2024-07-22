package com.cffex.simulatedtradinginstrumentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cffex.simulatedtradinginstrumentservice.mapper")
public class SimulatedTradingInstrumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingInstrumentServiceApplication.class, args);
    }

}
