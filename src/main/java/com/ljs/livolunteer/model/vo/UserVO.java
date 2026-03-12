package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserVO implements Serializable {

    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private String college;

    private String major;

    private String className;

    private BigDecimal totalVolunteerHours;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
