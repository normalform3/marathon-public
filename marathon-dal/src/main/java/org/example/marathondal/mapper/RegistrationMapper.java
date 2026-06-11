package org.example.marathondal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.marathondal.entity.RegistrationDO;

import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<RegistrationDO> {
    //随机选取一定数量的中签者id
    @Select("SELECT id FROM registration WHERE race_id = #{raceId} AND status = 1 ORDER BY RAND() LIMIT #{maxParticipants}")
    List<Long> selectRandomWinners(@Param("raceId") Integer raceId, @Param("maxParticipants") Integer maxParticipants);

}
