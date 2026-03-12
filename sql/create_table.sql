-- ================================================================
-- 校园志愿者服务系统 数据库建表脚本
-- 数据库: li_volunteer
-- 技术栈: SpringBoot + MyBatis-Plus + MySQL
-- ================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS li_volunteer
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE li_volunteer;

-- ================================================================
-- 1. 用户表 (user)
-- 说明: 统一管理志愿者、组织者、管理员三类角色
-- ================================================================
CREATE TABLE IF NOT EXISTS user
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    userAccount         VARCHAR(64)                        NOT NULL COMMENT '账号',
    userPassword        VARCHAR(128)                       NOT NULL COMMENT '密码(加密存储)',
    userName            VARCHAR(64)                        NULL COMMENT '用户昵称',
    userAvatar          VARCHAR(512)                       NULL COMMENT '用户头像URL',
    userPhone           VARCHAR(20)                        NULL COMMENT '手机号',
    userEmail           VARCHAR(128)                       NULL COMMENT '邮箱',
    studentId           VARCHAR(32)                        NULL COMMENT '学号',
    college             VARCHAR(128)                       NULL COMMENT '学院',
    major               VARCHAR(128)                       NULL COMMENT '专业',
    className           VARCHAR(64)                        NULL COMMENT '班级',
    userProfile         VARCHAR(512)                       NULL COMMENT '用户简介',
    userRole            VARCHAR(32) DEFAULT 'volunteer'    NOT NULL COMMENT '用户角色: volunteer-志愿者 organizer-组织者 admin-管理员',
    userStatus          INT         DEFAULT 0              NOT NULL COMMENT '用户状态: 0-正常 1-禁用',
    totalVolunteerHours DECIMAL(10, 2) DEFAULT 0.00        NOT NULL COMMENT '累计志愿时长(小时)',
    createTime          DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime          DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete            TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userRole (userRole),
    INDEX idx_studentId (studentId)
) COMMENT '用户表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 2. 活动表 (activity)
-- 说明: 存储志愿活动信息, 由组织者创建, 管理员审核发布
-- ================================================================
CREATE TABLE IF NOT EXISTS activity
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title                VARCHAR(256)                       NOT NULL COMMENT '活动标题',
    description          TEXT                               NULL COMMENT '活动描述',
    coverImage           VARCHAR(512)                       NULL COMMENT '活动封面图片URL',
    category             VARCHAR(64)                        NULL COMMENT '活动类别: community-社区帮扶 competition-赛事保障 environment-环保宣传 education-教育支持 other-其他',
    location             VARCHAR(256)                       NULL COMMENT '活动地点',
    startTime            DATETIME                           NOT NULL COMMENT '活动开始时间',
    endTime              DATETIME                           NOT NULL COMMENT '活动结束时间',
    registrationDeadline DATETIME                           NULL COMMENT '报名截止时间',
    maxParticipants      INT         DEFAULT 0              NOT NULL COMMENT '最大参与人数(0表示不限)',
    currentParticipants  INT         DEFAULT 0              NOT NULL COMMENT '当前报名通过人数',
    volunteerHours       DECIMAL(10, 2) DEFAULT 0.00        NOT NULL COMMENT '可获得志愿时长(小时)',
    organizerId          BIGINT                             NOT NULL COMMENT '组织者ID(关联user表)',
    status               INT         DEFAULT 0              NOT NULL COMMENT '活动状态: 0-待审核 1-已发布 2-审核拒绝 3-进行中 4-已完成 5-已取消',
    reviewMessage        VARCHAR(512)                       NULL COMMENT '审核意见',
    reviewerId           BIGINT                             NULL COMMENT '审核人ID(关联user表)',
    reviewTime           DATETIME                           NULL COMMENT '审核时间',
    createTime           DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime           DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete             TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    INDEX idx_organizerId (organizerId),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_startTime (startTime)
) COMMENT '活动表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 3. 活动报名表 (activity_registration)
-- 说明: 志愿者报名活动, 管理员审核报名申请
-- ================================================================
CREATE TABLE IF NOT EXISTS activity_registration
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    activityId    BIGINT                             NOT NULL COMMENT '活动ID(关联activity表)',
    userId        BIGINT                             NOT NULL COMMENT '报名用户ID(关联user表)',
    status        INT         DEFAULT 0              NOT NULL COMMENT '报名状态: 0-待审核 1-审核通过 2-审核拒绝',
    reviewMessage VARCHAR(512)                       NULL COMMENT '审核意见',
    reviewerId    BIGINT                             NULL COMMENT '审核人ID(关联user表)',
    reviewTime    DATETIME                           NULL COMMENT '审核时间',
    createTime    DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime    DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete      TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_activity_user (activityId, userId),
    INDEX idx_activityId (activityId),
    INDEX idx_userId (userId),
    INDEX idx_status (status)
) COMMENT '活动报名表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 4. 签到记录表 (check_in_record)
-- 说明: 记录志愿者的签到签退, 自动计算实际服务时长
-- ================================================================
CREATE TABLE IF NOT EXISTS check_in_record
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    activityId     BIGINT                             NOT NULL COMMENT '活动ID(关联activity表)',
    userId         BIGINT                             NOT NULL COMMENT '用户ID(关联user表)',
    registrationId BIGINT                             NOT NULL COMMENT '报名记录ID(关联activity_registration表)',
    checkInTime    DATETIME                           NULL COMMENT '签到时间',
    checkOutTime   DATETIME                           NULL COMMENT '签退时间',
    actualHours    DECIMAL(10, 2) DEFAULT 0.00        NULL COMMENT '实际服务时长(小时, 根据签到签退自动计算)',
    status         INT         DEFAULT 0              NOT NULL COMMENT '签到状态: 0-已签到 1-已签退',
    createTime     DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime     DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete       TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_activity_user (activityId, userId),
    INDEX idx_activityId (activityId),
    INDEX idx_userId (userId),
    INDEX idx_registrationId (registrationId)
) COMMENT '签到记录表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 5. 志愿时长认证表 (volunteer_hours)
-- 说明: 管理员对志愿者的服务时长进行认证
-- ================================================================
CREATE TABLE IF NOT EXISTS volunteer_hours
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    userId      BIGINT                             NOT NULL COMMENT '用户ID(关联user表)',
    activityId  BIGINT                             NOT NULL COMMENT '活动ID(关联activity表)',
    hours       DECIMAL(10, 2) DEFAULT 0.00        NOT NULL COMMENT '认证时长(小时)',
    status      INT         DEFAULT 0              NOT NULL COMMENT '认证状态: 0-待认证 1-已认证 2-认证拒绝',
    certifierId BIGINT                             NULL COMMENT '认证人ID(关联user表)',
    certifyTime DATETIME                           NULL COMMENT '认证时间',
    remark      VARCHAR(512)                       NULL COMMENT '备注',
    createTime  DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime  DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete    TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_user_activity (userId, activityId),
    INDEX idx_userId (userId),
    INDEX idx_activityId (activityId),
    INDEX idx_status (status)
) COMMENT '志愿时长认证表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 6. 活动评价表 (activity_evaluation)
-- 说明: 志愿者对参与的活动进行评分和评价
-- ================================================================
CREATE TABLE IF NOT EXISTS activity_evaluation
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    activityId BIGINT                             NOT NULL COMMENT '活动ID(关联activity表)',
    userId     BIGINT                             NOT NULL COMMENT '评价用户ID(关联user表)',
    rating     INT                                NOT NULL COMMENT '评分(1-5星)',
    content    TEXT                               NULL COMMENT '评价内容',
    status     INT         DEFAULT 0              NOT NULL COMMENT '评价状态: 0-正常 1-隐藏',
    createTime DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT     DEFAULT 0              NOT NULL COMMENT '是否删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_activity_user (activityId, userId),
    INDEX idx_activityId (activityId),
    INDEX idx_userId (userId)
) COMMENT '活动评价表' ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ================================================================
-- 初始数据: 插入默认管理员账号
-- 密码为加密后的 "12345678"（实际项目中应使用加密工具生成）
-- ================================================================
INSERT INTO user (userAccount, userPassword, userName, userRole)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin');
