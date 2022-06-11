package top.strelitzia.model;

public class EquipBuff {
    private String buffName;
    private Double value;
    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getBuffName() {
        return buffName;
    }

    public void setBuffName(String buffName) {
        this.buffName = buffName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
