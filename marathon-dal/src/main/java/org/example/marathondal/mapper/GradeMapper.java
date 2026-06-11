package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.marathondal.entity.GradeDO;

@Mapper
public interface GradeMapper extends BaseMapper<GradeDO> {
    // 更新成绩的评论情况
    @Update("update grade set is_comment = #{commentId} where id = #{gradeId}")
    void updateIsComment(Long gradeId, Long commentId);
}
