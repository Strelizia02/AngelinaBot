package top.strelitzia.model;

import java.io.Serializable;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class GroupAdminInfo {
    private Long groupId;
    private Integer found;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getFound() {
        return found;
    }

    public void setFound(Integer found) {
        this.found = found;
    }
}
