package org.example.marathonservice.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


@Data
public class AthleteExcel {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("参赛号")
    private Integer raceNumber;

    @ExcelProperty("成绩")
    private String score;
}
