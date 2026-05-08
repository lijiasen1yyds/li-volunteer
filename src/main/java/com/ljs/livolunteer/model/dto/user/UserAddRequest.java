package com.ljs.livolunteer.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员创建用户请求
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

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
     * 用户角色: volunteer-志愿者 organizer-组织者 admin-管理员
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
