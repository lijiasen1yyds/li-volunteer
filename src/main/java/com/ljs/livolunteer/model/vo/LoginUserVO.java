package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LoginUserVO implements Serializable {

    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userPhone;

    private String userEmail;

    private String studentId;

    private String college;

    private String major;

    private String className;

    private String userProfile;

    private String userRole;

    private Integer userStatus;

    private BigDecimal totalVolunteerHours;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
