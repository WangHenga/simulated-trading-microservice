package com.cffex.simulatedtradingserviceclient;

import com.cffex.simulatedtradingmodel.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "simulated-trading-user-service",path = "/user/inner")
public interface UserFeignClient {
    @PostMapping("/updateById")
    boolean updateById(@RequestBody User user);
    @GetMapping("/getById")
    User getById(@RequestParam("id") Integer id);
}
