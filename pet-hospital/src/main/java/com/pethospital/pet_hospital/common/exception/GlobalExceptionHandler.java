package com.pethospital.pet_hospital.common.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pethospital.pet_hospital.vo.ResultVo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 * 统一处理各种异常，返回标准格式响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVo<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultVo.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVo<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return ResultVo.error(400, message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResultVo<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return ResultVo.error(400, message);
    }

    /**
     * 约束校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResultVo<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败: {}", message);
        return ResultVo.error(400, message);
    }

    /**
     * 认证异常（密码错误等）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResultVo<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ResultVo.error(401, "用户名或密码错误");
    }

    /**
     * 权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResultVo<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResultVo.error(403, "权限不足，无法访问");
    }

    /**
     * 数据库唯一键冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResultVo<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        return ResultVo.error(409, "数据已存在，请勿重复提交");
    }

    /**
     * SQL完整性约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResultVo<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.warn("数据完整性约束违反: {}", e.getMessage());
        return ResultVo.error(400, "数据操作失败，请检查关联数据");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResultVo<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ResultVo.error(400, e.getMessage());
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResultVo<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return ResultVo.error(500, "系统内部错误");
    }

    /**
     * 其他未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public ResultVo<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResultVo.error(500, "系统繁忙，请稍后重试");
    }
}
