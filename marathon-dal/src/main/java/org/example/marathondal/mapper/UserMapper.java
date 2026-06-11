package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.marathondal.entity.UserDO;
import org.springframework.stereotype.Service;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
