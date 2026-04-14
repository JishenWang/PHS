package com.pethospital.pet_hospital.dto.common;

import lombok.Data;

/**
 * 文件上传响应
 */
@Data
public class UploadFileDto {

    private String fileName;

    private String fileUrl;

    private Long fileSize;
}
