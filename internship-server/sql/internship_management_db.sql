/*
 Navicat Premium Dump SQL

 Source Server         : mysql8
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : internship_management_db

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 14/12/2025 11:39:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attendance
-- ----------------------------
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance`  (
  `attendance_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `group_id` bigint NULL DEFAULT NULL COMMENT '考勤组ID',
  `time_slot_id` bigint NULL DEFAULT NULL COMMENT '时间段ID',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `attendance_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '考勤类型：1-出勤，2-迟到，3-早退，4-请假，5-缺勤',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime NULL DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(5, 2) NULL DEFAULT NULL COMMENT '工作时长（小时）',
  `leave_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请假原因（请假时必填）',
  `leave_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请假类型（事假、病假、调休等）',
  `confirm_user_id` bigint NULL DEFAULT NULL COMMENT '确认人ID（企业导师或企业管理员）',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `confirm_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '确认状态：0-待确认，1-已确认，2-已拒绝',
  `confirm_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '确认意见',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`attendance_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_attendance_date`(`attendance_date` ASC) USING BTREE,
  INDEX `idx_attendance_type`(`attendance_type` ASC) USING BTREE,
  INDEX `idx_confirm_user_id`(`confirm_user_id` ASC) USING BTREE,
  INDEX `idx_confirm_status`(`confirm_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_time_slot_id`(`time_slot_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考勤表，用于存储学生实习期间的考勤记录，包括考勤日期、类型、签到签退时间等，支持出勤、迟到、早退、请假、缺勤、休息等多种考勤类型，企业导师可以确认考勤' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance
-- ----------------------------
INSERT INTO `attendance` VALUES (6, 67, 129, 22, NULL, NULL, '2025-11-28', 1, '2025-11-28 21:15:36', '2025-11-28 23:15:42', 2.00, NULL, NULL, 137, '2025-11-28 21:16:21', 1, NULL, 1, '2025-11-28 21:15:36', '2025-11-29 12:40:13');
INSERT INTO `attendance` VALUES (7, 67, 129, 22, 1, 1, '2025-11-29', 4, '2025-11-29 12:38:58', '2025-11-29 12:39:01', 0.00, '萨法说法 ', '事假', 137, '2025-11-28 21:16:02', 1, NULL, 1, '2025-11-28 21:15:48', '2025-11-29 12:40:13');
INSERT INTO `attendance` VALUES (8, 67, 129, 22, NULL, NULL, '2025-11-30', 6, NULL, NULL, NULL, NULL, NULL, 135, '2025-11-28 23:52:13', 1, NULL, 1, '2025-11-28 23:51:53', '2025-11-29 12:40:13');
INSERT INTO `attendance` VALUES (9, 67, 129, 22, 1, 1, '2025-11-29', 2, '2025-11-29 12:40:21', '2025-11-29 12:40:23', 0.00, NULL, NULL, 137, '2025-11-29 12:40:36', 1, NULL, 0, '2025-11-29 12:40:21', '2025-11-29 12:40:21');
INSERT INTO `attendance` VALUES (10, 67, 129, 22, NULL, NULL, '2025-11-30', 4, NULL, NULL, NULL, '测试', '病假', 137, '2025-11-29 14:09:15', 1, '通过', 0, '2025-11-29 13:58:36', '2025-11-29 13:58:36');
INSERT INTO `attendance` VALUES (11, 66, 128, 21, NULL, NULL, '2025-12-01', 3, '2025-12-01 06:54:41', '2025-12-01 06:54:44', 0.00, NULL, NULL, 128, '2025-12-01 06:54:41', 1, NULL, 0, '2025-12-01 06:54:41', '2025-12-01 06:54:41');

-- ----------------------------
-- Table structure for attendance_group
-- ----------------------------
DROP TABLE IF EXISTS `attendance_group`;
CREATE TABLE `attendance_group`  (
  `group_id` bigint NOT NULL AUTO_INCREMENT COMMENT '考勤组ID',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `mentor_id` bigint NOT NULL COMMENT '创建人（企业导师ID）',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考勤组名称',
  `group_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '考勤组描述',
  `work_days_type` int NOT NULL DEFAULT 1 COMMENT '工作日类型：1-周一到周五，2-周一到周六，3-周一到周日，4-自定义',
  `work_days_config` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作日配置（自定义时使用，JSON格式）',
  `default_work_hours` decimal(4, 2) NOT NULL DEFAULT 8.00 COMMENT '默认工作时长（小时）',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `delete_flag` int NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `update_user_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`group_id`) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_mentor_id`(`mentor_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤组表，用于存储企业创建的考勤组配置信息，包括考勤组名称、工作日类型、默认工作时长等，企业导师可以为实习学生创建考勤组，设置工作时间和考勤规则' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance_group
-- ----------------------------
INSERT INTO `attendance_group` VALUES (1, 10, 135, '测试', '测试萨法说法', 1, '', 8.00, 1, 0, '2025-11-29 12:32:31', '2025-11-29 12:32:31', 135, NULL);

-- ----------------------------
-- Table structure for attendance_group_rule
-- ----------------------------
DROP TABLE IF EXISTS `attendance_group_rule`;
CREATE TABLE `attendance_group_rule`  (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `group_id` bigint NOT NULL COMMENT '考勤组ID',
  `rule_type` int NOT NULL COMMENT '规则类型：1-工作日，2-节假日，3-特殊日期',
  `rule_date` date NULL DEFAULT NULL COMMENT '规则日期（单个日期）',
  `rule_start_date` date NULL DEFAULT NULL COMMENT '规则开始日期（日期范围）',
  `rule_end_date` date NULL DEFAULT NULL COMMENT '规则结束日期（日期范围）',
  `rule_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则值（JSON格式）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则描述',
  `delete_flag` int NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_rule_type`(`rule_type` ASC) USING BTREE,
  INDEX `idx_rule_date`(`rule_date` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤组规则表，用于存储考勤组的特殊规则，包括工作日、节假日、特殊日期等规则配置，支持单个日期和日期范围两种规则类型，用于灵活配置考勤规则' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance_group_rule
-- ----------------------------

-- ----------------------------
-- Table structure for attendance_group_student
-- ----------------------------
DROP TABLE IF EXISTS `attendance_group_student`;
CREATE TABLE `attendance_group_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `group_id` bigint NOT NULL COMMENT '考勤组ID',
  `apply_id` bigint NOT NULL COMMENT '实习申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `effective_start_date` date NOT NULL COMMENT '生效开始日期',
  `effective_end_date` date NULL DEFAULT NULL COMMENT '生效结束日期',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：1-生效，0-失效',
  `delete_flag` int NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤组学生关联表，用于存储学生与考勤组的关联关系，包括生效时间范围、状态等，一个学生可以关联一个考勤组，考勤组用于计算学生的考勤记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance_group_student
-- ----------------------------
INSERT INTO `attendance_group_student` VALUES (1, 1, 22, 67, '2025-09-11', '2026-01-05', 1, 0, '2025-11-29 12:32:56', '2025-11-29 12:32:56', 137);

-- ----------------------------
-- Table structure for attendance_group_time_slot
-- ----------------------------
DROP TABLE IF EXISTS `attendance_group_time_slot`;
CREATE TABLE `attendance_group_time_slot`  (
  `slot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `group_id` bigint NOT NULL COMMENT '考勤组ID',
  `slot_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '时间段名称（如：早班、晚班、标准班）',
  `start_time` time NOT NULL COMMENT '上班时间',
  `end_time` time NOT NULL COMMENT '下班时间',
  `work_hours` decimal(4, 2) NOT NULL COMMENT '工作时长（小时）',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认：1-是，0-否',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `delete_flag` int NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`slot_id`) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤组时间段表，用于存储考勤组的工作时间段配置，包括时间段名称、上班时间、下班时间、工作时长等，一个考勤组可以有多个时间段，支持早班、晚班、标准班等多种时间段配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance_group_time_slot
-- ----------------------------
INSERT INTO `attendance_group_time_slot` VALUES (1, 1, '标准班', '09:00:00', '18:00:00', 9.00, 1, 0, 0, '2025-11-29 12:32:31', '2025-11-29 12:32:31');
INSERT INTO `attendance_group_time_slot` VALUES (2, 1, '早班', '08:00:00', '16:00:00', 8.00, 0, 1, 0, '2025-11-29 12:32:31', '2025-11-29 12:32:31');

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info`  (
  `class_id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级名称',
  `class_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级代码',
  `major_id` bigint NULL DEFAULT NULL COMMENT '所属专业ID',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `class_teacher_id` bigint NULL DEFAULT NULL COMMENT '班主任ID',
  `share_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级分享码',
  `share_code_expire_time` datetime NULL DEFAULT NULL COMMENT '分享码过期时间',
  `share_code_generate_time` datetime NULL DEFAULT NULL COMMENT '分享码生成时间',
  `share_code_use_count` int NULL DEFAULT NULL COMMENT '分享码使用次数',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`class_id`) USING BTREE,
  UNIQUE INDEX `uk_class_code_major`(`class_code` ASC, `major_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_share_code`(`share_code` ASC) USING BTREE,
  INDEX `idx_major_id`(`major_id` ASC) USING BTREE,
  INDEX `idx_class_teacher_id`(`class_teacher_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `major_info` (`major_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_class_teacher` FOREIGN KEY (`class_teacher_id`) REFERENCES `user_info` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级信息表，用于存储班级的基本信息，包括班级名称、班级代码、所属专业、入学年份等，支持班级分享码功能，用于学生自主加入班级' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_info
-- ----------------------------
INSERT INTO `class_info` VALUES (20, '计算机科学与技术2021级1班', 'CS2021-1', 24, 2021, 126, NULL, NULL, NULL, 0, 1, 0, '2025-11-23 05:09:46', '2025-11-23 08:08:06');
INSERT INTO `class_info` VALUES (21, '软件工程2021级1班', 'SE2021-1', 25, 2021, 126, 'JPE33PUD', '2025-12-31 10:22:13', '2025-12-01 10:22:13', 3, 1, 0, '2025-11-23 05:09:46', '2025-11-23 08:08:06');
INSERT INTO `class_info` VALUES (22, '计算机科学与技术2021级1班', 'CS2021-1', 26, 2021, NULL, NULL, NULL, NULL, 0, 1, 0, '2025-11-23 05:09:46', '2025-11-23 08:08:06');
INSERT INTO `class_info` VALUES (23, 'ces', 'ccc', 24, 2025, 126, NULL, NULL, NULL, 0, 1, 0, '2025-11-25 07:14:01', '2025-11-25 07:14:01');
INSERT INTO `class_info` VALUES (24, '测试', '123', 24, 2025, NULL, NULL, NULL, NULL, 0, 1, 0, '2025-12-14 04:08:08', '2025-12-14 04:08:08');
INSERT INTO `class_info` VALUES (25, 'cc', 'cc', 24, 2025, NULL, NULL, NULL, NULL, 0, 0, 1, '2025-12-14 09:49:32', '2025-12-14 09:49:34');

-- ----------------------------
-- Table structure for college_info
-- ----------------------------
DROP TABLE IF EXISTS `college_info`;
CREATE TABLE `college_info`  (
  `college_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学院ID',
  `college_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院名称',
  `college_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院代码',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `dean_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '院长姓名（用于显示）',
  `dean_user_id` bigint NULL DEFAULT NULL COMMENT '院长用户ID',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`college_id`) USING BTREE,
  UNIQUE INDEX `uk_college_code_school`(`college_code` ASC, `school_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_college_school` FOREIGN KEY (`school_id`) REFERENCES `school_info` (`school_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学院信息表，用于存储学院的基本信息，包括学院名称、学院代码、所属学校、院长信息等，是学校与专业之间的中间层级，下辖多个专业' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of college_info
-- ----------------------------
INSERT INTO `college_info` VALUES (23, '信息科学技术学院', 'PKU-IST', 9, '张院长', 124, '010-62751234', 1, 0, '2025-11-23 05:09:36', '2025-11-23 05:09:44');
INSERT INTO `college_info` VALUES (24, '计算机科学与技术系', 'THU-CS', 10, '李院长', 125, '010-62785001', 1, 0, '2025-11-23 05:09:36', '2025-11-23 05:09:45');

-- ----------------------------
-- Table structure for comprehensive_score
-- ----------------------------
DROP TABLE IF EXISTS `comprehensive_score`;
CREATE TABLE `comprehensive_score`  (
  `score_id` bigint NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `enterprise_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '企业评价分数',
  `school_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '学校评价分数',
  `self_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '学生自评分数',
  `comprehensive_score` decimal(5, 2) NOT NULL COMMENT '综合成绩（企业40% + 学校40% + 自评20%）',
  `grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级：优秀、良好、中等、及格、不及格',
  `calculate_time` datetime NOT NULL COMMENT '计算时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`score_id`) USING BTREE,
  UNIQUE INDEX `uk_apply_score`(`apply_id` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '综合成绩表，用于存储学生的实习综合成绩，综合企业评价、学校评价和学生自评计算得出，综合成绩计算公式：企业评价40% + 学校评价40% + 学生自评20%' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comprehensive_score
-- ----------------------------
INSERT INTO `comprehensive_score` VALUES (2, 22, 67, 92.00, 95.50, 88.00, 92.60, '优秀', '2025-12-01 06:24:24', 0, '2025-12-01 06:24:24', '2025-12-01 06:24:24');
INSERT INTO `comprehensive_score` VALUES (3, 21, 66, NULL, 95.75, 98.00, 96.65, '优秀', '2025-12-01 07:17:54', 0, '2025-12-01 07:17:54', '2025-12-01 07:17:54');

-- ----------------------------
-- Table structure for enterprise_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_evaluation`;
CREATE TABLE `enterprise_evaluation`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `work_attitude_score` decimal(5, 2) NOT NULL COMMENT '工作态度评分（0-100分）',
  `knowledge_application_score` decimal(5, 2) NOT NULL COMMENT '专业知识应用评分（0-100分）',
  `professional_skill_score` decimal(5, 2) NOT NULL COMMENT '专业技能评分（0-100分）',
  `teamwork_score` decimal(5, 2) NOT NULL COMMENT '团队协作评分（0-100分）',
  `innovation_score` decimal(5, 2) NOT NULL COMMENT '创新意识评分（0-100分）',
  `log_weekly_report_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '日志周报质量评分（0-100分，可自动计算）',
  `log_weekly_report_score_auto` decimal(5, 2) NULL DEFAULT NULL COMMENT '日志周报质量自动计算分数（只读参考）',
  `total_score` decimal(5, 2) NOT NULL COMMENT '总分（5项指标的平均分）',
  `evaluation_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价意见',
  `evaluation_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '评价状态：0-草稿，1-已提交',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `evaluator_id` bigint NULL DEFAULT NULL COMMENT '评价人ID（企业管理员或企业导师）',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  UNIQUE INDEX `uk_apply_enterprise`(`apply_id` ASC, `enterprise_id` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_evaluator_id`(`evaluator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业评价表，用于存储企业对学生的实习评价，包括工作态度、专业知识应用、专业技能等6项指标评分，企业管理员或企业导师可以对学生的实习表现进行综合评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise_evaluation
-- ----------------------------
INSERT INTO `enterprise_evaluation` VALUES (1, 22, 67, 10, 89.00, 99.00, 78.00, 100.00, 87.00, 99.00, 93.50, 92.00, '纯粹是', 1, '2025-12-01 06:05:39', 137, 0, '2025-11-29 20:42:03', '2025-12-01 06:05:38');

-- ----------------------------
-- Table structure for enterprise_info
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_info`;
CREATE TABLE `enterprise_info`  (
  `enterprise_id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（企业管理员账号）',
  `enterprise_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业名称',
  `enterprise_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业代码（系统内部唯一标识，必填）',
  `unified_social_credit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `legal_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '法人代表',
  `registered_capital` decimal(15, 2) NULL DEFAULT NULL COMMENT '注册资本（万元）',
  `industry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属行业',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `enterprise_scale` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业规模',
  `business_scope` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '经营范围',
  `audit_status` tinyint NULL DEFAULT NULL COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`enterprise_id`) USING BTREE,
  UNIQUE INDEX `uk_enterprise_code`(`enterprise_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_unified_social_credit_code`(`unified_social_credit_code` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  CONSTRAINT `fk_enterprise_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业信息表，用于存储企业的详细信息，包括企业名称、统一社会信用代码、法人代表、联系方式等，与企业管理员用户信息通过userId关联，支持企业审核和实习岗位管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise_info
-- ----------------------------
INSERT INTO `enterprise_info` VALUES (10, 135, '腾讯科技', 'TENCENT', '91440300708461136T', '马化腾', 2000000000.00, '互联网', '广东省深圳市南山区科技园', '李经理', '0755-86013388', 'contact@tencent.com', '大型', '社交网络、游戏、金融科技', 1, '审核通过', '2025-11-23 05:10:06', 122, 1, 0, '2025-11-23 05:09:56', '2025-11-23 05:18:11');
INSERT INTO `enterprise_info` VALUES (11, NULL, '测试自主实习', 'SELF_1764335470425', '21434324324', '32423', 324234.00, '324', '测试自主实习', '测试自主实习', '15144567855', '', '', '', 1, NULL, NULL, NULL, 1, 0, '2025-11-28 21:11:10', '2025-12-01 07:58:48');
INSERT INTO `enterprise_info` VALUES (17, 141, '萨法说法', 'ceshi', '91440300708461138T', '案说法', NULL, '计算机软件', '萨芬士大夫士大夫大师傅', '打发打发', '15455677656', 'asfsaf!q!@qq.com', '大型企业', '', 1, NULL, NULL, NULL, 1, 0, '2025-12-01 12:00:35', '2025-12-01 12:00:35');
INSERT INTO `enterprise_info` VALUES (18, 146, '测试阿风飒飒阿萨', '123123ENT001', '123213123213123213', '啊实打实', NULL, '计算机软件', '青蛙撒发顺丰', '暗室逢灯', '16675847756', '1231231@qq.com', '大型企业', '', 1, NULL, NULL, NULL, 1, 0, '2025-12-14 10:28:09', '2025-12-14 10:28:09');

-- ----------------------------
-- Table structure for enterprise_mentor_info
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_mentor_info`;
CREATE TABLE `enterprise_mentor_info`  (
  `mentor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业导师ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `enterprise_id` bigint NULL DEFAULT NULL COMMENT '所属企业ID',
  `mentor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '导师姓名',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`mentor_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  CONSTRAINT `fk_mentor_enterprise` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise_info` (`enterprise_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mentor_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业导师信息表，用于存储企业导师的基本信息，包括导师姓名、职位、部门、联系方式等，与企业用户信息通过userId关联，负责指导和管理实习学生' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise_mentor_info
-- ----------------------------
INSERT INTO `enterprise_mentor_info` VALUES (11, 137, 10, '腾讯导师1', '高级工程师', '技术部', '13800005002', 'mentor001@tencent.com', 1, 0, '2025-11-23 05:10:01', '2025-11-23 05:10:01');

-- ----------------------------
-- Table structure for enterprise_school_cooperation
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_school_cooperation`;
CREATE TABLE `enterprise_school_cooperation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `cooperation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合作类型（如：实习基地、校企合作、人才培养等）',
  `cooperation_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '合作状态：1-进行中，2-已结束',
  `start_time` datetime NULL DEFAULT NULL COMMENT '合作开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '合作结束时间',
  `cooperation_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '合作描述',
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_enterprise_school_type`(`enterprise_id` ASC, `school_id` ASC, `cooperation_type` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_cooperation_status`(`cooperation_status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业学校合作关系表，用于存储企业与学校之间的合作关系信息，包括合作类型、合作状态、合作时间等，支持多种合作类型，如实习基地、校企合作、人才培养等' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise_school_cooperation
-- ----------------------------
INSERT INTO `enterprise_school_cooperation` VALUES (7, 9, 9, '实习基地', 1, '2025-11-23 05:10:07', '2026-11-23 05:10:07', '阿里巴巴与北京大学建立实习基地合作关系', '2025-11-23 05:10:07.103737', '2025-11-23 05:23:10.284722', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (8, 9, 10, '实习基地', 1, '2025-11-23 05:10:07', '2026-11-23 05:10:07', '阿里巴巴与清华大学建立实习基地合作关系', '2025-11-23 05:10:07.103737', '2025-11-23 05:23:10.284722', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (9, 10, 9, '实习基地', 1, '2025-11-23 05:10:07', '2026-11-23 05:10:07', '腾讯与北京大学建立实习基地合作关系', '2025-11-23 05:10:07.103737', '2025-11-23 05:23:10.284722', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (10, 10, 10, '实习基地', 1, '2025-11-23 05:10:07', '2026-11-23 05:10:07', '腾讯与清华大学建立实习基地合作关系', '2025-11-23 05:10:07.103737', '2025-11-23 05:23:10.284722', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (12, 17, 9, '校企合作', 1, '2025-12-01 00:00:00', '2026-04-09 00:00:00', 'ces', '2025-12-01 12:18:48.850000', '2025-12-01 12:18:48.850000', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (14, 10, 9, '人才培养', 1, '2025-12-01 00:00:00', '2025-12-20 00:00:00', '啊事故发生的', '2025-12-01 13:10:18.750000', '2025-12-01 13:10:18.750000', 0);
INSERT INTO `enterprise_school_cooperation` VALUES (15, 18, 9, '实习基地', 1, '2025-12-14 00:00:00', '2026-12-31 00:00:00', '测试合作', '2025-12-14 10:48:18.937000', '2025-12-14 10:48:18.937000', 0);

-- ----------------------------
-- Table structure for enterprise_school_cooperation_apply
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_school_cooperation_apply`;
CREATE TABLE `enterprise_school_cooperation_apply`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `cooperation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合作类型',
  `start_time` datetime NULL DEFAULT NULL COMMENT '合作开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '合作结束时间',
  `cooperation_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '合作描述',
  `apply_status` int NULL DEFAULT 0 COMMENT '申请状态：0-待审核，1-已通过，2-已拒绝',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `delete_flag` int NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_apply_status`(`apply_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业学校合作申请表，用于存储企业向学校申请建立合作关系的申请信息，包括合作类型、合作时间、合作描述等，需要经过学校审核，审核通过后可以建立正式的合作关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enterprise_school_cooperation_apply
-- ----------------------------
INSERT INTO `enterprise_school_cooperation_apply` VALUES (1, 9, 9, '实习基地', '2025-11-23 05:10:07', '2026-11-23 05:10:07', '阿里巴巴与北京大学建立实习基地合作关系', 1, 122, '2025-11-23 05:10:06', '审核通过', 0, '2025-11-23 05:10:06', '2025-11-23 05:10:06');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (2, 9, 10, '实习基地', '2025-11-23 05:10:07', '2026-11-23 05:10:07', '阿里巴巴与清华大学建立实习基地合作关系', 1, 123, '2025-11-23 05:10:06', '审核通过', 0, '2025-11-23 05:10:06', '2025-11-23 05:10:06');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (3, 10, 9, '实习基地', '2025-11-23 05:10:07', '2026-11-23 05:10:07', '腾讯与北京大学建立实习基地合作关系', 1, 122, '2025-11-23 05:10:06', '审核通过', 0, '2025-11-23 05:10:06', '2025-11-23 05:10:06');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (4, 10, 10, '实习基地', '2025-11-23 05:10:07', '2026-11-23 05:10:07', '腾讯与清华大学建立实习基地合作关系', 1, 123, '2025-11-23 05:10:06', '审核通过', 0, '2025-11-23 05:10:06', '2025-11-23 05:10:06');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (8, 17, 9, '校企合作', '2025-12-01 00:00:00', '2026-04-09 00:00:00', 'ces', 1, 122, '2025-12-01 12:18:49', '', 0, '2025-12-01 12:18:31', '2025-12-01 12:18:31');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (9, 10, 9, '科研合作', '2025-12-01 12:55:10', '2026-04-10 02:00:00', 'safas', 1, 122, '2025-12-01 12:55:27', '通过', 0, '2025-12-01 12:55:18', '2025-12-01 12:55:18');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (10, 17, 9, '人才培养', '2025-12-01 00:00:00', '2025-12-04 00:00:00', '算法', 1, 122, '2025-12-01 12:56:35', '', 0, '2025-12-01 12:56:28', '2025-12-01 12:56:28');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (11, 10, 9, '人才培养', '2025-12-01 00:00:00', '2025-12-20 00:00:00', '啊事故发生的', 1, 122, '2025-12-01 13:10:19', '的发挥', 0, '2025-12-01 13:06:31', '2025-12-01 13:06:31');
INSERT INTO `enterprise_school_cooperation_apply` VALUES (12, 18, 9, '实习基地', '2025-12-14 00:00:00', '2026-12-31 00:00:00', '测试合作', 1, 122, '2025-12-14 10:48:19', '', 0, '2025-12-14 10:30:01', '2025-12-14 10:30:01');

-- ----------------------------
-- Table structure for internship_achievement
-- ----------------------------
DROP TABLE IF EXISTS `internship_achievement`;
CREATE TABLE `internship_achievement`  (
  `achievement_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `achievement_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成果名称',
  `achievement_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成果类型（项目文档、工作成果、学习笔记等）',
  `achievement_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '成果描述（富文本）',
  `file_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件URL（多个用逗号分隔）',
  `submit_date` date NOT NULL COMMENT '提交日期',
  `instructor_id` bigint NULL DEFAULT NULL COMMENT '指导教师ID',
  `review_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`achievement_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_achievement_type`(`achievement_type` ASC) USING BTREE,
  INDEX `idx_submit_date`(`submit_date` ASC) USING BTREE,
  INDEX `idx_instructor_id`(`instructor_id` ASC) USING BTREE,
  INDEX `idx_review_status`(`review_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '阶段性成果表，用于存储学生在实习过程中提交的阶段性成果，包括成果名称、类型、描述、文件等，支持多种成果类型，教师可以对成果进行审核和评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_achievement
-- ----------------------------
INSERT INTO `internship_achievement` VALUES (7, 67, 129, 22, '发顺丰案说法案说法', '项目成果', '<p>啊萨法说法是f啊沙发上发送f安抚案说法案说法</p><p><img src=\"data:image/jpeg;base64,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\"></p><p>萨法说法是按规范爱国俺是个</p>', '/uploads/2025/11/28/d5d105be4b0941308e18845a66ed6811.pdf', '2025-11-28', 137, 1, '2025-11-29 16:28:43', '通过', 0, '2025-11-28 21:14:50', '2025-11-28 21:54:27');
INSERT INTO `internship_achievement` VALUES (8, 66, 128, 21, 'wfas撒公司', '项目成果', '<p>啊沙发沙发阿法狗萨格的方式的发射点是否</p>', '/uploads/2025/12/01/b883954a3302411899512ef5d665e1ab.docx,/uploads/2025/12/01/f51440f96b664c43bda8a5b2f901d0a8.pdf', '2025-11-30', 126, 1, '2025-12-01 06:46:42', '俺是个大帅哥是提示', 0, '2025-12-01 06:45:40', '2025-12-01 06:45:40');

-- ----------------------------
-- Table structure for internship_apply
-- ----------------------------
DROP TABLE IF EXISTS `internship_apply`;
CREATE TABLE `internship_apply`  (
  `apply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '关联实习计划ID',
  `apply_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '申请类型：1-合作企业，2-自主实习',
  `enterprise_id` bigint NULL DEFAULT NULL COMMENT '企业ID（合作企业时必填）',
  `post_id` bigint NULL DEFAULT NULL COMMENT '岗位ID（合作企业时可选）',
  `self_enterprise_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习企业名称',
  `self_enterprise_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习企业地址',
  `self_unified_social_credit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业统一社会信用代码',
  `self_industry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习所属行业',
  `self_legal_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习法人代表',
  `self_contact_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习联系人',
  `self_contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习联系电话',
  `self_enterprise_nature` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习企业性质',
  `self_post_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自主实习岗位名称',
  `self_start_date` date NULL DEFAULT NULL COMMENT '自主实习开始日期',
  `self_end_date` date NULL DEFAULT NULL COMMENT '自主实习结束日期',
  `self_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自主实习说明',
  `resume_attachment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简历附件URL（多个用逗号分隔）',
  `apply_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请理由',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已录用，4-已拒绝录用',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID（班主任或学校管理员）',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `enterprise_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '企业反馈',
  `enterprise_feedback_time` datetime NULL DEFAULT NULL COMMENT '企业反馈时间',
  `interview_time` datetime NULL DEFAULT NULL COMMENT '面试时间',
  `interview_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试地点',
  `interview_result` tinyint(1) NULL DEFAULT NULL COMMENT '面试结果：1-通过，2-不通过',
  `interview_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '面试评价',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '录用时间',
  `student_confirm_status` int NULL DEFAULT 0 COMMENT '学生确认状态：0-未确认，1-已确认上岗，2-已拒绝',
  `student_confirm_time` datetime NULL DEFAULT NULL COMMENT '学生确认时间',
  `unbind_status` int NULL DEFAULT 0 COMMENT '解绑状态：0-未申请，1-申请解绑，2-已解绑，3-解绑被拒绝',
  `unbind_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解绑原因',
  `unbind_audit_user_id` bigint NULL DEFAULT NULL COMMENT '解绑审核人ID（班主任）',
  `unbind_audit_time` datetime NULL DEFAULT NULL COMMENT '解绑审核时间',
  `unbind_audit_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解绑审核意见',
  `internship_start_date` date NULL DEFAULT NULL COMMENT '实习开始日期',
  `internship_end_date` date NULL DEFAULT NULL COMMENT '实习结束日期',
  `mentor_id` bigint NULL DEFAULT NULL COMMENT '企业导师ID',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`apply_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_apply_type`(`apply_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实习申请表，用于存储学生实习申请的基本信息，包括申请类型、企业信息、申请状态等，支持合作企业申请和自主实习申请两种类型，需要经过审核流程' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_apply
-- ----------------------------
INSERT INTO `internship_apply` VALUES (21, 66, 128, 6, 2, 11, NULL, '测试自主实习', '测试自主实习', NULL, NULL, NULL, '测试自主实习', '15144567855', '测试自主实习', '测试自主实习', '2025-09-11', '2026-01-05', '', NULL, '测试自主实习', 13, 126, '2025-11-28 21:11:10', '同意', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-28 21:11:10', 1, '2025-11-28 21:13:37', 0, NULL, NULL, NULL, '而无法地方发生发', '2025-11-28', '2025-12-01', NULL, 0, '2025-11-28 21:10:24', '2025-12-01 07:22:07');
INSERT INTO `internship_apply` VALUES (22, 67, 129, 6, 1, 10, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '测试合作实习', 8, 126, '2025-11-28 21:11:06', '同意', '提供', '2025-11-28 21:13:40', NULL, NULL, NULL, NULL, '2025-11-28 21:13:40', 1, '2025-11-28 21:13:47', 2, '实习结束', NULL, NULL, NULL, '2025-09-11', '2025-12-01', 11, 0, '2025-11-28 21:10:50', '2025-12-01 08:04:00');

-- ----------------------------
-- Table structure for internship_feedback
-- ----------------------------
DROP TABLE IF EXISTS `internship_feedback`;
CREATE TABLE `internship_feedback`  (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `feedback_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '反馈类型：1-问题反馈，2-建议反馈，3-求助反馈',
  `feedback_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈标题',
  `feedback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容（富文本）',
  `attachment_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL（多个用逗号分隔）',
  `feedback_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '反馈状态：0-待处理，1-处理中，2-已解决，3-已关闭',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复人ID（教师或导师）',
  `reply_user_type` tinyint(1) NULL DEFAULT NULL COMMENT '回复人类型：1-指导教师，2-企业导师',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复内容（富文本）',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `solve_time` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_feedback_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_feedback_status`(`feedback_status` ASC) USING BTREE,
  INDEX `idx_reply_user_id`(`reply_user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '问题反馈表，用于存储学生在实习过程中提交的问题反馈，包括反馈类型、标题、内容等，支持问题反馈、建议反馈、求助反馈三种类型，教师或企业导师可以回复反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_feedback
-- ----------------------------
INSERT INTO `internship_feedback` VALUES (7, 67, 129, 22, 1, '啊是擦萨法说法啊沙发上', '暗杀三个噶时光碍事俺是个', '/uploads/2025/11/28/0257cd3ce3414467ac49d634da13d9f0.docx,/uploads/2025/11/28/ae0dab164c0e4692bbc49c6505623547.pdf', 2, 137, 1, '收到', '2025-11-29 16:28:23', '2025-11-29 16:28:33', 0, '2025-11-28 21:15:24', '2025-11-28 21:15:24');
INSERT INTO `internship_feedback` VALUES (8, 66, 128, 21, 1, '算法官方规范但', '范德萨发三个德国大使馆大使馆', '/uploads/2025/12/01/dd94a84484784dcd939c816089aa2036.docx,/uploads/2025/12/01/dae4e04bbdff43a6a9355707f10e5911.pdf', 2, 126, 2, '法沙发沙发案说法', '2025-12-01 07:16:53', '2025-12-01 07:16:58', 0, '2025-12-01 06:45:52', '2025-12-01 06:45:52');

-- ----------------------------
-- Table structure for internship_log
-- ----------------------------
DROP TABLE IF EXISTS `internship_log`;
CREATE TABLE `internship_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '关联实习计划ID',
  `log_date` date NOT NULL COMMENT '日志日期',
  `log_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志标题',
  `log_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志内容（富文本）',
  `attachment_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL（多个用逗号分隔）',
  `work_hours` decimal(5, 2) NULL DEFAULT NULL COMMENT '工作时长（小时）',
  `instructor_id` bigint NULL DEFAULT NULL COMMENT '指导教师ID',
  `review_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '批阅状态：0-未批阅，1-已批阅',
  `review_time` datetime NULL DEFAULT NULL COMMENT '批阅时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '批阅意见',
  `review_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '批阅评分（0-100）',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_log_date`(`log_date` ASC) USING BTREE,
  INDEX `idx_instructor_id`(`instructor_id` ASC) USING BTREE,
  INDEX `idx_review_status`(`review_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实习日志表，用于存储学生实习过程中的日志记录，包括日志日期、标题、内容、工作时长等，支持附件上传和教师批阅功能，教师可以对日志进行评分和评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_log
-- ----------------------------
INSERT INTO `internship_log` VALUES (9, 67, 129, 22, NULL, '2025-11-28', '2025-11-28 实习日志', '<p>萨法说法是发送嘎斯碍事给</p><p><img src=\"data:image/jpeg;base64,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\"></p><p>嘀咕嘀咕 安抚</p>', '/uploads/2025/11/28/0431a050c2c1477282af0b3e08be14c9.docx,/uploads/2025/11/28/05ca2b09e1ae47b893dd3f5deba3f56a.pdf', 8.00, 137, 1, '2025-11-29 15:39:57', '规范', 88.00, 0, '2025-11-28 21:14:07', '2025-11-28 23:34:34');
INSERT INTO `internship_log` VALUES (10, 66, 128, 21, NULL, '2025-12-01', '2025-12-01 实习日志', '<p>asfas as4324234wrwe ewr </p>', '/uploads/2025/12/01/7a78161cc9d34dedbc4f1cd0cb3d9f5e.docx,/uploads/2025/12/01/3c5c7591117c478bb169423275475e81.pdf', 8.00, 126, 1, '2025-12-01 06:46:29', '十大高手', 89.00, 0, '2025-12-01 06:45:04', '2025-12-01 06:45:04');

-- ----------------------------
-- Table structure for internship_plan
-- ----------------------------
DROP TABLE IF EXISTS `internship_plan`;
CREATE TABLE `internship_plan`  (
  `plan_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实习计划名称',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实习计划编号（唯一）',
  `semester_id` bigint NULL DEFAULT NULL COMMENT '关联学期ID',
  `school_id` bigint NOT NULL COMMENT '所属学校ID',
  `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID（可选，NULL表示全校）',
  `major_id` bigint NULL DEFAULT NULL COMMENT '所属专业ID（可选，NULL表示全院）',
  `plan_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实习类型（生产实习、毕业实习等）',
  `start_date` date NOT NULL COMMENT '实习开始日期',
  `end_date` date NOT NULL COMMENT '实习结束日期',
  `plan_outline` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实习大纲（富文本）',
  `task_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务要求（富文本）',
  `assessment_standards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '考核标准（富文本）',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已通过，3-已拒绝，4-已发布',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`) USING BTREE,
  UNIQUE INDEX `uk_plan_code`(`plan_code` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_semester_id`(`semester_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_major_id`(`major_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实习计划表，用于存储实习计划的基本信息，包括计划名称、计划编号、实习类型、时间范围等，支持全校、全院、专业三种范围的计划，需要审核后才能发布' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_plan
-- ----------------------------
INSERT INTO `internship_plan` VALUES (5, '2024-2025学年第一学期计算机科学与技术专业实习计划', 'PLAN-2024-2025-1-CS', 9, 9, 23, 24, '专业实习', '2024-09-01', '2025-01-31', '本实习计划旨在培养学生的实践能力和职业素养', '完成企业实习任务，提交实习报告', '根据实习表现和报告质量进行评价', 4, 122, '2025-11-23 07:33:09', '', '2025-11-23 07:33:12', 122, 0, '2025-11-23 05:24:02', '2025-11-23 05:24:02');
INSERT INTO `internship_plan` VALUES (6, '测试计划', '11233', 9, 9, NULL, NULL, '生产实习', '2025-09-11', '2026-01-05', '踩踩踩', '擦擦擦', '擦擦撒的', 4, 122, '2025-11-27 09:30:08', '', '2025-11-27 09:30:10', 122, 0, '2025-11-27 09:30:03', '2025-11-27 10:21:12');
INSERT INTO `internship_plan` VALUES (7, '纯粹是', '134', 9, 9, 23, 24, '生产实习', '2025-09-19', '2026-01-10', '撒打算', '萨达', '根深蒂固', 4, NULL, NULL, NULL, '2025-11-27 10:21:35', 122, 0, '2025-11-27 09:31:22', '2025-11-27 10:27:33');

-- ----------------------------
-- Table structure for internship_post
-- ----------------------------
DROP TABLE IF EXISTS `internship_post`;
CREATE TABLE `internship_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `post_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编号（企业内唯一）',
  `post_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '岗位描述（富文本）',
  `skill_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能要求（富文本）',
  `work_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作地点',
  `work_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `recruit_count` int NOT NULL DEFAULT 1 COMMENT '招聘人数',
  `applied_count` int NOT NULL DEFAULT 0 COMMENT '已申请人数',
  `accepted_count` int NOT NULL DEFAULT 0 COMMENT '已录用人数',
  `salary_min` decimal(10, 2) NULL DEFAULT NULL COMMENT '最低薪资（元/月）',
  `salary_max` decimal(10, 2) NULL DEFAULT NULL COMMENT '最高薪资（元/月）',
  `salary_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '薪资类型（月薪、日薪、时薪、面议）',
  `internship_start_date` date NULL DEFAULT NULL COMMENT '实习开始日期',
  `internship_end_date` date NULL DEFAULT NULL COMMENT '实习结束日期',
  `work_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作时间（如：周一至周五 9:00-18:00）',
  `contact_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已发布，4-已关闭',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`) USING BTREE,
  UNIQUE INDEX `uk_post_code`(`enterprise_id` ASC, `post_code` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实习岗位表，用于存储企业发布的实习岗位信息，包括岗位名称、岗位描述、技能要求、薪资范围等，支持岗位审核和发布流程，学生可以申请岗位并参加面试' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_post
-- ----------------------------
INSERT INTO `internship_post` VALUES (9, 10, 'Java开发实习生', 'TENCENT-JAVA-001', '负责Java后端开发工作，参与核心业务系统开发', '熟悉Java、Spring框架，了解MySQL数据库', '深圳', '深圳市南山区科技园', 5, 1, 1, 5000.00, 8000.00, '月薪', '2025-02-01', '2025-07-31', '周一至周五 9:00-18:00', '张经理', '0755-86013388', 'hr@tencent.com', 3, NULL, NULL, NULL, '2025-11-25 05:08:29', NULL, 0, '2025-11-25 05:08:29', '2025-11-25 05:08:29');
INSERT INTO `internship_post` VALUES (10, 10, '前端开发实习生', 'TENCENT-FRONTEND-001', '负责前端页面开发，参与产品功能实现', '熟悉Vue.js、React等前端框架，了解HTML/CSS/JavaScript', '深圳', '深圳市南山区科技园', 5, 2, 2, 5000.00, 8000.00, '月薪', '2025-02-01', '2025-07-31', '周一至周五 9:00-18:00', '张经理', '0755-86013388', 'hr@tencent.com', 3, NULL, NULL, NULL, '2025-11-25 05:08:32', NULL, 0, '2025-11-25 05:08:32', '2025-11-25 05:08:32');
INSERT INTO `internship_post` VALUES (11, 10, 'Python开发实习生', 'TENCENT-PYTHON-002', '负责Python后端开发工作，参与项目开发和维护，学习新技术。', '1. 熟悉Python基础语法\n2. 了解Django或Flask框架\n3. 熟悉数据库操作\n4. 有良好的学习能力', '深圳', '深圳市南山区科技园', 2, 0, 0, 6000.00, 9000.00, '月薪', NULL, NULL, '周一至周五 9:00-18:00', '李经理', '13800138000', 'hr@tencent.com', 3, 1, '2025-11-27 19:09:45', NULL, '2025-12-01 08:58:54', NULL, 0, '2025-11-27 18:52:40', '2025-11-27 18:53:34');
INSERT INTO `internship_post` VALUES (12, 10, '12312', '213123', '213123', '12312', '3123', '123', 121, 0, 0, 2323.00, 23123.00, '月薪', '2025-12-09', '2025-12-26', '3213', '1231', '12312', '3123', 3, 122, '2025-12-14 10:47:13', NULL, '2025-12-14 10:47:53', NULL, 0, '2025-12-01 08:58:44', '2025-12-01 08:58:44');
INSERT INTO `internship_post` VALUES (13, 18, '测试岗位', '001', '啊沙发上', '撒发生', '案说法', '啊沙发上', 1, 0, 0, 123.00, 2323.00, '月薪', '2025-12-14', '2026-01-29', '', '', '', '', 3, 122, '2025-12-14 11:01:01', NULL, '2025-12-14 11:01:01', NULL, 0, '2025-12-14 10:28:37', '2025-12-14 10:28:37');

-- ----------------------------
-- Table structure for internship_weekly_report
-- ----------------------------
DROP TABLE IF EXISTS `internship_weekly_report`;
CREATE TABLE `internship_weekly_report`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `week_number` int NOT NULL COMMENT '周次（第几周）',
  `week_start_date` date NULL DEFAULT NULL COMMENT '周开始日期（该周报对应的周的开始日期，周一）',
  `week_end_date` date NULL DEFAULT NULL COMMENT '周结束日期（该周报对应的周的结束日期，周日）',
  `report_date` date NOT NULL COMMENT '周报日期',
  `report_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '周报标题',
  `work_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工作内容（富文本）',
  `attachment_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL（多个用逗号分隔）',
  `instructor_id` bigint NULL DEFAULT NULL COMMENT '指导教师ID',
  `review_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '批阅状态：0-未批阅，1-已批阅',
  `review_time` datetime NULL DEFAULT NULL COMMENT '批阅时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '批阅意见',
  `review_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '批阅评分（0-100）',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_week_number`(`week_number` ASC) USING BTREE,
  INDEX `idx_report_date`(`report_date` ASC) USING BTREE,
  INDEX `idx_instructor_id`(`instructor_id` ASC) USING BTREE,
  INDEX `idx_review_status`(`review_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '周报表，用于存储学生实习过程中的周报记录，包括周次、工作内容、附件等，支持教师批阅功能，教师可以对周报进行评分和评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of internship_weekly_report
-- ----------------------------
INSERT INTO `internship_weekly_report` VALUES (7, 67, 129, 22, 1, NULL, NULL, '2025-11-28', '第1周实习周报', '<p>啊沙发沙发啊碍事发</p><p><img src=\"data:image/jpeg;base64,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\"></p><p>的故事大纲阿斯顿飞过案说法 安抚</p>', '/uploads/2025/11/28/61ccc96f5c3a461e9b32c212dfbc4b6b.docx,/uploads/2025/11/28/8685cbe8b51b497da5887e829b6c34fd.pdf', 137, 1, '2025-11-29 15:39:50', '是否', 99.00, 0, '2025-11-28 21:14:28', '2025-11-28 21:14:28');
INSERT INTO `internship_weekly_report` VALUES (13, 66, 128, 21, 2, '2025-12-08', '2025-12-14', '2025-12-08', '第2周实习周报', '<p>发生发萨法说法阿发</p>', NULL, 126, 1, '2025-12-01 07:15:53', '啊沙发沙发 案说法', 98.00, 0, '2025-12-01 07:15:14', '2025-12-01 07:15:14');

-- ----------------------------
-- Table structure for interview
-- ----------------------------
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview`  (
  `interview_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `interview_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '面试类型：1-现场面试，2-视频面试，3-电话面试',
  `interview_time` datetime NOT NULL COMMENT '面试时间',
  `interview_location` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试地点（现场面试时必填）',
  `interview_link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试链接（视频面试时必填）',
  `interview_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试电话（电话面试时必填）',
  `interviewer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试官（多个用逗号分隔）',
  `interview_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '面试内容/要求',
  `student_confirm` tinyint(1) NOT NULL DEFAULT 0 COMMENT '学生确认：0-未确认，1-已确认，2-已拒绝',
  `student_confirm_time` datetime NULL DEFAULT NULL COMMENT '学生确认时间',
  `interview_result` tinyint(1) NULL DEFAULT NULL COMMENT '面试结果：1-通过，2-不通过，3-待定',
  `interview_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '面试评价',
  `interview_feedback_time` datetime NULL DEFAULT NULL COMMENT '面试反馈时间',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-已完成，3-已取消',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`interview_id`) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_enterprise_id`(`enterprise_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_interview_time`(`interview_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '面试表，用于存储学生申请实习岗位后的面试信息，包括面试类型、时间、地点、结果等，支持现场面试、视频面试、电话面试三种类型，学生需要确认面试安排' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interview
-- ----------------------------
INSERT INTO `interview` VALUES (11, 22, 10, 67, 1, '2025-11-28 21:12:13', '出场费', NULL, NULL, '案说法', '撒发生', 1, '2025-11-28 21:12:37', 1, '通过', '2025-11-28 21:12:59', 2, 0, '2025-11-28 21:12:26', '2025-11-28 21:12:26');

-- ----------------------------
-- Table structure for major_info
-- ----------------------------
DROP TABLE IF EXISTS `major_info`;
CREATE TABLE `major_info`  (
  `major_id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `major_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业名称',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业代码',
  `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID',
  `duration` int NULL DEFAULT NULL COMMENT '学制年限',
  `training_objective` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '培养目标',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`major_id`) USING BTREE,
  UNIQUE INDEX `uk_major_code_college`(`major_code` ASC, `college_id` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `college_info` (`college_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业信息表，用于存储专业的基本信息，包括专业名称、专业代码、所属学院、学制年限等，是学院与班级之间的中间层级，下辖多个班级' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of major_info
-- ----------------------------
INSERT INTO `major_info` VALUES (24, '计算机科学与技术', 'CS', 23, 4, NULL, 1, 0, '2025-11-23 05:09:38', '2025-11-23 05:09:38');
INSERT INTO `major_info` VALUES (25, '软件工程', 'SE', 23, 4, NULL, 1, 0, '2025-11-23 05:09:38', '2025-11-23 05:09:38');
INSERT INTO `major_info` VALUES (26, '计算机科学与技术', 'CS', 24, 4, NULL, 1, 0, '2025-11-23 05:09:38', '2025-11-23 05:09:38');

-- ----------------------------
-- Table structure for permission_info
-- ----------------------------
DROP TABLE IF EXISTS `permission_info`;
CREATE TABLE `permission_info`  (
  `permission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限代码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限名称',
  `permission_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父权限ID',
  `permission_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`permission_id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限信息表，用于存储系统中的权限信息，包括权限代码、权限名称、权限类型等，支持菜单、按钮、接口三种权限类型，通过角色权限关联表实现权限分配' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission_info
-- ----------------------------
INSERT INTO `permission_info` VALUES (1, 'system:school:view', '查看学校', 'api', 0, '查看学校列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (2, 'system:school:add', '添加学校', 'api', 0, '添加新学校', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (3, 'system:school:edit', '编辑学校', 'api', 0, '编辑学校信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (4, 'system:school:delete', '删除学校', 'api', 0, '停用学校', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (5, 'system:college:view', '查看学院', 'api', 0, '查看学院列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (6, 'system:college:add', '添加学院', 'api', 0, '添加新学院', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (7, 'system:college:edit', '编辑学院', 'api', 0, '编辑学院信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (8, 'system:college:delete', '删除学院', 'api', 0, '停用学院', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (9, 'system:major:view', '查看专业', 'api', 0, '查看专业列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (10, 'system:major:add', '添加专业', 'api', 0, '添加新专业', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (11, 'system:major:edit', '编辑专业', 'api', 0, '编辑专业信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (12, 'system:major:delete', '删除专业', 'api', 0, '停用专业', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (13, 'system:class:view', '查看班级', 'api', 0, '查看班级列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (14, 'system:class:add', '添加班级', 'api', 0, '添加新班级', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (15, 'system:class:edit', '编辑班级', 'api', 0, '编辑班级信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (16, 'system:class:delete', '删除班级', 'api', 0, '停用班级', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (17, 'system:class:sharecode', '管理分享码', 'api', 0, '生成和管理班级分享码', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (18, 'system:semester:view', '查看学期', 'api', 0, '查看学期列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (19, 'system:semester:add', '添加学期', 'api', 0, '添加新学期', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (20, 'system:semester:edit', '编辑学期', 'api', 0, '编辑学期信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (21, 'system:semester:delete', '删除学期', 'api', 0, '删除学期', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (22, 'system:config:view', '查看配置', 'api', 0, '查看系统配置', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (23, 'system:config:add', '添加配置', 'api', 0, '添加系统配置', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (24, 'system:config:edit', '编辑配置', 'api', 0, '编辑系统配置', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (25, 'system:config:delete', '删除配置', 'api', 0, '删除系统配置', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (26, 'user:view', '查看用户', 'api', 0, '查看用户列表和详情', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (27, 'user:add', '添加用户', 'api', 0, '添加新用户', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (28, 'user:edit', '编辑用户', 'api', 0, '编辑用户信息', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (29, 'user:delete', '删除用户', 'api', 0, '停用用户', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');
INSERT INTO `permission_info` VALUES (30, 'user:resetPassword', '重置密码', 'api', 0, '重置用户密码', 1, 0, '2025-11-20 18:28:25', '2025-11-20 18:28:25');

-- ----------------------------
-- Table structure for role_info
-- ----------------------------
DROP TABLE IF EXISTS `role_info`;
CREATE TABLE `role_info`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色代码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色名称',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色信息表，用于存储系统中的角色信息，包括角色代码、角色名称、角色描述等，系统支持多种角色，通过角色权限关联表实现权限管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_info
-- ----------------------------
INSERT INTO `role_info` VALUES (1, 'ROLE_SYSTEM_ADMIN', '系统管理员', '系统管理员，拥有最高权限，可管理所有数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (2, 'ROLE_SCHOOL_ADMIN', '学校管理员', '学校管理员，可管理本校的所有数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (3, 'ROLE_COLLEGE_LEADER', '学院负责人', '学院负责人，可管理本院的所有数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (4, 'ROLE_CLASS_TEACHER', '班主任', '班主任，可管理本班的学生数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (5, 'ROLE_INSTRUCTOR', '指导教师', '指导教师，可管理分配的学生数据', 0, 1, '2025-11-20 18:28:11', '2025-11-25 06:35:54');
INSERT INTO `role_info` VALUES (6, 'ROLE_ENTERPRISE_ADMIN', '企业管理员', '企业管理员，可管理本企业的数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (7, 'ROLE_ENTERPRISE_MENTOR', '企业导师', '企业导师，可管理本企业的实习生数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');
INSERT INTO `role_info` VALUES (8, 'ROLE_STUDENT', '学生', '学生，只能查看和管理个人数据', 1, 0, '2025-11-20 18:28:11', '2025-11-20 18:28:11');

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键，自增）',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID锛堝閿紝鍏宠仈role_info琛級',
  `permission_id` bigint NOT NULL COMMENT '鏉冮檺ID锛堝閿紝鍏宠仈permission_info琛級',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission_info` (`permission_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role_info` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表，用于存储角色与权限的关联关系，实现角色与权限的多对多关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 1, 1, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (2, 1, 2, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (3, 1, 3, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (4, 1, 4, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (5, 1, 5, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (6, 1, 6, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (7, 1, 7, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (8, 1, 8, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (9, 1, 9, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (10, 1, 10, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (11, 1, 11, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (12, 1, 12, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (13, 1, 13, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (14, 1, 14, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (15, 1, 15, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (16, 1, 16, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (17, 1, 17, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (18, 1, 18, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (19, 1, 19, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (20, 1, 20, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (21, 1, 21, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (22, 1, 22, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (23, 1, 23, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (24, 1, 24, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (25, 1, 25, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (26, 1, 26, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (27, 1, 27, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (28, 1, 28, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (29, 1, 29, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');
INSERT INTO `role_permission` VALUES (30, 1, 30, 0, '2025-11-20 18:28:54.000000', '2025-11-20 18:28:54.000000');

-- ----------------------------
-- Table structure for school_admin_info
-- ----------------------------
DROP TABLE IF EXISTS `school_admin_info`;
CREATE TABLE `school_admin_info`  (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  CONSTRAINT `fk_school_admin_school` FOREIGN KEY (`school_id`) REFERENCES `school_info` (`school_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_school_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校管理员信息表，用于存储学校管理员的基本信息，包括所属学校、用户信息等，与学校用户信息通过userId关联，负责管理学校相关的业务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_admin_info
-- ----------------------------
INSERT INTO `school_admin_info` VALUES (9, 122, 9, 1, 0, '2025-11-23 05:09:35', '2025-11-23 05:09:35');

-- ----------------------------
-- Table structure for school_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `school_evaluation`;
CREATE TABLE `school_evaluation`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `log_weekly_report_score` decimal(5, 2) NOT NULL COMMENT '日志周报质量评分（0-100分）',
  `process_performance_score` decimal(5, 2) NOT NULL COMMENT '过程表现评分（0-100分）',
  `achievement_score` decimal(5, 2) NOT NULL COMMENT '成果展示评分（0-100分）',
  `summary_reflection_score` decimal(5, 2) NOT NULL COMMENT '总结反思评分（0-100分）',
  `total_score` decimal(5, 2) NOT NULL COMMENT '总分（4项指标的平均分）',
  `evaluation_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价意见',
  `evaluation_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '评价状态：0-草稿，1-已提交',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `evaluator_id` bigint NULL DEFAULT NULL COMMENT '评价人ID（教师）',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  UNIQUE INDEX `uk_apply_school`(`apply_id` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_evaluator_id`(`evaluator_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校评价表，用于存储学校教师对学生的实习评价，包括日志周报质量、过程表现、成果展示等4项指标评分，教师可以对学生的实习过程进行综合评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_evaluation
-- ----------------------------
INSERT INTO `school_evaluation` VALUES (2, 22, 67, 98.00, 88.00, 98.00, 98.00, 95.50, '发生发生', 1, '2025-12-01 06:24:24', 126, 0, '2025-12-01 06:24:23', '2025-12-01 06:24:23');
INSERT INTO `school_evaluation` VALUES (3, 21, 66, 98.00, 99.00, 88.00, 98.00, 95.75, '的手法首发啊', 1, '2025-12-01 07:17:54', 126, 0, '2025-12-01 07:17:54', '2025-12-01 07:17:54');

-- ----------------------------
-- Table structure for school_info
-- ----------------------------
DROP TABLE IF EXISTS `school_info`;
CREATE TABLE `school_info`  (
  `school_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  `school_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校名称',
  `school_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校代码',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`school_id`) USING BTREE,
  UNIQUE INDEX `uk_school_code`(`school_code` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校信息表，用于存储学校的基本信息，包括学校名称、学校代码、地址、联系方式等，是系统组织架构的顶层单位，下辖学院、专业、班级等' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_info
-- ----------------------------
INSERT INTO `school_info` VALUES (9, '北京大学', 'PKU', '北京市海淀区颐和园路5号', '张主任', '010-62751234', 1, 0, '2025-11-23 05:09:29', '2025-11-23 05:09:29');
INSERT INTO `school_info` VALUES (10, '清华大学', 'THU', '北京市海淀区清华园1号', '李主任', '010-62785001', 1, 0, '2025-11-23 05:09:29', '2025-11-23 05:09:29');
INSERT INTO `school_info` VALUES (11, '测试学校', 'TEST', '测试地址', '测试联系人', '13800138000', 1, 0, '2025-11-27 12:39:09', '2025-11-27 19:30:41');

-- ----------------------------
-- Table structure for self_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `self_evaluation`;
CREATE TABLE `self_evaluation`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `apply_id` bigint NOT NULL COMMENT '申请ID（关联实习申请）',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `self_score` decimal(5, 2) NOT NULL COMMENT '自评分数（0-100分）',
  `reflection_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自我反思和总结（富文本）',
  `evaluation_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '评价状态：0-草稿，1-已提交',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  UNIQUE INDEX `uk_apply_self`(`apply_id` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生自评表，用于存储学生对自己实习过程的自评信息，包括自评分数、自我反思和总结等，学生可以在实习结束后提交自评，作为综合成绩计算的组成部分' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of self_evaluation
-- ----------------------------
INSERT INTO `self_evaluation` VALUES (1, 22, 67, 88.00, '<p>萨法说法是as噶杀杀杀啊水水碍事f</p>', 1, '2025-12-01 06:05:49', 0, '2025-12-01 05:18:56', '2025-12-01 06:05:49');
INSERT INTO `self_evaluation` VALUES (2, 21, 66, 98.00, '<p>发生飞洒飞洒发萨芬萨芬案发时</p>', 1, '2025-12-01 07:17:35', 0, '2025-12-01 07:17:35', '2025-12-01 07:17:35');

-- ----------------------------
-- Table structure for semester_info
-- ----------------------------
DROP TABLE IF EXISTS `semester_info`;
CREATE TABLE `semester_info`  (
  `semester_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学期ID',
  `semester_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学期名称',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `is_current` tinyint NULL DEFAULT NULL COMMENT '是否当前学期：1-是，0-否',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`semester_id`) USING BTREE,
  UNIQUE INDEX `uk_semester_name`(`semester_name` ASC) USING BTREE,
  INDEX `idx_is_current`(`is_current` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学期信息表，用于存储学期的基本信息，包括学期名称、开始日期、结束日期等，支持设置当前学期，用于实习计划的时间范围管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of semester_info
-- ----------------------------
INSERT INTO `semester_info` VALUES (9, '2024-2025学年第一学期', '2024-09-01', '2025-01-31', 9, 1, 0, '2025-11-23 05:10:26', '2025-11-27 09:18:32');

-- ----------------------------
-- Table structure for student_info
-- ----------------------------
DROP TABLE IF EXISTS `student_info`;
CREATE TABLE `student_info`  (
  `student_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `class_id` bigint NULL DEFAULT NULL COMMENT '所属班级ID',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `graduation_year` int NULL DEFAULT NULL COMMENT '毕业年份',
  `major_id` bigint NULL DEFAULT NULL COMMENT '所属专业ID（冗余字段）',
  `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID（冗余字段）',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID（冗余字段）',
  `current_apply_id` bigint NULL DEFAULT NULL COMMENT '当前实习申请ID',
  `current_enterprise_id` bigint NULL DEFAULT NULL COMMENT '当前实习企业ID',
  `internship_status` tinyint NULL DEFAULT NULL COMMENT '实习状态：0-未实习，1-实习中，3-已结束',
  `audit_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `audit_time` datetime(6) NULL DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`student_id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_major_id`(`major_id` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`class_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息表，用于存储学生的详细信息，包括学号、所属班级、入学年份、实习状态等，与学生用户信息通过userId关联，支持学生实习申请和实习过程管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_info
-- ----------------------------
INSERT INTO `student_info` VALUES (66, 128, '2021001001', 20, 2021, NULL, 24, 23, 9, 24, 12, 1, 1, NULL, NULL, NULL, '2025-11-23 05:09:52', '2025-12-01 05:40:06');
INSERT INTO `student_info` VALUES (67, 129, '2021001002', 20, 2021, NULL, 24, 23, 9, 22, 10, 3, 1, NULL, NULL, NULL, '2025-11-23 05:09:52', '2025-12-01 05:40:07');
INSERT INTO `student_info` VALUES (72, 138, '123456', 21, 2021, NULL, 25, 23, 9, NULL, NULL, 0, 0, NULL, NULL, NULL, '2025-12-01 10:32:37', '2025-12-14 03:47:10');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置类型',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_type`(`config_type` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表，用于存储系统的配置信息，包括配置键、配置值、配置类型等，支持动态配置系统参数，如实习类型、系统设置等' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (7, 'share_code_expire_days', '30', 'share_code', '分享码有效期（天）', 0, '2025-11-20 17:57:34', '2025-11-20 17:57:34');
INSERT INTO `system_config` VALUES (8, 'share_code_length', '8', 'share_code', '分享码长度', 0, '2025-11-20 17:57:34', '2025-11-20 17:57:34');
INSERT INTO `system_config` VALUES (9, 'system_notice_template_welcome', '欢迎使用高校实习管理系统！', 'notice_template', '欢迎通知模板', 1, '2025-11-20 17:57:34', '2025-11-29 16:27:24');
INSERT INTO `system_config` VALUES (11, 'enterprise_evaluation_weight', '0.4', 'evaluation', '企业评价权重', 0, '2025-11-28 20:08:39', '2025-11-28 20:14:53');
INSERT INTO `system_config` VALUES (12, 'school_evaluation_weight', '0.4', 'evaluation', '学校评价权重', 0, '2025-11-28 20:08:40', '2025-12-01 07:38:46');
INSERT INTO `system_config` VALUES (13, 'student_self_evaluation_weight', '0.2', 'evaluation', '学生自评权重', 0, '2025-11-28 20:08:41', '2025-12-01 07:38:50');
INSERT INTO `system_config` VALUES (26, 'self_internship_school_weight', '0.8', 'evaluation', '自主实习学校评价权重', 0, '2025-12-01 07:37:07', '2025-12-01 07:37:07');
INSERT INTO `system_config` VALUES (27, 'self_internship_self_weight', '0.2', 'evaluation', '自主实习学生自评权重', 0, '2025-12-01 07:37:08', '2025-12-01 07:37:08');

-- ----------------------------
-- Table structure for teacher_info
-- ----------------------------
DROP TABLE IF EXISTS `teacher_info`;
CREATE TABLE `teacher_info`  (
  `teacher_id` bigint NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `teacher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工号',
  `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID（冗余字段）',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属学院',
  `audit_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `audit_opinion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `audit_time` datetime(6) NULL DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`teacher_id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_no`(`teacher_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  CONSTRAINT `fk_teacher_college` FOREIGN KEY (`college_id`) REFERENCES `college_info` (`college_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息表，用于存储教师的基本信息，包括工号、所属学院、职称、部门等，与教师用户信息通过userId关联，支持教师管理学生实习过程' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_info
-- ----------------------------
INSERT INTO `teacher_info` VALUES (21, 124, 'T001', 23, 9, '教授', '信息科学技术学院', 1, NULL, NULL, NULL, '2025-11-23 05:09:42', '2025-11-23 05:09:42');
INSERT INTO `teacher_info` VALUES (23, 126, 'T003', 23, 9, '副教授', '信息科学技术学院', 1, NULL, NULL, NULL, '2025-11-23 05:09:42', '2025-11-23 05:09:42');
INSERT INTO `teacher_info` VALUES (27, 145, 'T009', 23, 9, NULL, NULL, 1, '', '2025-12-14 10:00:56.909000', 124, '2025-12-14 09:50:26', '2025-12-14 09:50:26');

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '个人介绍',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态：1-启用，0-禁用',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 147 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户基础信息表，用于存储系统中所有用户的基础信息，包括用户名、密码、真实姓名、联系方式等，支持多种角色，通过关联表实现用户与角色的多对多关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES (1, 'admin', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '系统管理员', NULL, '13800000001', NULL, NULL, 'admin@example.com', '男', NULL, NULL, 1, 0, '2025-11-20 18:33:58', '2025-12-01 10:01:10');
INSERT INTO `user_info` VALUES (122, 'pku_admin', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '北京大学管理员', '', '13800001001', NULL, NULL, 'pku_admin@pku.edu.cn', '男', NULL, NULL, 1, 0, '2025-11-23 05:09:31', '2025-12-14 03:32:11');
INSERT INTO `user_info` VALUES (124, 'pku_dean', '$2a$10$NdQcc9H/BdnJrWbBKSJnn.MP8Uh12pF2mRK85DfQA4m4EH6EKYdqG', '张院长', '', '13800002002', 'ccccccccc', 'sagasg asfasfga ', 'dean_ist@pku.edu.cn', '男', '', '/uploads/2025/11/25/da297383bb4e4ea4a21a1265ac9b87f1.jpg', 1, 0, '2025-11-23 05:09:40', '2025-12-01 10:01:08');
INSERT INTO `user_info` VALUES (126, 'pku_teacher1', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '刘老师', NULL, '13800003001', NULL, NULL, 'teacher001@pku.edu.cn', '男', NULL, NULL, 1, 0, '2025-11-23 05:09:40', '2025-12-01 10:01:08');
INSERT INTO `user_info` VALUES (128, '2021001001', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '张三', NULL, '13800004001', NULL, NULL, '2021001001@pku.edu.cn', '男', NULL, NULL, 1, 0, '2025-11-23 05:09:48', '2025-12-01 09:58:05');
INSERT INTO `user_info` VALUES (129, '2021001002', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '李四', NULL, '13800004002', NULL, NULL, '2021001002@pku.edu.cn', '女', NULL, NULL, 1, 0, '2025-11-23 05:09:48', '2025-12-01 09:58:05');
INSERT INTO `user_info` VALUES (135, 'tencent_admin', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '腾讯管理员', NULL, '0755-86013388', NULL, NULL, 'admin@tencent.com', '男', NULL, NULL, 1, 0, '2025-11-23 05:09:53', '2025-12-01 10:01:13');
INSERT INTO `user_info` VALUES (137, 'tencent_mentor1', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '腾讯导师1', NULL, '13800005002', NULL, NULL, 'mentor001@tencent.com', '男', NULL, NULL, 1, 0, '2025-11-23 05:09:58', '2025-12-01 10:01:12');
INSERT INTO `user_info` VALUES (138, '123456', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', 'sfasfa', '', '', NULL, NULL, '', '男', NULL, NULL, 1, 1, '2025-12-01 10:32:37', '2025-12-14 03:44:56');
INSERT INTO `user_info` VALUES (139, 'testadmin', '$2a$10$fw/hSrgw2r9pxRXlrPWbYejOzax6ZgfAiVJj62NZ/RUOoJG8TIzGC', '打发打发', '', '15455677656', NULL, NULL, 'asfsaf!q!@qq.com', '男', NULL, NULL, 1, 1, '2025-12-01 12:00:35', '2025-12-14 03:50:20');
INSERT INTO `user_info` VALUES (140, 'T005', '$2a$10$vpJ2dAudbvbUHU1TLjPcG.kK0DhISdo4812HN6DV6nGBuW2HYrU9u', '测试', '', '', NULL, NULL, '', NULL, NULL, NULL, 0, 0, '2025-12-13 21:40:17', '2025-12-14 03:46:59');
INSERT INTO `user_info` VALUES (141, '123123', '$2a$10$x4q8lPouxi2yU9IGMVIQO.wS8ZYQOnn7d5gDQAKXjE/hUONTSc0he', '123123', '123', '', NULL, NULL, '', '男', NULL, NULL, 1, 0, '2025-12-14 03:49:58', '2025-12-14 03:49:58');
INSERT INTO `user_info` VALUES (142, 'student03', '$2a$10$tPJt3aJuzNMfAkS.dMF5Z.X8/wkl0RXhHSr1Ew8RaUreEvmSS8MHK', '123', '', '', NULL, NULL, '', NULL, NULL, NULL, 0, 0, '2025-12-14 03:54:57', '2025-12-14 03:54:57');
INSERT INTO `user_info` VALUES (143, 'student04', '$2a$10$aeIKloPJ2jDx67IrxQGcMeXDH/kHdSnXGo95TXj1gqd3I8MZoI.Sy', '123123', '', '', NULL, NULL, '', '男', NULL, NULL, 0, 0, '2025-12-14 03:56:18', '2025-12-14 09:59:20');
INSERT INTO `user_info` VALUES (144, 'test006', '$2a$10$XsGDfjxytaPEP1qfJCUWb.pNwPhrZAvA917f.7u/8fTSEq7rTI9uO', '测试', NULL, NULL, NULL, NULL, NULL, '男', NULL, NULL, 1, 0, '2025-12-14 04:29:22', '2025-12-14 04:29:22');
INSERT INTO `user_info` VALUES (145, 'teacher04', '$2a$10$/Y5E9OS96LvWBPbAPZGfSuMtIdMNOfFf5Rt2HFXMXqzQm0cBaCVlW', 'ces', NULL, NULL, NULL, NULL, NULL, '男', NULL, NULL, 1, 0, '2025-12-14 09:50:26', '2025-12-14 09:50:26');
INSERT INTO `user_info` VALUES (146, 'qiye01', '$2a$10$Fzomb5OY41HXts3qxH/iee.Zo8cjBwnFEUDaOJ6BkFG90.H3/QkQG', '暗室逢灯', NULL, '16675847756', NULL, NULL, '1231231@qq.com', NULL, NULL, NULL, 1, 0, '2025-12-14 10:28:10', '2025-12-14 10:28:10');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `delete_flag` tinyint NULL DEFAULT NULL COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role_info` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 167 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表，用于存储用户与角色的关联关系，实现用户与角色的多对多关系，一个用户可以有多个角色，一个角色可以分配给多个用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1, 0, '2025-11-20 18:36:16', '2025-11-20 18:36:16');
INSERT INTO `user_role` VALUES (140, 122, 2, 0, '2025-11-23 05:09:33', '2025-11-23 05:09:33');
INSERT INTO `user_role` VALUES (142, 124, 3, 0, '2025-11-23 05:09:41', '2025-11-23 05:09:41');
INSERT INTO `user_role` VALUES (144, 126, 4, 0, '2025-11-23 05:09:41', '2025-11-23 05:09:41');
INSERT INTO `user_role` VALUES (145, 126, 5, 1, '2025-11-23 05:09:41', '2025-11-25 06:36:01');
INSERT INTO `user_role` VALUES (148, 128, 8, 0, '2025-11-23 05:09:49', '2025-11-23 05:09:49');
INSERT INTO `user_role` VALUES (149, 129, 8, 0, '2025-11-23 05:09:49', '2025-11-23 05:09:49');
INSERT INTO `user_role` VALUES (155, 135, 6, 0, '2025-11-23 05:09:55', '2025-11-23 05:09:55');
INSERT INTO `user_role` VALUES (157, 137, 7, 0, '2025-11-23 05:10:00', '2025-11-23 05:10:00');
INSERT INTO `user_role` VALUES (158, 138, 8, 0, '2025-12-01 10:32:56', '2025-12-01 10:32:56');
INSERT INTO `user_role` VALUES (159, 139, 6, 0, '2025-12-01 12:00:35', '2025-12-01 12:00:35');
INSERT INTO `user_role` VALUES (160, 140, 4, 0, '2025-12-13 21:40:17', '2025-12-13 21:40:17');
INSERT INTO `user_role` VALUES (161, 141, 6, 0, '2025-12-14 03:49:58', '2025-12-14 03:49:58');
INSERT INTO `user_role` VALUES (162, 143, 8, 0, '2025-12-14 03:57:28', '2025-12-14 03:57:28');
INSERT INTO `user_role` VALUES (163, 144, 4, 0, '2025-12-14 04:57:36', '2025-12-14 04:57:36');
INSERT INTO `user_role` VALUES (164, 142, 8, 0, '2025-12-14 10:00:16', '2025-12-14 10:00:16');
INSERT INTO `user_role` VALUES (165, 145, 4, 0, '2025-12-14 10:00:57', '2025-12-14 10:00:57');
INSERT INTO `user_role` VALUES (166, 146, 6, 0, '2025-12-14 10:28:10', '2025-12-14 10:28:10');

SET FOREIGN_KEY_CHECKS = 1;
