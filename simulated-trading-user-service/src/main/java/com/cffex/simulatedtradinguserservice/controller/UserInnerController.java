package com.cffex.simulatedtradinguserservice.controller;

import com.cffex.simulatedtradingmodel.entity.User;
import com.cffex.simulatedtradinguserservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class UserInnerController {
    @Resource
    private UserService userService;
    @PostMapping("/updateById")
    boolean updateById(@RequestBody User user){
        return userService.updateById(user);
    }
    @GetMapping("/getById")
    User getById(@RequestParam("id") Integer id){
        return userService.getById(id);
    }
}
