package top.strelitzia.model;

/**
 * @author wangzy
 * @Date 2020/12/14 15:08
 * 材料获取途径信息
 **/
public class SourcePlace {
    private String zoneName;
    private String code;
    private Integer apCost;
    private Double rate;

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Integer getApCost() {
        return apCost;
    }

    public void setApCost(Integer apCost) {
        this.apCost = apCost;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
