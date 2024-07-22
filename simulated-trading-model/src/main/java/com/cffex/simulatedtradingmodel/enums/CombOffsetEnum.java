package com.cffex.simulatedtradingmodel.enums;

public enum CombOffsetEnum {
    OPEN(0, "开仓"),
    CLOSE(1, "平仓")/*,
    CLOSE_TODAY(2, "平今"),
    CLOSE_YESTERDAY(3, "平昨")*/;
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    CombOffsetEnum(int code, String message) {
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
