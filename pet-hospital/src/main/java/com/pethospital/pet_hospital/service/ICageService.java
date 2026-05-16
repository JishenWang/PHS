package com.pethospital.pet_hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethospital.pet_hospital.entity.Cage;

import java.util.List;

public interface ICageService extends IService<Cage> {

    Page<Cage> pageQuery(Integer current, Integer size, String cageNo, String area, Integer status);

    List<Cage> listAvailable();
}
