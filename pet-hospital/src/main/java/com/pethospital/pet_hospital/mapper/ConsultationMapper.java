// 咨询+回复（在线问诊）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConsultationMapper {
    Consultation getById(@Param("id") Long id);

    List<Consultation> listAll();

    // 聚合咨询主表 + 回复表（consultation_reply）
    List<Map<String, Object>> listWithLatestReply();

    List<Map<String, Object>> listRepliesByConsultationId(@Param("consultationId") Long consultationId);

    // 兼容另一套在线咨询表（consult）
    List<Map<String, Object>> listConsultLegacy();
}
