package com.cffex.simulatedtradingmodel.enums;

public enum OrderStatusEnum {
    UNFILLED(0, "未成交"),
    PARTIALLY_FILLED(1, "部分成交"),
    FULLY_FILLED(2, "全部成交"),
    CANCEL(3, "撤单");
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    OrderStatusEnum(int code, String message) {
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
