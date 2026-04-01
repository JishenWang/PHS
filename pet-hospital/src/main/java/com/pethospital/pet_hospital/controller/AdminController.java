package com.pethospital.pet_hospital.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // 新增：管理端连通测试接口
    @GetMapping("/test/info")
    public String testAdminConnect() {
        return "✅ 管理端前后端连通成功！";
    }

    // 👇 你原有的Admin业务代码，全部保留不动
}