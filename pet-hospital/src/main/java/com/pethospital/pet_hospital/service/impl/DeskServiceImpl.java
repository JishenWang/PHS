package com.pethospital.pet_hospital.service.impl;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            list = registerRecordMapper.listDeskRegisters(
                    queryDto.getStatus(),
                    queryDto.getKeyword(),
                    doctorId,
                    offset,
                    pageSize
            );
            total = registerRecordMapper.countDeskRegisters(
                    queryDto.getStatus(),
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
            one.put("serviceType", "普通门诊");
            one.put("reason", "");
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
                jdbcTemplate.update(
                        "insert into register_record(register_no,appointment_id,owner_user_id,pet_id,doctor_id,service_item_id,register_time,status,amount,is_deleted,created_time,updated_time,create_time,update_time) " +
                                "values(?,?,?,?,?,?,now(),0,0,0,now(),now(),now(),now())",
                        registerNo, reserveId, ownerId, petId, doctorId, a.get("service_item_id")
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
        Long ownerId = parseLongObj(payload.get("customerId"));
        Long petId = parseLongObj(payload.get("petId"));
        Long doctorId = parseLongObj(payload.get("doctorId"));
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
            String reason = str(payload.get("reason"));
            jdbcTemplate.update(
                    "insert into register_record(register_no,owner_user_id,pet_id,doctor_id,register_time,symptom,status,amount,is_deleted,created_time,updated_time,create_time,update_time) " +
                            "values(?,?,?,?,now(),?,0,0,0,now(),now(),now(),now())",
                    registerNo, ownerId, petId, doctorId, reason
            );
            result.put("success", true);
            result.put("registerNo", registerNo);
            return result;
        } catch (Exception ex) {
            log.error("createRegister failed", ex);
            result.put("success", false);
            result.put("message", "创建挂号失败");
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
                    "update order_info set pay_status=2, remark=concat(ifnull(remark,''),' 退款原因:',?), update_time=now(), updated_time=now() where id=? and pay_status=1",
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
                        "oi.pay_method as payMethod, oi.paid_amount as total, oi.created_time as createdAt " +
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

    private Object[] mergeArgs(List<Object> args, Object... tail) {
        List<Object> merged = new ArrayList<>(args);
        for (Object t : tail) {
            merged.add(t);
        }
        return merged.toArray();
    }
}
