package com.pethospital.pet_hospital.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页查询DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
public class PageQueryDto {
    
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Integer status;
    private String startTime;
    private String endTime;
    private String orderBy;
    private String orderDirection = "DESC";
    
    public PageQueryDto() {}
    
    public PageQueryDto(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    
    @Min(value = 1, message = "页码最小为1")
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
    
    public String getOrderDirection() {
        return orderDirection;
    }
    
    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }
}