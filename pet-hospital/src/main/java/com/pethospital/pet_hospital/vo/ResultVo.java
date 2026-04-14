package com.pethospital.pet_hospital.vo;

import com.pethospital.pet_hospital.common.constant.HttpStatus;
import lombok.Data;

/**
 * 统一响应结果类（泛型）
 */
@Data
public class ResultVo<T> {

    private Integer code;
    private String message;
    private T data;

    public ResultVo() {
    }

    public ResultVo(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功响应 ==========
    public static <T> ResultVo<T> success() {
        return new ResultVo<>(HttpStatus.SUCCESS, "操作成功", null);
    }

    public static <T> ResultVo<T> success(T data) {
        return new ResultVo<>(HttpStatus.SUCCESS, "操作成功", data);
    }

    public static <T> ResultVo<T> success(String message, T data) {
        return new ResultVo<>(HttpStatus.SUCCESS, message, data);
    }

    // ========== 失败响应 ==========
    public static <T> ResultVo<T> error() {
        return new ResultVo<>(HttpStatus.INTERNAL_SERVER_ERROR, "操作失败", null);
    }

    public static <T> ResultVo<T> error(String message) {
        return new ResultVo<>(HttpStatus.BAD_REQUEST, message, null);
    }

    public static <T> ResultVo<T> error(Integer code, String message) {
        return new ResultVo<>(code, message, null);
    }
}
