package com.pethospital.pet_hospital.common.exception;

/**
 * 业务异常类
 * 用于抛出业务逻辑相关的异常
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // ========== 常用预定义异常 ==========

    /**
     * 资源不存在
     */
    public static BusinessException notFound(String entityName) {
        return new BusinessException(404, entityName + "不存在");
    }

    /**
     * 资源已存在
     */
    public static BusinessException alreadyExists(String entityName) {
        return new BusinessException(409, entityName + "已存在");
    }

    /**
     * 权限不足
     */
    public static BusinessException permissionDenied() {
        return new BusinessException(403, "权限不足");
    }

    /**
     * 未登录或登录已过期
     */
    public static BusinessException unauthenticated() {
        return new BusinessException(401, "未登录或登录已过期");
    }

    /**
     * 参数无效
     */
    public static BusinessException invalidParam(String paramName) {
        return new BusinessException(400, paramName + "参数无效");
    }

    /**
     * 操作失败
     */
    public static BusinessException operationFailed(String reason) {
        return new BusinessException(500, "操作失败：" + reason);
    }

    /**
     * 登录失败
     */
    public static BusinessException loginFailed() {
        return new BusinessException(401, "用户名或密码错误");
    }

    /**
     * Token过期
     */
    public static BusinessException tokenExpired() {
        return new BusinessException(401, "登录已过期，请重新登录");
    }

    /**
     * 数据校验失败
     */
    public static BusinessException validationFailed(String message) {
        return new BusinessException(400, message);
    }
}
