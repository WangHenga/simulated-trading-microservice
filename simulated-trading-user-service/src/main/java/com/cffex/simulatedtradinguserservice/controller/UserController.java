package com.cffex.simulatedtradinguserservice.controller;

import com.cffex.simulatedtradingmodel.annotation.AuthCheck;
import com.cffex.simulatedtradingmodel.common.BaseResponse;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.ResultUtils;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.entity.User;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradingmodel.dto.user.UserLoginRequest;
import com.cffex.simulatedtradingmodel.dto.user.UserRegisterRequest;
import com.cffex.simulatedtradingmodel.vo.LoginUserVO;
import com.cffex.simulatedtradingmodel.vo.RegisterUserVO;
import com.cffex.simulatedtradingmodel.vo.UserAccountVO;
import com.cffex.simulatedtradinguserservice.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String account = request.getAccount();
        String password = request.getPassword();
        if (StringUtils.isAnyBlank(account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.login(account, password);
        return ResultUtils.success(loginUserVO);
    }
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String token=request.getHeader("token");
        boolean result = userService.logout(token);
        return ResultUtils.success(result);
    }
    @PostMapping("/register")
    public BaseResponse<RegisterUserVO> register(@RequestBody UserRegisterRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String account = request.getAccount();
        String password = request.getPassword();
        String checkPassword = request.getCheckPassword();
        if (StringUtils.isAnyBlank(account, password,checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        RegisterUserVO loginUserVO = userService.register(account, password,checkPassword);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/getAccount")
    public BaseResponse<UserAccountVO> getAccount(@RequestParam("account") String account) {
        if (StringUtils.isBlank(account)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAccountVO userAccount = userService.getAccountInfo(account);
        return ResultUtils.success(userAccount);
    }

    @GetMapping("/getUserInfo")
    @AuthCheck
    public BaseResponse<UserAccountVO> getUserInfo(HttpServletRequest request) {
        Integer userId = ThreadLocalUtil.getUserId();
        User user = userService.getById(userId);
        UserAccountVO userAccountVO = new UserAccountVO();
        BeanUtils.copyProperties(user, userAccountVO);
        userAccountVO.setBalance(user.getBalance().toString());
        userAccountVO.setFrozenMargin(user.getFrozenMargin().toString());
        userAccountVO.setUsedMargin(user.getUsedMargin().toString());
        return ResultUtils.success(userAccountVO);
    }
}
