package com.ljs.livolunteer.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员更新用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}
