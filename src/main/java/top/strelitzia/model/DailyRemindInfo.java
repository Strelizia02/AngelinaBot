package top.strelitzia.model;

public class DailyRemindInfo {
    private Long groupId;
    private String remindContent;
    private Integer dayLeft;
    private Long userId;
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getRemindContent() {
        return remindContent;
    }

    public void setRemindContent(String remindContent) {
        this.remindContent = remindContent;
    }

    public Integer getDayLeft() {
        return dayLeft;
    }

    public void setDayLeft(Integer dayLeft) {
        this.dayLeft = dayLeft;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
