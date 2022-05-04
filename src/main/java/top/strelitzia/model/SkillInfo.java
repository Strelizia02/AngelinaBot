package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class SkillInfo {
    private Integer skillId;
    private Integer operatorId;
    private Integer skillIndex;
    private String skillName;
    private String skillPng;
    private String skillIdYj;

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public Integer getSkillIndex() {
        return skillIndex;
    }

    public void setSkillIndex(Integer skillIndex) {
        this.skillIndex = skillIndex;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillPng() {
        return skillPng;
    }

    public void setSkillPng(String skillPng) {
        this.skillPng = skillPng;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getSkillIdYj() {
        return skillIdYj;
    }

    public void setSkillIdYj(String skillIdYj) {
        this.skillIdYj = skillIdYj;
    }
}
