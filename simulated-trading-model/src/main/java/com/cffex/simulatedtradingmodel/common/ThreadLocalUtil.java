package com.cffex.simulatedtradingmodel.common;

public class ThreadLocalUtil {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    public static void setUserId(Integer userId) {
        threadLocal.set(userId+"");
    }
    public static Integer getUserId() {
        return Integer.parseInt(threadLocal.get());
    }
}
