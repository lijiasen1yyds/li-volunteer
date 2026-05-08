package com.ljs.livolunteer.model.dto.user;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 学院
     */
    private String college;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 用户状态
     */
    private Integer userStatus;

    private static final long serialVersionUID = 1L;
}
