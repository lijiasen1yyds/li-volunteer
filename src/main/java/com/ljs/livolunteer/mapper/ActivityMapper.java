package com.ljs.livolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ljs.livolunteer.model.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动数据库操作
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {


}
