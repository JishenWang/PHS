package com.pethospital.pet_hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethospital.pet_hospital.entity.Cage;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.Hospitalization;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.entity.PrescriptionItem;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.CageMapper;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.HospitalizationMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionItemMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IHospitalizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class HospitalizationServiceImpl extends ServiceImpl<HospitalizationMapper, Hospitalization> implements IHospitalizationService {

    @Autowired
    private CageMapper cageMapper;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Hospitalization createHospitalization(Hospitalization hospitalization) {
        // 生成住院单号
        hospitalization.setHospNo("H" + System.currentTimeMillis());
        hospitalization.setStatus(0);
        hospitalization.setPayStatus(0);
        hospitalization.setIsDeleted(0);
        if (hospitalization.getBedFeePerDay() == null) {
            hospitalization.setBedFeePerDay(BigDecimal.ZERO);
        }
        hospitalization.setTotalBedFee(BigDecimal.ZERO);
        hospitalization.setPrescriptionAmount(BigDecimal.ZERO);
        hospitalization.setTotalAmount(BigDecimal.ZERO);
        hospitalization.setAdmissionTime(LocalDateTime.now());

        this.save(hospitalization);

        // 占用笼子
        Cage cage = new Cage();
        cage.setId(hospitalization.getCageId());
        cage.setStatus(1);
        cage.setCurrentPetId(hospitalization.getPetId());
        cageMapper.updateById(cage);

        return hospitalization;
    }

    @Override
    public Page<Hospitalization> pageQuery(Integer current, Integer size, Long ownerId, Long doctorId, Integer status) {
        Page<Hospitalization> page = new Page<>(current, size);
        LambdaQueryWrapper<Hospitalization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospitalization::getIsDeleted, 0);
        if (ownerId != null) {
            wrapper.eq(Hospitalization::getOwnerId, ownerId);
        }
        if (doctorId != null) {
            wrapper.eq(Hospitalization::getDoctorId, doctorId);
        }
        if (status != null) {
            wrapper.eq(Hospitalization::getStatus, status);
        }
        wrapper.orderByDesc(Hospitalization::getCreateTime);
        Page<Hospitalization> result = this.page(page, wrapper);
        // 填充宠物名、医生名、主人名，以及支付状态
        for (Hospitalization h : result.getRecords()) {
            if (h.getPetId() != null) {
                Pet pet = petMapper.selectById(h.getPetId());
                if (pet != null) h.setPetName(pet.getName());
            }
            if (h.getDoctorId() != null) {
                Doctor doctor = doctorMapper.selectById(h.getDoctorId());
                if (doctor != null) h.setDoctorName(doctor.getName());
            }
            if (h.getOwnerId() != null) {
                User user = userMapper.selectById(h.getOwnerId());
                if (user != null) h.setOwnerName(user.getRealName());
            }
            // 查询该住院记录是否有已支付的订单
            Integer paidCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_info WHERE hospitalization_id = ? AND pay_status = 1", Integer.class, h.getId());
            if (paidCount != null && paidCount > 0) {
                h.setPayStatus(1);
            }
            // 兼容旧数据：已出院但 totalAmount 为 0 的，重新计算总费用
            if (h.getStatus() != null && h.getStatus() == 1 && (h.getTotalAmount() == null || h.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)) {
                BigDecimal bedFee = h.getTotalBedFee() != null ? h.getTotalBedFee() : BigDecimal.ZERO;
                LambdaQueryWrapper<Prescription> pw = new LambdaQueryWrapper<>();
                pw.eq(Prescription::getHospitalizationId, h.getId())
                  .eq(Prescription::getIsDeleted, 0)
                  .eq(Prescription::getStatus, 1);
                List<Prescription> prescriptions = prescriptionMapper.selectList(pw);
                BigDecimal prescriptionAmount = prescriptions.stream()
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                h.setTotalAmount(bedFee.add(prescriptionAmount));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Hospitalization discharge(Long hospId, BigDecimal bedFeePerDay) {
        Hospitalization hosp = this.getById(hospId);
        if (hosp == null || hosp.getIsDeleted() == 1) {
            throw new RuntimeException("住院记录不存在");
        }
        if (hosp.getStatus() != 0) {
            throw new RuntimeException("该住院记录已处理");
        }

        // 计算实际住院天数
        LocalDateTime admissionTime = hosp.getAdmissionTime();
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(admissionTime.toLocalDate(), now.toLocalDate());
        if (days < 1) days = 1;

        BigDecimal fee = bedFeePerDay != null ? bedFeePerDay : hosp.getBedFeePerDay();
        if (fee == null) fee = BigDecimal.ZERO;

        // 查询该住院记录下的处方总金额
        LambdaQueryWrapper<Prescription> presWrapper = new LambdaQueryWrapper<>();
        presWrapper.eq(Prescription::getHospitalizationId, hospId)
                   .eq(Prescription::getIsDeleted, 0)
                   .eq(Prescription::getStatus, 1); // 已提交的处方
        List<Prescription> prescriptions = prescriptionMapper.selectList(presWrapper);
        BigDecimal prescriptionAmount = prescriptions.stream()
            .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBedFee = fee.multiply(BigDecimal.valueOf(days));
        BigDecimal grossAmount = totalBedFee.add(prescriptionAmount);

        // 查询已支付的阶段性缴费总额
        List<Map<String, Object>> paidRows = jdbcTemplate.queryForList(
            "SELECT SUM(paid_amount) as paid FROM order_info WHERE hospitalization_id = ? AND pay_status = 1 AND order_no LIKE 'HI%'", hospId);
        BigDecimal interimPaid = BigDecimal.ZERO;
        if (!paidRows.isEmpty() && paidRows.get(0).get("paid") != null) {
            interimPaid = new BigDecimal(paidRows.get(0).get("paid").toString());
        }

        BigDecimal payableAmount = grossAmount.subtract(interimPaid);
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) payableAmount = BigDecimal.ZERO;

        hosp.setActualDays((int) days);
        hosp.setBedFeePerDay(fee);
        hosp.setTotalBedFee(totalBedFee);
        hosp.setTotalAmount(grossAmount); // 费用合计保存总费用
        hosp.setStatus(1);

        this.updateById(hosp);

        // 释放笼子
        Cage cage = new Cage();
        cage.setId(hosp.getCageId());
        cage.setStatus(0);
        cage.setCurrentPetId(null);
        cageMapper.updateById(cage);

        // 创建订单记录：total_amount = 总费用，payable_amount = 扣除已缴后的应付金额
        String orderNo = "HO" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO order_info(order_no, hospitalization_id, owner_user_id, pet_id, doctor_id, total_amount, discount_amount, reduction_amount, adjust_amount, payable_amount, paid_amount, pay_status, pay_status_text, remark, create_time, update_time, created_time, updated_time) " +
            "VALUES(?,?,?,?,?,?,0,0,0,?,0,0,'pending',?,NOW(),NOW(),NOW(),NOW())",
            orderNo, hospId, hosp.getOwnerId(), hosp.getPetId(), hosp.getDoctorId(),
            grossAmount.doubleValue(), payableAmount.doubleValue(),
            "住院结算：床位费 " + totalBedFee + " + 处方费 " + prescriptionAmount + " - 已缴 " + interimPaid + "，共 " + days + " 天"
        );

        Long orderId = jdbcTemplate.queryForObject(
            "SELECT id FROM order_info WHERE order_no = ?",
            Long.class, orderNo
        );

        if (orderId != null) {
            hosp.setOrderId(orderId);
            this.updateById(hosp);
        }

        return hosp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> interimCharge(Long hospId) {
        Hospitalization hosp = this.getById(hospId);
        if (hosp == null || hosp.getIsDeleted() == 1) {
            throw new RuntimeException("住院记录不存在");
        }
        if (hosp.getStatus() != 0) {
            throw new RuntimeException("该住院记录已出院，无法进行阶段性缴费");
        }

        // 计算已住院天数
        LocalDateTime admissionTime = hosp.getAdmissionTime();
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(admissionTime.toLocalDate(), now.toLocalDate());
        if (days < 1) days = 1;

        BigDecimal fee = hosp.getBedFeePerDay() != null ? hosp.getBedFeePerDay() : BigDecimal.ZERO;

        // 查询该住院记录下已提交的处方总金额
        LambdaQueryWrapper<Prescription> presWrapper = new LambdaQueryWrapper<>();
        presWrapper.eq(Prescription::getHospitalizationId, hospId)
                   .eq(Prescription::getIsDeleted, 0)
                   .eq(Prescription::getStatus, 1);
        List<Prescription> prescriptions = prescriptionMapper.selectList(presWrapper);
        BigDecimal prescriptionAmount = prescriptions.stream()
            .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBedFee = fee.multiply(BigDecimal.valueOf(days));
        BigDecimal totalAmount = totalBedFee.add(prescriptionAmount);

        // 查询该住院已生成的订单累计已付金额
        List<java.util.Map<String, Object>> paidRows = jdbcTemplate.queryForList(
            "SELECT SUM(paid_amount) as paid FROM order_info WHERE hospitalization_id = ? AND pay_status = 1",
            hospId
        );
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (!paidRows.isEmpty() && paidRows.get(0).get("paid") != null) {
            paidAmount = new BigDecimal(paidRows.get(0).get("paid").toString());
        }

        BigDecimal payableAmount = totalAmount.subtract(paidAmount);
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        // 生成阶段性缴费订单
        String orderNo = "HI" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO order_info(order_no, hospitalization_id, owner_user_id, pet_id, doctor_id, total_amount, discount_amount, reduction_amount, adjust_amount, payable_amount, paid_amount, pay_status, pay_status_text, remark, create_time, update_time, created_time, updated_time) " +
            "VALUES(?,?,?,?,?,?,0,0,0,?,0,0,'pending',?,NOW(),NOW(),NOW(),NOW())",
            orderNo, hospId, hosp.getOwnerId(), hosp.getPetId(), hosp.getDoctorId(),
            payableAmount.doubleValue(), payableAmount.doubleValue(),
            "阶段性缴费：床位费 " + totalBedFee + " + 处方费 " + prescriptionAmount + " - 已付 " + paidAmount + "，共 " + days + " 天"
        );

        Long orderId = jdbcTemplate.queryForObject(
            "SELECT id FROM order_info WHERE order_no = ?",
            Long.class, orderNo
        );

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", orderNo);
        result.put("totalBedFee", totalBedFee);
        result.put("prescriptionAmount", prescriptionAmount);
        result.put("paidAmount", paidAmount);
        result.put("payableAmount", payableAmount);
        result.put("days", days);
        return result;
    }

    @Override
    public List<Prescription> listPrescriptionsByHospId(Long hospId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getHospitalizationId, hospId)
               .eq(Prescription::getIsDeleted, 0)
               .orderByDesc(Prescription::getCreateTime);
        List<Prescription> list = prescriptionMapper.selectList(wrapper);
        for (Prescription p : list) {
            LambdaQueryWrapper<PrescriptionItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(PrescriptionItem::getPrescriptionId, p.getId())
                       .eq(PrescriptionItem::getItemType, "SERVICE")
                       .orderByAsc(PrescriptionItem::getSortNo);
            List<PrescriptionItem> items = prescriptionItemMapper.selectList(itemWrapper);
            if (!items.isEmpty()) {
                String names = items.stream()
                    .map(PrescriptionItem::getItemName)
                    .filter(name -> name != null && !name.isEmpty())
                    .collect(java.util.stream.Collectors.joining(", "));
                p.setServiceNames(names);
            }
        }
        return list;
    }
}
