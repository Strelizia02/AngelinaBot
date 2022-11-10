package top.strelitzia.model;

public class GroupWelcomeInfo {
    private Long groupId;
    private String welcomeMessage;
    private Integer pictureNum;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
    public Integer getPictureNum() {return pictureNum;}

    public void setPictureNum(Integer pictureNum) {this.pictureNum = pictureNum;}
}
