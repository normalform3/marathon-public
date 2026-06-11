package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.marathondal.entity.NewsDO;

import java.util.List;

@Mapper
public interface NewsMapper extends BaseMapper<NewsDO> {
}
