package top.strelitzia.model;

public class DailyRemindCount {
    private Long groupId;
    private String remindContent;

    public Long getGroupId(){return this.groupId;}
    public void setGroupId(Long groupId){this.groupId=groupId;}
    public String getRemindContent(){return this.remindContent;}
    public void setRemindContent(String remindContent){this.remindContent=remindContent;}
}
