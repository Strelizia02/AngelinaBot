package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class BiliCount {
    private Long uid;
    private String name;
    private String top;
    private Integer topTime;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public Integer getTopTime() {
        return topTime;
    }

    public void setTopTime(Integer topTime) {
        this.topTime = topTime;
    }
}
