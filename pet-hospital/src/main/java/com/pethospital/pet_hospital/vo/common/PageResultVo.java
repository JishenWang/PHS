package com.pethospital.pet_hospital.vo.common;

import java.util.List;

/**
 * 统一分页响应VO
 */
public class PageResultVo<T> {
    
    private Integer code;
    private String msg;
    private List<T> data;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    
    public PageResultVo() {}
    
    public PageResultVo(Integer code, String msg, List<T> data, Long total, Long pages, Long current, Long size) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
    }
    
    // 静态工厂方法
    public static <T> PageResultVo<T> success(List<T> data, Long total, Long current, Long size) {
        PageResultVo<T> result = new PageResultVo<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        result.setTotal(total);
        result.setPages((total + size - 1) / size);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }
    
    public static <T> PageResultVo<T> error(String msg) {
        PageResultVo<T> result = new PageResultVo<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
    
    // Getters and Setters
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    
    public Long getPages() { return pages; }
    public void setPages(Long pages) { this.pages = pages; }
    
    public Long getCurrent() { return current; }
    public void setCurrent(Long current) { this.current = current; }
    
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
}