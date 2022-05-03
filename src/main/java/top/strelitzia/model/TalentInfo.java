package top.strelitzia.model;

/**
 * @author wangzy
 * @Date 2021/4/14 11:16
 **/
public class TalentInfo {
    private Integer operatorId;
    private String talentName;
    private String description;
    private Integer phase;
    private Integer level;
    private Integer potential;

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getTalentName() {
        return talentName;
    }

    public void setTalentName(String talentName) {
        this.talentName = talentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getPotential() {
        return potential;
    }

    public void setPotential(Integer potential) {
        this.potential = potential;
    }
}
