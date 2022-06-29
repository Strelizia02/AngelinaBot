package top.strelitzia.model;

public class EquipInfo {

    private String equipId;
    private String equipName;
    private String charId;
    private Integer phase;
    private Integer level;
    private String desc;
    private Integer equipLevel;

    public Integer getEquipLevel() {
        return equipLevel;
    }

    public void setEquipLevel(Integer equipLevel) {
        this.equipLevel = equipLevel;
    }

    public String getEquipId() {
        return equipId;
    }

    public void setEquipId(String equipId) {
        this.equipId = equipId;
    }

    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public String getCharId() {
        return charId;
    }

    public void setCharId(String charId) {
        this.charId = charId;
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
