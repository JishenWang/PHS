package com.pethospital.pet_hospital.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        initOperationLogTable();
        initFeedbackTable();
        initMedicineItemExpiryDate();
    }

    private void initFeedbackTable() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'feedback'",
                Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute(
                    "CREATE TABLE `feedback` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `user_id` bigint DEFAULT NULL," +
                    "  `user_name` varchar(50) DEFAULT NULL," +
                    "  `type` varchar(20) DEFAULT NULL," +
                    "  `content` text," +
                    "  `contact` varchar(100) DEFAULT NULL," +
                    "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`) USING BTREE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户反馈表'"
                );
                log.info("feedback 表创建成功");
            } else {
                log.info("feedback 表已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("初始化 feedback 表失败", e);
        }
    }

    private void initMedicineItemExpiryDate() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'medicine_item' AND column_name = 'expiry_date'",
                Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE medicine_item ADD COLUMN expiry_date DATE DEFAULT NULL COMMENT '保质期'");
                log.info("medicine_item 表添加 expiry_date 列成功");
            } else {
                log.info("medicine_item 表已存在 expiry_date 列，跳过");
            }
        } catch (Exception e) {
            log.error("初始化 medicine_item expiry_date 列失败", e);
        }
    }

    private void initOperationLogTable() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'operation_log'",
                Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute(
                    "CREATE TABLE `operation_log` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `action` varchar(100) DEFAULT NULL," +
                    "  `action_type` varchar(50) DEFAULT NULL," +
                    "  `detail` varchar(500) DEFAULT NULL," +
                    "  `operator` varchar(50) DEFAULT NULL," +
                    "  `ip` varchar(64) DEFAULT NULL," +
                    "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`) USING BTREE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表'"
                );
                log.info("operation_log 表创建成功");
            } else {
                log.info("operation_log 表已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("初始化 operation_log 表失败", e);
        }
    }
}
