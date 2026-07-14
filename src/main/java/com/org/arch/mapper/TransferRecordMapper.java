package com.org.arch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.org.arch.entity.TransferRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调动记录 Mapper 接口
 */
@Mapper
public interface TransferRecordMapper extends BaseMapper<TransferRecord> {
}