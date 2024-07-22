package com.cffex.simulatedtradingmodel.enums;

public enum InstrumentStateEnum {
    PRE_LISTED(0, "未上市"),
    LISTED(1, "上市"),
    SUSPENDED(2, "停牌"),
    EXPIRATION(3, "到期");
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    InstrumentStateEnum(int code, String message) {
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
