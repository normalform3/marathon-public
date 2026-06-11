package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.marathondal.entity.RouteDO;

@Mapper
public interface RouteMapper extends BaseMapper<RouteDO> {
}
