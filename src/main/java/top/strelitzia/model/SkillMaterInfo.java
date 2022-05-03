package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 技能专精材料信息
 **/
public class SkillMaterInfo {
    private Integer skillId;
    private Integer materLevel;
    private Integer useMaterialId;
    private Integer useNumber;

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public Integer getMaterLevel() {
        return materLevel;
    }

    public void setMaterLevel(Integer materLevel) {
        this.materLevel = materLevel;
    }

    public Integer getUseMaterialId() {
        return useMaterialId;
    }

    public void setUseMaterialId(Integer useMaterialId) {
        this.useMaterialId = useMaterialId;
    }

    public Integer getUseNumber() {
        return useNumber;
    }

    public void setUseNumber(Integer useNumber) {
        this.useNumber = useNumber;
    }
}
