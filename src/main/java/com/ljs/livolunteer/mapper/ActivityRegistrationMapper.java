package com.ljs.livolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动报名数据库操作
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

}
