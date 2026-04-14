package com.pethospital.pet_hospital.common.constant;

/**
 * HTTP状态码常量
 */
public class HttpStatus {

    // 成功
    public static final int SUCCESS = 200;
    
    // 客户端错误
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    
    // 服务器错误
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;
}