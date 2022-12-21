package top.strelitzia.model;


/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * @Date 2021/4/20 17:41
 **/
public class NickName {
    private String nickName;
    private String name;

    private Integer version;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
