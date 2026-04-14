package com.pethospital.pet_hospital.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {

    PENDING(0, "pending", "待支付"),
    PAID(1, "paid", "已支付"),
    CANCELLED(2, "cancelled", "已取消"),
    REFUNDING(3, "refunding", "退款中"),
    REFUNDED(4, "refunded", "已退款");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String value;
    
    private final String description;

    OrderStatusEnum(Integer code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatusEnum fromCode(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
    
    public static OrderStatusEnum fromValue(String value) {
        for (OrderStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return PENDING;
    }

    /**
     * 是否为可支付状态
     */
    public boolean isPayable() {
        return this == PENDING;
    }

    /**
     * 是否为可退款状态
     */
    public boolean isRefundable() {
        return this == PAID;
    }
}