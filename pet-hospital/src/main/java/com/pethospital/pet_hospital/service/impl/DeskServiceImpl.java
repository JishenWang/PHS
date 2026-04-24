package com.pethospital.pet_hospital.service.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pethospital.pet_hospital.dto.desk.ChargeQueryDto;
import com.pethospital.pet_hospital.dto.desk.CustomerQueryDto;
import com.pethospital.pet_hospital.dto.desk.RegisterQueryDto;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.OrderInfoMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.RegisterRecordMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IDeskService;

@Service
public class DeskServiceImpl implements IDeskService {
    private static final Logger log = LoggerFactory.getLogger(DeskServiceImpl.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private RegisterRecordMapper registerRecordMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String ping() {
        return "desk service ready";
    }

    @Override
    public Map<String, Object> queryCustomers(CustomerQueryDto queryDto) {
        int page = safePage(queryDto.getPage());
        int pageSize = safePageSize(queryDto.getPageSize());
        String keyword = pickKeyword(queryDto.getKeyword(), queryDto.getName(), queryDto.getPhone(), queryDto.getPetName());
        List<Map<String, Object>> all = new ArrayList<>();
        try {
            List<User> users = userMapper.listByNameOrPhone(keyword);
            for (User user : users) {
                if (user == null) {
                    continue;
                }
                if (!isMatchUser(user, queryDto)) {
                    continue;
                }
                List<Pet> pets = petMapper.listByOwnerId(user.getId());
                if (!isMatchPet(pets, queryDto.getPetName(), queryDto.getKeyword())) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", String.valueOf(user.getId()));
                item.put("name", user.getRealName() == null ? user.getUsername() : user.getRealName());
                item.put("phone", user.getPhone());
                item.put("address", "");
                item.put("isTemp", false);
                item.put("pets", pets == null ? new ArrayList<>() : pets);
                all.add(item);
            }
        } catch (Exception ex) {
            log.error("queryCustomers failed, fallback to empty list", ex);
        }
        return paginate(all, page, pageSize);
    }

    @Override
    public Map<String, Object> queryRegisters(RegisterQueryDto queryDto) {
        int page = safePage(queryDto.getPage());
        int pageSize = safePageSize(queryDto.getPageSize());
        int offset = (page - 1) * pageSize;
        Long doctorId = parseLong(queryDto.getDoctorId());
        List<Map<String, Object>> list;
        Long total;
        try {
            // 关键修复：将 String 类型的 status 转换为 Integer
            Integer status = null;
            String statusStr = queryDto.getStatus();
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    // 如果是数字字符串，直接转换
                    status = Integer.parseInt(statusStr);
                } catch (NumberFormatException e) {
                    // 如果是状态名称，进行映射
                    switch (statusStr) {
                        case "WAITING": status = 0; break;
                        case "IN_PROGRESS": status = 1; break;
                        case "DONE": status = 2; break;
                        case "CANCELED": status = 3; break;
                        default: status = null;
                    }
                }
            }
            
            list = registerRecordMapper.listDeskRegisters(
                    status,  // 传 Integer，不是 String
                    queryDto.getKeyword(),
                    doctorId,
                    offset,
                    pageSize
            );
            total = registerRecordMapper.countDeskRegisters(
                    status,  // 传 Integer，不是 String
                    queryDto.getKeyword(),
                    doctorId
            );
        } catch (Exception ex) {
            log.error("queryRegisters failed, fallback to empty list", ex);
            list = new ArrayList<>();
            total = 0L;
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> one = new HashMap<>(row);
            one.put("registerNo", firstNonBlank(row, "registerNo", "register_no"));
            one.put("customerName", firstNonBlank(row, "customerName", "ownerName", "owner_name"));
            one.put("phone", firstNonBlank(row, "phone", "ownerPhone", "owner_phone"));
            one.put("petName", firstNonBlank(row, "petName", "pet_name"));
            one.put("petSpecies", firstNonBlank(row, "petSpecies", "petType", "pet_species", "pet_type"));
            one.put("doctorName", firstNonBlank(row, "doctorName", "doctor_name"));
            one.put("serviceType", firstNonBlank(row, "serviceType", "service_type", "serviceName"));
            one.put("reason", firstNonBlank(row, "reason", "symptom", "triage_note"));
            one.put("visitTime", firstNonNull(row, "visitTime", "registerTime", "visit_time", "register_time"));
            
            // 兼容：status 可能是数字(0/1/2/3)或字符串(WAITING...)，统一转成字符串给前端
            Object rawStatus = row.get("status");
            if (rawStatus instanceof Number) {
                int s = ((Number) rawStatus).intValue();
                switch (s) {
                    case 0: one.put("status", "WAITING"); break;
                    case 1: one.put("status", "IN_PROGRESS"); break;
                    case 2: one.put("status", "DONE"); break;
                    case 3: one.put("status", "CANCELED"); break;
                    default: one.put("status", "WAITING");
                }
            }
            
            normalized.add(one);
        }
        return pageMap(normalized, total == null ? 0L : total, page, pageSize);
    }

    @Override
    public Map<String, Object> queryCharges(ChargeQueryDto queryDto) {
        int page = safePage(queryDto.getPage());
        int pageSize = safePageSize(queryDto.getPageSize());
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list;
        Long total;
        try {
            list = orderInfoMapper.listDeskCharges(
                    queryDto.getStatus(),
                    queryDto.getKeyword(),
                    offset,
                    pageSize
            );
            total = orderInfoMapper.countDeskCharges(
                    queryDto.getStatus(),
                    queryDto.getKeyword()
            );
        } catch (Exception ex) {
            log.error("queryCharges failed, fallback to empty list", ex);
            list = new ArrayList<>();
            total = 0L;
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> one = new HashMap<>(row);
            double totalAmount = parseDouble(one.get("total"));
            one.put("subtotal", totalAmount);
            one.put("reduction", 0);
            one.put("adjustAmount", 0);
            one.put("adjustReason", "");
            List<Map<String, Object>> detail = new ArrayList<>();
            Map<String, Object> line = new HashMap<>();
            line.put("name", "诊疗服务费");
            line.put("amount", totalAmount);
            detail.add(line);
            one.put("detail", detail);
            normalized.add(one);
        }
        return pageMap(normalized, total == null ? 0L : total, page, pageSize);
    }

    private Map<String, Object> paginate(List<Map<String, Object>> all, int page, int pageSize) {
        int fromIndex = Math.min((page - 1) * pageSize, all.size());
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        return pageMap(all.subList(fromIndex, toIndex), (long) all.size(), page, pageSize);
    }

    private Map<String, Object> pageMap(List<Map<String, Object>> list, Long total, int page, int pageSize) {
        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("total", total);
        res.put("page", page);
        res.put("pageSize", pageSize);
        return res;
    }

    private int safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String pickKeyword(String keyword, String name, String phone, String petName) {
        if (!isBlank(keyword)) {
            return keyword.trim();
        }
        if (!isBlank(name)) {
            return name.trim();
        }
        if (!isBlank(phone)) {
            return phone.trim();
        }
        if (!isBlank(petName)) {
            return petName.trim();
        }
        return null;
    }

    private boolean isMatchUser(User user, CustomerQueryDto queryDto) {
        String displayName = user.getRealName() == null ? user.getUsername() : user.getRealName();
        if (!contains(displayName, queryDto.getName())) {
            return false;
        }
        if (!contains(user.getPhone(), queryDto.getPhone())) {
            return false;
        }
        if (!isBlank(queryDto.getKeyword())
                && !contains(displayName, queryDto.getKeyword())
                && !contains(user.getPhone(), queryDto.getKeyword())) {
            return false;
        }
        return true;
    }

    private boolean isMatchPet(List<Pet> pets, String petName, String keyword) {
        if (pets == null) {
            return isBlank(petName) && isBlank(keyword);
        }
        if (!isBlank(petName)) {
            boolean hit = false;
            for (Pet pet : pets) {
                if (contains(pet.getName(), petName)) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                return false;
            }
        }
        if (!isBlank(keyword)) {
            boolean hit = false;
            for (Pet pet : pets) {
                if (contains(pet.getName(), keyword)) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                return true;
            }
        }
        return true;
    }

    private boolean contains(String text, String part) {
        if (isBlank(part)) {
            return true;
        }
        if (text == null) {
            return false;
        }
        return text.toLowerCase().contains(part.trim().toLowerCase());
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private Long parseLong(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0D;
        }
    }

    @Override
    public Map<String, Object> debugTableStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> stats = new ArrayList<>();
        String[] tables = new String[]{
                "sys_user", "pet", "doctor_profile", "register_record", "order_info"
        };
        for (String table : tables) {
            Map<String, Object> row = new HashMap<>();
            row.put("table", table);
            try {
                Long count = jdbcTemplate.queryForObject("select count(1) from " + table, Long.class);
                row.put("ok", true);
                row.put("count", count == null ? 0 : count);
            } catch (Exception ex) {
                row.put("ok", false);
                row.put("error", ex.getMessage());
            }
            stats.add(row);
        }
        result.put("list", stats);
        return result;
    }

    @Override
    public Map<String, Object> updateRegisterStatus(Long id, String status) {
        Map<String, Object> result = new HashMap<>();
        Integer dbStatus = toDbRegisterStatus(status);
        if (id == null || dbStatus == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        try {
            int updated = registerRecordMapper.updateStatusById(id, dbStatus);
            if (updated > 0 && dbStatus == 2) {
                // 完成就诊后确保产生待收费订单，打通“挂号 -> 收费 -> 订单核对”闭环。
                ensurePendingOrderForRegister(id);
            }
            result.put("success", updated > 0);
            result.put("message", updated > 0 ? "状态更新成功" : "挂号单不存在");
        } catch (Exception ex) {
            log.error("updateRegisterStatus failed, id={}, status={}", id, status, ex);
            result.put("success", false);
            result.put("message", "状态更新失败");
        }
        return result;
    }

    private Integer toDbRegisterStatus(String status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case "WAITING":
                return 0;
            case "IN_PROGRESS":
                return 1;
            case "DONE":
                return 2;
            case "CANCELED":
                return 3;
            default:
                return null;
        }
    }

    @Override
    public Map<String, Object> confirmCharge(Long id, String payMethod) {
        Map<String, Object> result = new HashMap<>();
        if (id == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        String method = isBlank(payMethod) ? "现金" : payMethod.trim();
        try {
            int updated = orderInfoMapper.confirmChargeById(id, method);
            result.put("success", updated > 0);
            result.put("message", updated > 0 ? "收费成功" : "收费单不存在");
        } catch (Exception ex) {
            log.error("confirmCharge failed, id={}, payMethod={}", id, method, ex);
            result.put("success", false);
            result.put("message", "收费失败");
        }
        return result;
    }

    @Override
    public Map<String, Object> createTempCustomer(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        String name = str(payload.get("name"));
        String phone = str(payload.get("phone"));
        String petName = str(payload.get("petName"));
        if (isBlank(name) || isBlank(phone) || isBlank(petName)) {
            result.put("success", false);
            result.put("message", "缺少必填项");
            return result;
        }
        try {
            String username = "temp_" + System.currentTimeMillis();
            jdbcTemplate.update(
                    "insert into sys_user(username,password,real_name,phone,role,role_code,status,is_deleted,created_time,updated_time,create_time,update_time) " +
                            "values(?,?,?,?,'owner','OWNER',1,0,now(),now(),now(),now())",
                    username, "", name, phone
            );
            Long ownerId = jdbcTemplate.queryForObject(
                    "select id from sys_user where phone=? order by id desc limit 1",
                    Long.class, phone
            );
            if (ownerId == null) {
                result.put("success", false);
                result.put("message", "创建客户失败");
                return result;
            }
            jdbcTemplate.update(
                    "insert into pet(owner_user_id,owner_id,owner_name,owner_phone,owner_is_temp,name,species,gender,age,allergy_history,is_deleted,created_time,updated_time,create_time,update_time) " +
                            "values(?,?,?,?,1,?,?,?,?,?,0,now(),now(),now(),now())",
                    ownerId, ownerId, name, phone, petName,
                    strOrDefault(payload.get("petSpecies"), "未知"),
                    strOrDefault(payload.get("petGender"), "未知"),
                    intOrDefault(payload.get("petAge"), 0),
                    str(payload.get("allergyHistory"))
            );
            result.put("success", true);
            result.put("id", String.valueOf(ownerId));
            result.put("name", name);
            result.put("phone", phone);
            return result;
        } catch (Exception ex) {
            log.error("createTempCustomer failed", ex);
            result.put("success", false);
            result.put("message", "创建失败");
            return result;
        }
    }

    @Override
    public Map<String, Object> getReserveList(Map<String, Object> params) {
        int page = safePage(parseInteger(params.get("page")));
        int pageSize = safePageSize(parseInteger(params.get("pageSize")));
        int offset = (page - 1) * pageSize;
        String status = str(params.get("status"));
        String keyword = str(params.get("keyword"));

        StringBuilder where = new StringBuilder(" a.is_deleted = 0 ");
        List<Object> args = new ArrayList<>();
        if (!isBlank(status)) {
            if ("BOOKED".equals(status)) {
                where.append(" and a.status in (0,1) ");
            } else if ("VERIFIED".equals(status)) {
                where.append(" and a.status = 2 ");
            } else if ("CANCELED".equals(status)) {
                where.append(" and a.status = 3 ");
            }
        }
        if (!isBlank(keyword)) {
            where.append(" and (su.real_name like ? or su.phone like ? or p.name like ? or dp.name like ?) ");
            String like = "%" + keyword + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }

        String baseSql = " from appointment a " +
                "left join sys_user su on su.id = ifnull(a.owner_user_id,a.owner_id) " +
                "left join pet p on p.id = a.pet_id " +
                "left join doctor_profile dp on dp.id = a.doctor_id " +
                "where " + where;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select a.id as id, a.appointment_time as reserveTime, su.real_name as customerName, su.phone as phone, " +
                        "p.id as petId, p.name as petName, p.species as petSpecies, dp.id as doctorId, dp.name as doctorName, " +
                        "ifnull(a.service_type,'普通门诊') as serviceType, " +
                        "case when a.status in (0,1) then 'BOOKED' when a.status=2 then 'VERIFIED' else 'CANCELED' end as status " +
                        baseSql + " order by a.id desc limit ?,?",
                mergeArgs(args, offset, pageSize)
        );
        Long total = jdbcTemplate.queryForObject("select count(1) " + baseSql, Long.class, args.toArray());
        return pageMap(list, total == null ? 0L : total, page, pageSize);
    }

    @Override
    public Map<String, Object> verifyReserve(Long reserveId) {
        Map<String, Object> result = new HashMap<>();
        if (reserveId == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, owner_user_id, owner_id, pet_id, doctor_id, service_item_id, service_type " +
                            "from appointment where id=? and is_deleted=0 limit 1",
                    reserveId
            );
            if (rows.isEmpty()) {
                result.put("success", false);
                result.put("message", "预约不存在");
                return result;
            }
            Map<String, Object> a = rows.get(0);
            Long ownerId = parseLongObj(a.get("owner_user_id"));
            if (ownerId == null) {
                ownerId = parseLongObj(a.get("owner_id"));
            }
            Long petId = parseLongObj(a.get("pet_id"));
            Long doctorId = parseLongObj(a.get("doctor_id"));
            if (ownerId == null || petId == null || doctorId == null) {
                result.put("success", false);
                result.put("message", "预约数据不完整");
                return result;
            }
            jdbcTemplate.update("update appointment set status=2, verified_time=now(), update_time=now(), updated_time=now() where id=?", reserveId);
            Long exist = jdbcTemplate.queryForObject("select count(1) from register_record where appointment_id=? and is_deleted=0", Long.class, reserveId);
            if (exist != null && exist == 0) {
                String registerNo = "RG" + System.currentTimeMillis();
                insertReserveRegisterWithCompatibility(
                        registerNo,
                        reserveId,
                        ownerId,
                        petId,
                        doctorId,
                        parseLongObj(a.get("service_item_id"))
                );
            }
            result.put("success", true);
            return result;
        } catch (Exception ex) {
            log.error("verifyReserve failed, reserveId={}", reserveId, ex);
            result.put("success", false);
            result.put("message", "核销失败");
            return result;
        }
    }

    @Override
    public Map<String, Object> createRegister(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        Long ownerId = parseIdFromPayload(payload, "customerId", "ownerId", "ownerUserId");
        Long petId = parseIdFromPayload(payload, "petId");
        Long doctorId = parseIdFromPayload(payload, "doctorId");
        if (doctorId == null) {
            doctorId = queryDoctorIdByName(str(payload.get("doctorName")));
        }
        if (ownerId == null || petId == null || doctorId == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        try {
            String registerNo = str(payload.get("registerNo"));
            if (isBlank(registerNo)) {
                registerNo = "RG" + System.currentTimeMillis();
            }
            String appointmentNo = "AP" + registerNo.replaceFirst("^RG", "");
            String reason = str(payload.get("reason"));
            Long serviceItemId = queryServiceItemIdByType(str(payload.get("serviceType")));
            Timestamp appointmentTime = parseVisitTime(payload.get("visitTime"));
            insertAppointmentWithCompatibility(
                    appointmentNo,
                    ownerId,
                    petId,
                    doctorId,
                    serviceItemId,
                    str(payload.get("serviceType")),
                    reason,
                    appointmentTime
            );
            result.put("success", true);
            result.put("reserveNo", appointmentNo);
            result.put("message", "预约已创建，请先在预约核销中完成核销");
            return result;
        } catch (Exception ex) {
            log.error("createRegister failed", ex);
            result.put("success", false);
            result.put("message", "创建挂号失败：" + friendlyDbError(ex));
            return result;
        }
    }

    @Override
    public Map<String, Object> updateChargePricing(Long id, Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        if (id == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        try {
            double subtotal = 0D;
            Object detailObj = payload.get("detail");
            if (detailObj instanceof List) {
                for (Object item : ((List<?>) detailObj)) {
                    if (item instanceof Map) {
                        subtotal += parseDouble(((Map<?, ?>) item).get("amount"));
                    }
                }
            } else {
                Double dbTotal = jdbcTemplate.queryForObject("select total_amount from order_info where id=?", Double.class, id);
                subtotal = dbTotal == null ? 0D : dbTotal;
            }
            double discount = parseDouble(payload.get("discount"));
            double reduction = parseDouble(payload.get("reduction"));
            double adjustAmount = parseDouble(payload.get("adjustAmount"));
            double payable = Math.max(subtotal - discount - reduction + adjustAmount, 0D);
            String adjustReason = str(payload.get("adjustReason"));
            int updated = jdbcTemplate.update(
                    "update order_info set total_amount=?,discount_amount=?,reduction_amount=?,adjust_amount=?,adjust_reason=?,payable_amount=?,update_time=now(),updated_time=now() where id=?",
                    subtotal, discount, reduction, adjustAmount, adjustReason, payable, id
            );
            result.put("success", updated > 0);
            result.put("message", updated > 0 ? "更新成功" : "收费单不存在");
            return result;
        } catch (Exception ex) {
            log.error("updateChargePricing failed, id={}", id, ex);
            result.put("success", false);
            result.put("message", "更新失败");
            return result;
        }
    }

    @Override
    public Map<String, Object> applyRefund(Long id, String reason) {
        Map<String, Object> result = new HashMap<>();
        if (id == null) {
            result.put("success", false);
            result.put("message", "参数无效");
            return result;
        }
        try {
            int updated = jdbcTemplate.update(
                    "update order_info set pay_status=2, pay_status_text='refunding', remark=concat(ifnull(remark,''),' 退款原因:',?), update_time=now(), updated_time=now() where id=? and pay_status=1",
                    strOrDefault(reason, ""), id
            );
            result.put("success", updated > 0);
            result.put("message", updated > 0 ? "退款申请成功" : "订单不存在或状态不允许");
            return result;
        } catch (Exception ex) {
            log.error("applyRefund failed, id={}", id, ex);
            result.put("success", false);
            result.put("message", "退款申请失败");
            return result;
        }
    }

    @Override
    public Map<String, Object> getOrderList(Map<String, Object> params) {
        int page = safePage(parseInteger(params.get("page")));
        int pageSize = safePageSize(parseInteger(params.get("pageSize")));
        int offset = (page - 1) * pageSize;
        String keyword = str(params.get("keyword"));

        String where = " where 1=1 ";
        List<Object> args = new ArrayList<>();
        if (!isBlank(keyword)) {
            where += " and (oi.order_no like ? or su.real_name like ? or ifnull(dp.name,'') like ? or ifnull(oi.pay_method,'') like ?) ";
            String like = "%" + keyword + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        String baseSql = " from order_info oi " +
                "left join sys_user su on su.id=oi.owner_user_id " +
                "left join doctor_profile dp on dp.id=oi.doctor_id " + where;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select oi.id as id, oi.order_no as orderNo, su.real_name as customerName, ifnull(dp.name,'') as doctorName, " +
                        "oi.pay_method as payMethod, ifnull(oi.paid_amount, oi.payable_amount) as total, oi.create_time as createdAt " +
                        baseSql + " order by oi.id desc limit ?,?",
                mergeArgs(args, offset, pageSize)
        );
        Long total = jdbcTemplate.queryForObject("select count(1) " + baseSql, Long.class, args.toArray());
        return pageMap(list, total == null ? 0L : total, page, pageSize);
    }

    @Override
    public Map<String, Object> getDoctorStatusList() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> doctors = jdbcTemplate.queryForList(
                    "select id, name, work_status from doctor_profile where is_deleted=0 order by id desc"
            );
            List<Map<String, Object>> busyRows = jdbcTemplate.queryForList(
                    "select distinct doctor_id from register_record where is_deleted=0 and status=1"
            );
            Map<Long, Boolean> busyMap = new HashMap<>();
            for (Map<String, Object> row : busyRows) {
                Long did = parseLongObj(row.get("doctor_id"));
                if (did != null) {
                    busyMap.put(did, true);
                }
            }
            for (Map<String, Object> d : doctors) {
                Long id = parseLongObj(d.get("id"));
                Integer workStatus = parseInteger(d.get("work_status"));
                String status;
                if (workStatus != null && workStatus == 0) {
                    status = "REST";
                } else if (id != null && busyMap.containsKey(id)) {
                    status = "BUSY";
                } else {
                    status = "FREE";
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", String.valueOf(id));
                item.put("name", str(d.get("name")));
                item.put("status", status);
                list.add(item);
            }
        } catch (Exception ex) {
            log.error("getDoctorStatusList failed", ex);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return result;
    }

    private String str(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private String strOrDefault(Object obj, String def) {
        String v = str(obj);
        return isBlank(v) ? def : v;
    }

    private int intOrDefault(Object obj, int def) {
        Integer i = parseInteger(obj);
        return i == null ? def : i;
    }

    private Integer parseInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongObj(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 兼容前端传入 "d_3001" / "3001" / 3001 等多种主键格式。
     */
    private Long parseFlexibleLong(Object obj) {
        if (obj == null) {
            return null;
        }
        String raw = String.valueOf(obj).trim();
        if (raw.isEmpty()) {
            return null;
        }
        Long direct = parseLongObj(raw);
        if (direct != null) {
            return direct;
        }
        String digits = raw.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return null;
        }
        return parseLongObj(digits);
    }

    private Long parseIdFromPayload(Map<String, Object> payload, String... keys) {
        if (payload == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Long id = parseFlexibleLong(payload.get(key));
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    private Long queryDoctorIdByName(String doctorName) {
        if (isBlank(doctorName)) {
            return null;
        }
        try {
            List<Long> ids = jdbcTemplate.query(
                    "select id from doctor_profile where is_deleted=0 and name=? order by id asc limit 1",
                    (rs, rowNum) -> rs.getLong("id"),
                    doctorName.trim()
            );
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception ex) {
            log.warn("queryDoctorIdByName failed, doctorName={}", doctorName, ex);
            return null;
        }
    }

    private Long queryServiceItemIdByType(String serviceType) {
        if (isBlank(serviceType)) {
            return null;
        }
        try {
            List<Long> ids = jdbcTemplate.query(
                    "select id from service_item where is_deleted=0 and (service_name=? or category=?) order by id asc limit 1",
                    (rs, rowNum) -> rs.getLong("id"),
                    serviceType.trim(),
                    serviceType.trim()
            );
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception ex) {
            log.warn("queryServiceItemIdByType failed, serviceType={}", serviceType, ex);
            return null;
        }
    }

    private void insertRegisterWithCompatibility(String registerNo,
                                                 Long ownerId,
                                                 Long petId,
                                                 Long doctorId,
                                                 Long serviceItemId,
                                                 String reason) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,service_item_id,register_time,symptom,status,amount,is_deleted,created_time,updated_time,create_time,update_time" +
                        ") values(?,?,?,?,?,now(),?,0,0,0,now(),now(),now(),now())"
        );
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,service_item_id,register_time,symptom,status,amount,is_deleted,create_time,update_time" +
                        ") values(?,?,?,?,?,now(),?,0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,service_item_id,register_time,symptom,status,amount,is_deleted,created_time,updated_time" +
                        ") values(?,?,?,?,?,now(),?,0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,register_time,symptom,status,amount,is_deleted,create_time,update_time" +
                        ") values(?,?,?,?,now(),?,0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,register_time,symptom,status,amount,is_deleted,created_time,updated_time" +
                        ") values(?,?,?,?,now(),?,0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(" +
                        "register_no,owner_user_id,pet_id,doctor_id,register_time,symptom,status,amount,is_deleted" +
                        ") values(?,?,?,?,now(),?,0,0,0)"
        );

        Exception last = null;
        for (String sql : sqlList) {
            try {
                if (sql.contains("service_item_id")) {
                    jdbcTemplate.update(sql, registerNo, ownerId, petId, doctorId, serviceItemId, reason);
                } else {
                    jdbcTemplate.update(sql, registerNo, ownerId, petId, doctorId, reason);
                }
                return;
            } catch (Exception ex) {
                last = ex;
                log.warn("insert register fallback failed, try next sql", ex);
            }
        }
        if (last instanceof RuntimeException) {
            throw (RuntimeException) last;
        }
        throw new RuntimeException(last);
    }

    private void insertAppointmentWithCompatibility(String appointmentNo,
                                                    Long ownerId,
                                                    Long petId,
                                                    Long doctorId,
                                                    Long serviceItemId,
                                                    String serviceType,
                                                    String reason,
                                                    Timestamp appointmentTime) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(
                "insert into appointment(" +
                        "appointment_no,owner_user_id,owner_id,pet_id,doctor_id,service_item_id,service_type,appointment_time,symptom_desc,source_type,status,status_text,is_deleted,created_time,updated_time,create_time,update_time" +
                        ") values(?,?,?,?,?,?,?,?,?,'DESK',0,'pending',0,now(),now(),now(),now())"
        );
        sqlList.add(
                "insert into appointment(" +
                        "appointment_no,owner_user_id,owner_id,pet_id,doctor_id,service_item_id,service_type,appointment_time,symptom_desc,source_type,status,status_text,is_deleted,create_time,update_time" +
                        ") values(?,?,?,?,?,?,?,?,?,'DESK',0,'pending',0,now(),now())"
        );
        sqlList.add(
                "insert into appointment(" +
                        "appointment_no,owner_user_id,owner_id,pet_id,doctor_id,service_item_id,service_type,appointment_time,symptom_desc,source_type,status,is_deleted,create_time,update_time" +
                        ") values(?,?,?,?,?,?,?,?,?,'DESK',0,0,now(),now())"
        );

        Exception last = null;
        for (String sql : sqlList) {
            try {
                jdbcTemplate.update(
                        sql,
                        appointmentNo,
                        ownerId,
                        ownerId,
                        petId,
                        doctorId,
                        serviceItemId,
                        strOrDefault(serviceType, "普通门诊"),
                        appointmentTime,
                        strOrDefault(reason, "")
                );
                return;
            } catch (Exception ex) {
                last = ex;
                log.warn("insert appointment fallback failed, try next sql", ex);
            }
        }
        if (last instanceof RuntimeException) {
            throw (RuntimeException) last;
        }
        throw new RuntimeException(last);
    }

    private Timestamp parseVisitTime(Object visitTime) {
        String raw = str(visitTime);
        if (isBlank(raw)) {
            return new Timestamp(System.currentTimeMillis());
        }
        try {
            return Timestamp.from(Instant.parse(raw));
        } catch (Exception ignore) {
            // continue with local datetime parse
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return Timestamp.valueOf(dt);
        } catch (Exception ignore) {
            // continue
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            return Timestamp.valueOf(dt);
        } catch (Exception ignore) {
            return Timestamp.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    private String friendlyDbError(Exception ex) {
        if (ex == null || ex.getMessage() == null) {
            return "数据库异常";
        }
        String msg = ex.getMessage();
        String lower = msg.toLowerCase();
        if (lower.contains("communications link failure")
                || lower.contains("connection")
                || lower.contains("jdbcconnection")) {
            return "数据库连接异常";
        }
        if (lower.contains("constraint") || lower.contains("foreign key")) {
            return "关联数据不存在";
        }
        if (lower.contains("unknown column")) {
            return "数据库字段不匹配";
        }
        return "数据库写入异常";
    }

    private void ensurePendingOrderForRegister(Long registerId) {
        if (registerId == null) {
            return;
        }
        Long exists = jdbcTemplate.queryForObject(
                "select count(1) from order_info where register_id=?",
                Long.class,
                registerId
        );
        if (exists != null && exists > 0) {
            return;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select rr.id, rr.appointment_id, rr.owner_user_id, rr.pet_id, rr.doctor_id, rr.service_item_id, rr.amount, " +
                        "si.price as service_price, si.service_name " +
                        "from register_record rr " +
                        "left join service_item si on si.id = rr.service_item_id " +
                        "where rr.id=? and rr.is_deleted=0 limit 1",
                registerId
        );
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> row = rows.get(0);
        Long ownerId = parseLongObj(row.get("owner_user_id"));
        if (ownerId == null) {
            return;
        }
        Long petId = parseLongObj(row.get("pet_id"));
        Long doctorId = parseLongObj(row.get("doctor_id"));
        Long appointmentId = parseLongObj(row.get("appointment_id"));
        double servicePrice = parseDouble(row.get("service_price"));
        double registerAmount = parseDouble(row.get("amount"));
        double payable = servicePrice > 0 ? servicePrice : (registerAmount > 0 ? registerAmount : 30D);
        String serviceName = strOrDefault(row.get("service_name"), "普通门诊");

        String orderNo = "OD" + System.currentTimeMillis() + ((int) (Math.random() * 90) + 10);
        jdbcTemplate.update(
                "insert into order_info(order_no,register_id,appointment_id,owner_user_id,pet_id,doctor_id,total_amount,discount_amount,reduction_amount,adjust_amount,payable_amount,paid_amount,pay_status,pay_status_text,remark,create_time,update_time,created_time,updated_time) " +
                        "values(?,?,?,?,?,?,?,0,0,0,?,0,0,'pending',?,now(),now(),now(),now())",
                orderNo, registerId, appointmentId, ownerId, petId, doctorId, payable, payable, "挂号完成待收费：" + serviceName
        );
    }

    private void insertReserveRegisterWithCompatibility(String registerNo,
                                                        Long reserveId,
                                                        Long ownerId,
                                                        Long petId,
                                                        Long doctorId,
                                                        Long serviceItemId) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add(
                "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,service_item_id,register_time,status,amount,is_deleted,created_time,updated_time,create_time,update_time) " +
                        "values(?,?,?,?,?,?,now(),0,0,0,now(),now(),now(),now())"
        );
        sqlList.add(
                "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,service_item_id,register_time,status,amount,is_deleted,create_time,update_time) " +
                        "values(?,?,?,?,?,?,now(),0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,service_item_id,register_time,status,amount,is_deleted,created_time,updated_time) " +
                        "values(?,?,?,?,?,?,now(),0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,register_time,status,amount,is_deleted,create_time,update_time) " +
                        "values(?,?,?,?,?,now(),0,0,0,now(),now())"
        );
        sqlList.add(
                "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,register_time,status,amount,is_deleted,created_time,updated_time) " +
                        "values(?,?,?,?,?,now(),0,0,0,now(),now())"
        );

        Exception last = null;
        for (String sql : sqlList) {
            try {
                if (sql.contains("service_item_id")) {
                    jdbcTemplate.update(sql, registerNo, reserveId, ownerId, petId, doctorId, serviceItemId);
                } else {
                    jdbcTemplate.update(sql, registerNo, reserveId, ownerId, petId, doctorId);
                }
                return;
            } catch (Exception ex) {
                last = ex;
                log.warn("insert reserve register fallback failed, try next sql", ex);
            }
        }
        if (last instanceof RuntimeException) {
            throw (RuntimeException) last;
        }
        throw new RuntimeException(last);
    }

    private Object[] mergeArgs(List<Object> args, Object... tail) {
        List<Object> merged = new ArrayList<>(args);
        for (Object t : tail) {
            merged.add(t);
        }
        return merged.toArray();
    }

    private String firstNonBlank(Map<String, Object> row, String... keys) {
        Object obj = firstNonNull(row, keys);
        if (obj == null) {
            return "";
        }
        String str = String.valueOf(obj);
        return isBlank(str) ? "" : str;
    }

    private Object firstNonNull(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (row.containsKey(key) && row.get(key) != null) {
                return row.get(key);
            }
        }
        return null;
    }
}
