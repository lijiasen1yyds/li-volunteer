package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.user.UserQueryRequest;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.LoginUserVO;
import com.ljs.livolunteer.model.vo.UserStatsVO;
import com.ljs.livolunteer.model.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 用户登出
     */
    boolean userLogout();

    /**
     * 获取当前登录用户
     */
    User getLoginUser();

    /**
     * 获取登录用户视图对象
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取用户公开视图对象
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户公开视图对象列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 加密密码
     */
    String encryptPassword(String userPassword);

    /**
     * 获取当前登录用户的个人工作台统计
     */
    UserStatsVO getMyStats();
}
