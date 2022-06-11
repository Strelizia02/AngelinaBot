package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 材料以及数量信息
 **/
public class MaterialInfo {
    private String materialName;
    private Integer materialNum;
    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Integer getMaterialNum() {
        return materialNum;
    }

    public void setMaterialNum(Integer materialNum) {
        this.materialNum = materialNum;
    }
}
