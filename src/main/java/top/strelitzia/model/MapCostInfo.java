package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class MapCostInfo {
    private String zoneName;
    private String code;
    private Integer apCost;

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getApCost() {
        return apCost;
    }

    public void setApCost(Integer apCost) {
        this.apCost = apCost;
    }
}
