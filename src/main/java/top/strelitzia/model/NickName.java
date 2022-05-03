package top.strelitzia.model;

import java.io.Serializable;

/**
 * @author Strelizia
 * @Description
 * @ProjectName arknights
 * @Package com.strelizia.arknights.model
 * @Date 2021/4/20 17:41
 **/
public class NickName implements Serializable {
    private String nickName;
    private String name;

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
}
