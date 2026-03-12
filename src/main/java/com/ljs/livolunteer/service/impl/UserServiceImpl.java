package com.ljs.livolunteer.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.UserMapper;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.LoginUserVO;
import com.ljs.livolunteer.model.vo.UserVO;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "ljs_volunteer";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号长度不能少于4位");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2. 检查账号是否已存在
        synchronized (userAccount.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.count(queryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");

            // 3. 加密密码
            String encryptPassword = DigestUtil.md5Hex(SALT + userPassword);

            // 4. 插入新用户
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName("用户" + RandomUtil.randomNumbers(6));
            boolean saved = this.save(user);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号长度不能少于4位");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");

        // 2. 加密密码并查询用户
        String encryptPassword = DigestUtil.md5Hex(SALT + userPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        // 3. 检查用户状态
        ThrowUtils.throwIf(user.getUserStatus() != 0, ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");

        // 4. SA-Token 登录
        StpUtil.login(user.getId());
        log.info("用户登录成功, userId = {}", user.getId());

        return this.getLoginUserVO(user);
    }

    @Override
    public boolean userLogout() {
        StpUtil.checkLogin();
        StpUtil.logout();
        return true;
    }

    @Override
    public User getLoginUser() {
        Object userIdObj = StpUtil.getLoginId();
        long userId = Long.parseLong(userIdObj.toString());
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        ThrowUtils.throwIf(user.getUserStatus() != 0, ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        return user;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }
}
