package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;

/**
 * 宠物视图对象
 */
@Data
public class PetVo {

    private Long id;

    private String name;

    private String breed;

    private String genderText;  // 公/母

    private Integer age;  // 年龄（岁）

    private Double weight;

    private String birthday;

    private String chipNumber;

    private String neuteredText;  // 已绝育/未绝育

    private String avatar;

    private String description;
}
