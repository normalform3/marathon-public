package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.marathondal.entity.CommentDO;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
}
