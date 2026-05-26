package cn.zhongjiating.canteen.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OperatingHoursUtilTest {

    @Test
    void testIsOperating() {
        // Empty/null should default to true (always open)
        assertThat(OperatingHoursUtil.isOperating(null)).isTrue();
        assertThat(OperatingHoursUtil.isOperating("")).isTrue();
        assertThat(OperatingHoursUtil.isOperating("  ")).isTrue();

        // Valid ranges
        String json = "[{\"startTime\":\"08:00\",\"endTime\":\"10:00\"},{\"startTime\":\"11:30\",\"endTime\":\"14:00\"}]";
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(9, 0))).isTrue();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(8, 0))).isTrue();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(10, 0))).isTrue();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(10, 30))).isFalse();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(12, 0))).isTrue();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(14, 0))).isTrue();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(14, 1))).isFalse();
        assertThat(OperatingHoursUtil.isOperating(json, LocalTime.of(7, 59))).isFalse();

        // Empty array defaults to true
        assertThat(OperatingHoursUtil.isOperating("[]")).isTrue();

        // Invalid JSON defaults to true
        assertThat(OperatingHoursUtil.isOperating("invalid-json")).isTrue();
    }
}
