package top.strelitzia.model;

/**
 * @author wangzy
 * @Date 2021/1/9 20:01
 **/
public class MapMatrixInfo {
    private String materialName;
    private Double rate;
    private Integer quantity;
    private Integer times;

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }
}
