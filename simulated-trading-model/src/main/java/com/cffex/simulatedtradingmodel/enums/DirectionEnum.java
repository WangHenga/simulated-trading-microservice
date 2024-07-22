package com.cffex.simulatedtradingmodel.enums;

public enum DirectionEnum {
    CALL(0, "看涨(买)"),
    PUT(1, "看跌(卖)");
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    DirectionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
