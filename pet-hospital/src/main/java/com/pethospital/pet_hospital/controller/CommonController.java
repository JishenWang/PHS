package com.pethospital.pet_hospital.controller;

import com.pethospital.pet_hospital.service.ICommonService;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公共控制器
 * 提供文件上传等公共服务接口
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    private final ICommonService commonService;

    public CommonController(ICommonService commonService) {
        this.commonService = commonService;
    }

    /**
     * 单文件上传
     *
     * @param file 上传的文件
     * @param type 文件类型（可选，如 avatar、image 等）
     * @return 上传后的文件访问URL
     */
    @PostMapping("/upload")
    public ResultVo<String> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "type", required = false) String type) {
        String url = commonService.uploadFile(file, type);
        return ResultVo.success(url);
    }
}
