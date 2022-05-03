package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class BiliCount {
    private Long uid;
    private String name;
    private Long top;
    private Long first;

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

    public Long getTop() {
        return top;
    }

    public void setTop(Long top) {
        this.top = top;
    }

    public Long getFirst() {
        return first;
    }

    public void setFirst(Long first) {
        this.first = first;
    }
}
