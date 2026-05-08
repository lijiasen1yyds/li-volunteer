package com.ljs.livolunteer.aop;

import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.UserRoleEnum;
import com.ljs.livolunteer.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 权限校验 AOP 切面
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前登录用户（未登录会抛出 NotLoginException）
        User loginUser = userService.getLoginUser();

        String mustRole = authCheck.mustRole();
        // 仅需登录，无角色要求
        if (mustRole == null || mustRole.isEmpty()) {
            return joinPoint.proceed();
        }

        // 获取要求的角色枚举
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        // 获取当前用户的角色枚举
        String userRoleValue = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRoleValue);
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        // 管理员拥有所有权限
        if (UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            return joinPoint.proceed();
        }

        // 要求管理员角色但当前用户不是管理员
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        // 要求组织者角色，当前用户必须是组织者
        if (UserRoleEnum.ORGANIZER.equals(mustRoleEnum)
                && !UserRoleEnum.ORGANIZER.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        // volunteer 角色要求 → 任何已登录用户均可通过
        return joinPoint.proceed();
    }
}
