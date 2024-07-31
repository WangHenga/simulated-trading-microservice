package com.cffex.simulatedtradinguserservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.JwtUtil;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradinguserservice.mapper.UserMapper;
import com.cffex.simulatedtradingmodel.entity.User;
import com.cffex.simulatedtradingmodel.vo.LoginUserVO;
import com.cffex.simulatedtradingmodel.vo.RegisterUserVO;
import com.cffex.simulatedtradingmodel.vo.UserAccountVO;
import com.cffex.simulatedtradinguserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
* @author 17204
* @description 针对表【user(用户信息表)】的数据库操作Service实现
* @createDate 2024-07-08 14:33:48
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserMapper userMapper;
    private static final String SALT = "1234567890";
    private static final Long EXPIRE_TIME = 1000L * 60 * 60 * 10;

    @Override
    public LoginUserVO login(String account, String password) {
        // 1. 校验
        if (StringUtils.isAnyBlank(account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        queryWrapper.eq("password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 生成token
        String token = JwtUtil.generateToken(user.getId(),EXPIRE_TIME);
        // 4. 存入redis
        redisTemplate.opsForValue().set("user_"+user.getId().toString(), token,EXPIRE_TIME, TimeUnit.MILLISECONDS);
        // 将用户的账户信息也存入redis
        if(!redisTemplate.hasKey("account_"+user.getId())){
            redisTemplate.opsForHash().put("account_"+user.getId().toString(), "balance", user.getBalance().toString());
            redisTemplate.opsForHash().put("account_"+user.getId().toString(), "frozenMargin", user.getFrozenMargin().toString());
            redisTemplate.opsForHash().put("account_"+user.getId().toString(), "usedMargin", user.getUsedMargin().toString());
        }
        // 5. 返回
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setAccount(account);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public RegisterUserVO register(String account, String password, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(account, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (account.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("account", account);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setAccount(account);
            user.setPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            RegisterUserVO registerUserVO = new RegisterUserVO();
            registerUserVO.setAccount(account);
            return registerUserVO;
        }
    }

    @Override
    public boolean logout(String token) {
        // 1. 获取token
        if(token == null) {
            return false;
        }
        Integer userId = JwtUtil.getUserIdFromToken(token);
        if (userId== null) {
            return false;
        }
        // 2. 删除redis
        String savedToken=(String)redisTemplate.opsForValue().get("user_"+userId);
        if (StringUtils.isBlank(savedToken)||!savedToken.equals(token)) {
            return false;
        }

        // 将redis中数据保存到数据库中
        BigDecimal balance=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "balance").toString());
        BigDecimal frozenMargin=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "frozenMargin").toString());
        BigDecimal usedMargin=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "usedMargin").toString());


        redisTemplate.delete("user_"+userId);
//        redisTemplate.delete("account_"+userId);

        User user = new User();
        user.setId(userId);
        user.setBalance(balance);
        user.setFrozenMargin(frozenMargin);
        user.setUsedMargin(usedMargin);

        this.updateById(user);

        return true;
    }

    @Override
    public UserAccountVO getAccountInfo(String account) {
        User user=userMapper.getUserByAccount(account);
        Integer id = user.getId();
        if(redisTemplate.hasKey("account_"+id)) {
            user.setBalance(new BigDecimal(redisTemplate.opsForHash().get("account_"+id, "balance").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            user.setFrozenMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+id, "frozenMargin").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            user.setUsedMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+id, "usedMargin").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            this.updateById(user);
        }
        UserAccountVO userAccountVO = new UserAccountVO();
        BeanUtils.copyProperties(user, userAccountVO);
        userAccountVO.setBalance(user.getBalance().toString());
        userAccountVO.setFrozenMargin(user.getFrozenMargin().toString());
        userAccountVO.setUsedMargin(user.getUsedMargin().toString());
        return userAccountVO;
    }

}




