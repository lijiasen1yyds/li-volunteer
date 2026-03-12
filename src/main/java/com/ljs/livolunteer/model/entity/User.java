package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户实体类
 *
 * @TableName user
 */
@Data
@TableName("user")
public class User implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码(加密存储)
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像URL
     */
    private String userAvatar;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String major;

    /**
     * 班级
     */
    private String className;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: volunteer-志愿者 organizer-组织者 admin-管理员
     */
    private String userRole;

    /**
     * 用户状态: 0-正常 1-禁用
     */
    private Integer userStatus;

    /**
     * 累计志愿时长(小时)
     */
    private BigDecimal totalVolunteerHours;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
