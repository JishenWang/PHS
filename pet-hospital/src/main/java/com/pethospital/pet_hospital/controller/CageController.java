package com.pethospital.pet_hospital.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Cage;
import com.pethospital.pet_hospital.service.ICageService;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cage")
public class CageController {

    @Autowired
    private ICageService cageService;

    @GetMapping("/page")
    public ResultVo<Page<Cage>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String cageNo,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Integer status) {
        Page<Cage> result = cageService.pageQuery(current, size, cageNo, area, status);
        return ResultVo.success(result);
    }

    @GetMapping("/available")
    public ResultVo<List<Cage>> available() {
        List<Cage> list = cageService.listAvailable();
        return ResultVo.success(list);
    }

    @GetMapping("/{id}")
    public ResultVo<Cage> getById(@PathVariable Long id) {
        Cage cage = cageService.getById(id);
        return ResultVo.success(cage);
    }

    @PostMapping
    public ResultVo<String> save(@RequestBody Cage cage) {
        cage.setStatus(0);
        cage.setIsDeleted(0);
        boolean flag = cageService.save(cage);
        return flag ? ResultVo.success("添加成功") : ResultVo.error("添加失败");
    }

    @PutMapping
    public ResultVo<String> update(@RequestBody Cage cage) {
        boolean flag = cageService.updateById(cage);
        return flag ? ResultVo.success("更新成功") : ResultVo.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ResultVo<String> delete(@PathVariable Long id) {
        Cage cage = new Cage();
        cage.setId(id);
        cage.setIsDeleted(1);
        boolean flag = cageService.updateById(cage);
        return flag ? ResultVo.success("删除成功") : ResultVo.error("删除失败");
    }
}
