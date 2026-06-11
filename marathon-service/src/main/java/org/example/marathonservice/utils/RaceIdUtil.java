package org.example.marathonservice.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RaceIdUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成 Race ID，格式为地名拼音前两个字母大写拼接日期（yyyyMMdd）
     * @param location 地名
     * @param raceDate 比赛日期
     * @return 生成的 Race ID
     */
    public static String generateRaceId(String location, LocalDateTime raceDate) {
        String placePrefix = getPinyinPrefix(location);
        String dateStr = formatDate(LocalDate.from(raceDate));
        return placePrefix + dateStr;
    }

    /**
     * 获取地名拼音的前两个字母并转换为大写
     * @param location 地名
     * @return 地名拼音的前两个字母大写
     */
    private static String getPinyinPrefix(String location) {
        StringBuilder pinyin = new StringBuilder();
        for (char c : location.toCharArray()) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyinArray != null && pinyinArray.length > 0) {
                pinyin.append(pinyinArray[0].charAt(0));
            }
        }
        return pinyin.toString().substring(0, 2).toUpperCase();
    }

    /**
     * 将日期格式化为 yyyyMMdd 格式
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
