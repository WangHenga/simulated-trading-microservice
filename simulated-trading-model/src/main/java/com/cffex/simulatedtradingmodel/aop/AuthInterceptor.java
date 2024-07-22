package com.cffex.simulatedtradingmodel.aop;


import com.cffex.simulatedtradingmodel.annotation.AuthCheck;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.JwtUtil;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        String token = request.getHeader("token");
        Integer userId = JwtUtil.getUserIdFromToken(token);
        if(userId == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        String savedToken = (String)redisTemplate.opsForValue().get("user_"+userId.toString());
        if(!token.equals(savedToken)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        ThreadLocalUtil.setUserId(userId);
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
