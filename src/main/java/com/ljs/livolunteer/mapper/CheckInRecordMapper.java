package com.ljs.livolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录 Mapper
 */
@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {
}
