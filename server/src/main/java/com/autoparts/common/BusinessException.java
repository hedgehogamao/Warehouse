package com.autoparts.common;

/**
 * 业务异常
 * 用于在 Service 层抛出可预期的业务错误
 */
public class BusinessException extends RuntimeException {

    /** 状态码 */
    private final int code;

    /**
     * 使用状态码和消息构造
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用 ResultCode 构造
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用 ResultCode 和自定义消息构造
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public int getCode() { return code; }
}
