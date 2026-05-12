package com.ljs.livolunteer.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.DeleteRequest;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.model.dto.user.*;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.LoginUserVO;
import com.ljs.livolunteer.model.vo.UserStatsVO;
import com.ljs.livolunteer.model.vo.UserVO;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.ljs.livolunteer.constant.UserConstant.ADMIN_ROLE;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        boolean result = userService.userLogout();
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 管理员用户管理

    /**
     * 管理员创建用户
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.hasBlank(userAddRequest.getUserAccount()),
                ErrorCode.PARAMS_ERROR, "账号不能为空");

        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);

        // 设置默认密码（如果未指定）
        String password = StrUtil.isBlank(userAddRequest.getUserPassword())
                ? "12345678" : userAddRequest.getUserPassword();
        user.setUserPassword(userService.encryptPassword(password));

        // 设置默认角色（如果未指定）
        if (StrUtil.isBlank(user.getUserRole())) {
            user.setUserRole("volunteer");
        }

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建用户失败");
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员删除用户
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除用户失败");
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新用户
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新用户失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取用户（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(
                new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest)
        );
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户列表（封装类）
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");
        Page<User> userPage = userService.page(
                new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest)
        );
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        userVOPage.setRecords(userService.getUserVOList(userPage.getRecords()));
        return ResultUtils.success(userVOPage);
    }

    // endregion

    // region 用户个人操作

    /**
     * 用户编辑个人信息
     */
    @AuthCheck
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUser(@RequestBody UserEditRequest userEditRequest) {
        ThrowUtils.throwIf(userEditRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        User user = new User();
        BeanUtil.copyProperties(userEditRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新个人信息失败");
        return ResultUtils.success(true);
    }

    /**
     * 个人工作台聚合统计
     */
    @AuthCheck
    @GetMapping("/my/stats")
    public BaseResponse<UserStatsVO> getMyStats() {
        return ResultUtils.success(userService.getMyStats());
    }

    // endregion
}
