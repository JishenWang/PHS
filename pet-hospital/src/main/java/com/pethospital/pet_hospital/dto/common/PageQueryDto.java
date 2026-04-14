package com.pethospital.pet_hospital.dto.common;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQueryDto {

    private Integer pageNum = 1;  // 当前页码

    private Integer pageSize = 10;  // 每页条数

    private String keyword;  // 搜索关键词
}
