package cn.zhongjiating.canteen.common.enums;

import java.time.LocalTime;

public enum MealPeriod {

    BREAKFAST("早餐", LocalTime.of(6, 0), LocalTime.of(9, 0)),
    LUNCH("午餐", LocalTime.of(11, 0), LocalTime.of(14, 0)),
    DINNER("晚餐", LocalTime.of(17, 0), LocalTime.of(20, 0)),
    SNACK("夜宵", LocalTime.of(20, 0), LocalTime.of(23, 0));

    private final String description;

    private final LocalTime startTime;

    private final LocalTime endTime;

    MealPeriod(String description, LocalTime startTime, LocalTime endTime) {
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
