package com.gov.payment.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DateTimeUtil {

    // 한국 시간대
    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    // 다양한 포맷터들
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter KOREAN_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");

    /**
     * 현재 한국 시간 반환
     */
    public static LocalDateTime nowInKorea() {
        return LocalDateTime.now(KOREA_ZONE);
    }

    /**
     * 현재 한국 날짜 반환
     */
    public static LocalDate todayInKorea() {
        return LocalDate.now(KOREA_ZONE);
    }

    /**
     * 디스플레이용 포맷 (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        try {
            return dateTime.format(DISPLAY_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 포맷 실패: {}", dateTime, e);
            return dateTime.toString();
        }
    }

    /**
     * 날짜만 디스플레이용 포맷 (yyyy-MM-dd)
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 포맷 실패: {}", date, e);
            return date.toString();
        }
    }

    /**
     * 시간만 디스플레이용 포맷 (HH:mm:ss)
     */
    public static String formatTimeForDisplay(LocalTime time) {
        if (time == null) {
            return "";
        }
        try {
            return time.format(TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("시간 포맷 실패: {}", time, e);
            return time.toString();
        }
    }

    /**
     * 파일명용 포맷 (yyyyMMdd_HHmmss)
     */
    public static String formatForFileName(LocalDateTime dateTime) {
        if (dateTime == null) {
            return formatForFileName(nowInKorea());
        }
        try {
            return dateTime.format(FILE_FORMATTER);
        } catch (Exception e) {
            log.warn("파일명 날짜 포맷 실패: {}", dateTime, e);
            return dateTime.format(COMPACT_FORMATTER);
        }
    }

    /**
     * 컴팩트 포맷 (yyyyMMddHHmmss)
     */
    public static String formatCompact(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        try {
            return dateTime.format(COMPACT_FORMATTER);
        } catch (Exception e) {
            log.warn("컴팩트 날짜 포맷 실패: {}", dateTime, e);
            return dateTime.toString().replaceAll("[^0-9]", "");
        }
    }

    /**
     * 한국어 포맷 (yyyy년 MM월 dd일 HH시 mm분)
     */
    public static String formatKorean(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        try {
            return dateTime.format(KOREAN_FORMATTER);
        } catch (Exception e) {
            log.warn("한국어 날짜 포맷 실패: {}", dateTime, e);
            return formatForDisplay(dateTime);
        }
    }

    /**
     * 문자열을 LocalDateTime으로 파싱
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 여러 포맷 시도
            DateTimeFormatter[] formatters = {
                DISPLAY_FORMATTER,
                ISO_FORMATTER,
                COMPACT_FORMATTER,
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(dateTimeStr.trim(), formatter);
                } catch (DateTimeParseException ignored) {
                    // 다음 포맷터 시도
                }
            }

            log.warn("날짜 파싱 실패: {}", dateTimeStr);
            return null;
        } catch (Exception e) {
            log.error("날짜 파싱 중 오류: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * 문자열을 LocalDate로 파싱
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter[] formatters = {
                DATE_FORMATTER,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyyMMdd"),
                DateTimeFormatter.ISO_LOCAL_DATE
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr.trim(), formatter);
                } catch (DateTimeParseException ignored) {
                    // 다음 포맷터 시도
                }
            }

            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        } catch (Exception e) {
            log.error("날짜 파싱 중 오류: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 하루의 시작 시간 (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 하루의 마지막 시간 (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(LocalTime.MAX);
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 월의 시작일
     */
    public static LocalDateTime startOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
    }

    /**
     * 월의 마지막일
     */
    public static LocalDateTime endOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * 년의 시작일
     */
    public static LocalDateTime startOfYear(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();
    }

    /**
     * 년의 마지막일
     */
    public static LocalDateTime endOfYear(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.lastDayOfYear()).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * 두 날짜 간의 일수 계산
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    /**
     * 두 날짜 간의 시간 계산 (시간 단위)
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 두 날짜 간의 시간 계산 (분 단위)
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 날짜가 오늘인지 확인
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(todayInKorea());
    }

    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(todayInKorea());
    }

    /**
     * 날짜가 과거인지 확인
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(nowInKorea());
    }

    /**
     * 날짜가 미래인지 확인
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(nowInKorea());
    }

    /**
     * 날짜가 특정 범위 내에 있는지 확인
     */
    public static boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        if (target == null || start == null || end == null) {
            return false;
        }
        return !target.isBefore(start) && !target.isAfter(end);
    }

    /**
     * 영업일 여부 확인 (주말 제외)
     */
    public static boolean isBusinessDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 다음 영업일 반환
     */
    public static LocalDate nextBusinessDay(LocalDate date) {
        if (date == null) {
            date = todayInKorea();
        }

        LocalDate nextDay = date.plusDays(1);
        while (!isBusinessDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * 상대적 시간 표현 (예: "2시간 전", "3일 후")
     */
    public static String getRelativeTime(LocalDateTime target) {
        if (target == null) {
            return "";
        }

        LocalDateTime now = nowInKorea();
        Duration duration = Duration.between(target, now);

        long seconds = Math.abs(duration.getSeconds());
        boolean isPast = duration.getSeconds() >= 0;
        String suffix = isPast ? "전" : "후";

        if (seconds < 60) {
            return "방금 " + suffix;
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + "분 " + suffix;
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + "시간 " + suffix;
        } else if (seconds < 2592000) { // 30일
            long days = seconds / 86400;
            return days + "일 " + suffix;
        } else if (seconds < 31536000) { // 1년
            long months = seconds / 2592000;
            return months + "개월 " + suffix;
        } else {
            long years = seconds / 31536000;
            return years + "년 " + suffix;
        }
    }

    /**
     * Unix 타임스탬프를 LocalDateTime으로 변환
     */
    public static LocalDateTime fromUnixTimestamp(long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), KOREA_ZONE);
        } catch (Exception e) {
            log.error("Unix 타임스탬프 변환 실패: {}", timestamp, e);
            return null;
        }
    }

    /**
     * LocalDateTime을 Unix 타임스탬프로 변환
     */
    public static long toUnixTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        try {
            return dateTime.atZone(KOREA_ZONE).toEpochSecond();
        } catch (Exception e) {
            log.error("Unix 타임스탬프 변환 실패: {}", dateTime, e);
            return 0;
        }
    }

    /**
     * 시간대 변환
     */
    public static LocalDateTime convertTimeZone(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
        if (dateTime == null || fromZone == null || toZone == null) {
            return dateTime;
        }

        try {
            ZonedDateTime zonedDateTime = dateTime.atZone(fromZone);
            return zonedDateTime.withZoneSameInstant(toZone).toLocalDateTime();
        } catch (Exception e) {
            log.error("시간대 변환 실패: dateTime={}, from={}, to={}", dateTime, fromZone, toZone, e);
            return dateTime;
        }
    }

    /**
     * UTC를 한국 시간으로 변환
     */
    public static LocalDateTime utcToKorea(LocalDateTime utcDateTime) {
        return convertTimeZone(utcDateTime, ZoneOffset.UTC, KOREA_ZONE);
    }

    /**
     * 한국 시간을 UTC로 변환
     */
    public static LocalDateTime koreaToUtc(LocalDateTime koreaDateTime) {
        return convertTimeZone(koreaDateTime, KOREA_ZONE, ZoneOffset.UTC);
    }

    /**
     * 날짜 범위 유효성 검증
     */
    public static boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        return !start.isAfter(end);
    }

    /**
     * 최대 날짜 범위 검증 (예: 최대 1년)
     */
    public static boolean isWithinMaxRange(LocalDateTime start, LocalDateTime end, int maxDays) {
        if (!isValidDateRange(start, end)) {
            return false;
        }
        return daysBetween(start, end) <= maxDays;
    }
}
