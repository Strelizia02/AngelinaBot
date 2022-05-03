package top.strelitzia.model;

/**
 * @author wangzy
 * @Date 2020/12/14 11:12
 * 材料以及数量信息
 **/
public class MaterialInfo {
    private String materialName;
    private Integer materialNum;

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
