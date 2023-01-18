package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 管理员权限信息
 **/
public class AdminUserInfo {
    //管理员qq
    private String qq;
    //管理员昵称，用于肉眼识别
    private String name;
    //无限抽卡权限
    private Integer found;
    //爆率拉满权限
    private Integer six;


    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFound() {
        return found;
    }

    public void setFound(Integer found) {
        this.found = found;
    }

    public Integer getSix() {
        return six;
    }

    public void setSix(Integer six) {
        this.six = six;
    }
}
