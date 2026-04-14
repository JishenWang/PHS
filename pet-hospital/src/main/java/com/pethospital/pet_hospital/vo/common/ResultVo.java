package com.pethospital.pet_hospital.vo.common;

public class ResultVo<T> {
    
    private Integer code;
    private String msg;
    private T data;
    
    public ResultVo() {}
    
    public ResultVo(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    public static <T> ResultVo<T> success(T data) {
        return new ResultVo<>(200, "操作成功", data);
    }
    
    public static <T> ResultVo<T> success(String msg, T data) {
        return new ResultVo<>(200, msg, data);
    }
    
    public static <T> ResultVo<T> success() {
        return new ResultVo<>(200, "操作成功", null);
    }
    
    public static <T> ResultVo<T> error(String msg) {
        return new ResultVo<>(500, msg, null);
    }
    
    public static <T> ResultVo<T> error(Integer code, String msg) {
        return new ResultVo<>(code, msg, null);
    }
    
    public static <T> ResultVo<T> badRequest(String msg) {
        return new ResultVo<>(400, msg, null);
    }
    
    public static <T> ResultVo<T> unauthorized(String msg) {
        return new ResultVo<>(401, msg, null);
    }
    
    public static <T> ResultVo<T> forbidden(String msg) {
        return new ResultVo<>(403, msg, null);
    }
    
    // Getters and Setters
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}