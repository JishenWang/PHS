package com.pethospital.pet_hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethospital.pet_hospital.entity.Cage;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.mapper.CageMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.service.ICageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CageServiceImpl extends ServiceImpl<CageMapper, Cage> implements ICageService {

    @Autowired
    private PetMapper petMapper;

    @Override
    public Page<Cage> pageQuery(Integer current, Integer size, String cageNo, String area, Integer status) {
        Page<Cage> page = new Page<>(current, size);
        LambdaQueryWrapper<Cage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cage::getIsDeleted, 0);
        if (StringUtils.hasText(cageNo)) {
            wrapper.like(Cage::getCageNo, cageNo);
        }
        if (StringUtils.hasText(area)) {
            wrapper.like(Cage::getArea, area);
        }
        if (status != null) {
            wrapper.eq(Cage::getStatus, status);
        }
        wrapper.orderByAsc(Cage::getCageNo);
        Page<Cage> result = this.page(page, wrapper);
        for (Cage cage : result.getRecords()) {
            if (cage.getCurrentPetId() != null && cage.getStatus() != null && cage.getStatus() == 1) {
                Pet pet = petMapper.selectById(cage.getCurrentPetId());
                if (pet != null) {
                    cage.setCurrentPetName(pet.getName());
                }
            }
        }
        return result;
    }

    @Override
    public List<Cage> listAvailable() {
        LambdaQueryWrapper<Cage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cage::getIsDeleted, 0)
               .eq(Cage::getStatus, 0)
               .orderByAsc(Cage::getCageNo);
        return this.list(wrapper);
    }
}
