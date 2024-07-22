package com.cffex.simulatedtradinguserservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.entity.User;
import com.cffex.simulatedtradingmodel.vo.LoginUserVO;
import com.cffex.simulatedtradingmodel.vo.RegisterUserVO;
import com.cffex.simulatedtradingmodel.vo.UserAccountVO;

/**
* @author 17204
* @description 针对表【user(用户信息表)】的数据库操作Service
* @createDate 2024-07-08 14:33:48
*/
public interface UserService extends IService<User> {

    LoginUserVO login(String account, String password);

    RegisterUserVO register(String account, String password, String checkPassword);

    boolean logout(String token);

    UserAccountVO getAccountInfo(String account);
}
