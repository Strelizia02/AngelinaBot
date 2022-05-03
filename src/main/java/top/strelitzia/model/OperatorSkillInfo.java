package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 干员技能信息
 **/
public class OperatorSkillInfo {
    private Integer operatorId;
    private Integer skillIndex;
    private String skillName;
    private String skillIdYj;

    public String getSkillIdYj() {
        return skillIdYj;
    }

    public void setSkillIdYj(String skillIdYj) {
        this.skillIdYj = skillIdYj;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
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
}
