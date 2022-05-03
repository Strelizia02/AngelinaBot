package top.strelitzia.model;

import java.util.Objects;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class BuildingSkill {
    private Integer operatorId;
    private Integer phase;
    private Integer level;
    private String buffName;
    private String roomType;
    private String description;

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
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

    public String getBuffName() {
        return buffName;
    }

    public void setBuffName(String buffName) {
        this.buffName = buffName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingSkill that = (BuildingSkill) o;
        return Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(buffName, that.buffName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operatorId, buffName);
    }
}
