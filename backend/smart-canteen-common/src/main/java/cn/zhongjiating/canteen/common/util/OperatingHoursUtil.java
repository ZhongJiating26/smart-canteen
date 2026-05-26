package cn.zhongjiating.canteen.common.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatingHoursUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Pattern matches "startTime":"HH:mm" and "endTime":"HH:mm" in either order
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "\"startTime\"\\s*:\\s*\"([0-9]{2}:[0-9]{2})\"\\s*,\\s*\"endTime\"\\s*:\\s*\"([0-9]{2}:[0-9]{2})\""
    );
    private static final Pattern RANGE_PATTERN_ALT = Pattern.compile(
            "\"endTime\"\\s*:\\s*\"([0-9]{2}:[0-9]{2})\"\\s*,\\s*\"startTime\"\\s*:\\s*\"([0-9]{2}:[0-9]{2})\""
    );

    public static boolean isOperating(String operatingHoursJson) {
        return isOperating(operatingHoursJson, LocalTime.now());
    }

    public static boolean isOperating(String operatingHoursJson, LocalTime targetTime) {
        if (operatingHoursJson == null || operatingHoursJson.isBlank()) {
            return true;
        }
        try {
            boolean matched = false;
            
            Matcher matcher = RANGE_PATTERN.matcher(operatingHoursJson);
            while (matcher.find()) {
                matched = true;
                LocalTime start = LocalTime.parse(matcher.group(1), TIME_FORMATTER);
                LocalTime end = LocalTime.parse(matcher.group(2), TIME_FORMATTER);
                if (!targetTime.isBefore(start) && !targetTime.isAfter(end)) {
                    return true;
                }
            }

            Matcher matcherAlt = RANGE_PATTERN_ALT.matcher(operatingHoursJson);
            while (matcherAlt.find()) {
                matched = true;
                LocalTime end = LocalTime.parse(matcherAlt.group(1), TIME_FORMATTER);
                LocalTime start = LocalTime.parse(matcherAlt.group(2), TIME_FORMATTER);
                if (!targetTime.isBefore(start) && !targetTime.isAfter(end)) {
                    return true;
                }
            }

            return !matched;
        } catch (Exception e) {
            return true;
        }
    }
}
