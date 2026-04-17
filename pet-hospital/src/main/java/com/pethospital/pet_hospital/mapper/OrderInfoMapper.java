package com.pethospital.pet_hospital.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.OrderInfo;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Update("UPDATE order_info SET pay_status = 1, pay_status_text = 'paid', pay_method = #{payMethod}, pay_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int confirmChargeById(@Param("id") Long id, @Param("payMethod") String payMethod);

    @Select("<script>" +
            "SELECT oi.id, oi.order_no as orderNo, su.real_name as customerName, oi.total_amount as total, oi.pay_status as payStatus, oi.create_time as createTime " +
            "FROM order_info oi " +
            "LEFT JOIN sys_user su ON su.id = oi.owner_user_id " +
            "WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND oi.pay_status_text = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (oi.order_no LIKE CONCAT('%', #{keyword}, '%') OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.phone LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY oi.id DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> listDeskCharges(@Param("status") String status, 
                                              @Param("keyword") String keyword, 
                                              @Param("offset") int offset, 
                                              @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM order_info oi " +
            "LEFT JOIN sys_user su ON su.id = oi.owner_user_id " +
            "WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND oi.pay_status_text = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (oi.order_no LIKE CONCAT('%', #{keyword}, '%') OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.phone LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    Long countDeskCharges(@Param("status") String status, @Param("keyword") String keyword);
}