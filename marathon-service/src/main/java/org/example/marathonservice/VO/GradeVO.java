package org.example.marathonservice.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeVO {
    //成绩id
    Long id;

    //赛事id
    Long raceId;

    //赛事名称
    String raceName;

    //赛事类型
    Integer raceType;

    //赛事时间
    LocalDateTime raceDate;

    //参赛号
    Integer athleteNumber;

    //姓名
    String name;

    //成绩
    String grade;

    //排名
    Integer rank;

    //是否评论过 0:未评论 已评论则存评论id
    Long isComment;
}
