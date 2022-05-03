package top.strelitzia.model;

/**
 * @author wangzy
 * @Date 2021/1/9 16:40
 **/
public class MapJson {
    private String stageId;
    private String zoneId;
    private String code;
    private Integer apCost;

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
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
