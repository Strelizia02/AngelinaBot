package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class SkinInfo {
    private String operatorId;
    private String operatorName;
    private String skinGroupName;
    private String skinName;
    private String skinBase64;
    private String dialog;
    private String drawerName;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getSkinGroupName() {
        return skinGroupName;
    }

    public void setSkinGroupName(String skinGroupName) {
        this.skinGroupName = skinGroupName;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public String getSkinBase64() {
        return skinBase64;
    }

    public void setSkinBase64(String skinBase64) {
        this.skinBase64 = skinBase64;
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public String getDrawerName() {
        return drawerName;
    }

    public void setDrawerName(String drawerName) {
        this.drawerName = drawerName;
    }
}
