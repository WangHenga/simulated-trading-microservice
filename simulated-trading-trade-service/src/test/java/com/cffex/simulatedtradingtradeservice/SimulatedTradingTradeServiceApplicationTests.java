package com.cffex.simulatedtradingtradeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class SimulatedTradingTradeServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(System.currentTimeMillis());
        System.out.println(new Date().getTime());
    }

}
