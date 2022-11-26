package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 干员基础档案
 **/
public class OperatorBasicInfo {
    private Integer operatorId;

    private String operatorName;

    private Integer operatorRarity;
    private Integer operatorClass;
    private String charId;
    private String drawName;
    private String cvNameOfCNMandarin;
    private String cvNameOfCNTopolect;
    private String cvNameOfJP;
    private String cvNameOfKR;
    private String cvNameOfEN;
    private String codeName;
    private String sex;
    private String comeFrom;
    private String birthday;
    private String race;
    private Integer height;
    private String infection;
    private String comprehensiveTest;
    private String objectiveResume;
    private String clinicalDiagnosis;
    private String archives1;
    private String archives2;
    private String archives3;
    private String archives4;
    private String promotionInfo;

    public Integer getOperatorRarity() { return operatorRarity; }

    public void setOperatorRarity(Integer operatorRarity) {
        this.operatorRarity = operatorRarity;
    }

    public Integer getOperatorClass() {
        return operatorClass;
    }

    public void setOperatorClass(Integer operatorClass) {
        this.operatorClass = operatorClass;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getCharId() {
        return charId;
    }

    public void setCharId(String charId) { this.charId = charId; }

    public String getDrawName() {
        drawName = drawName.trim();
        return drawName;
    }

    public void setDrawName(String drawName) {
        this.drawName = drawName;
    }

    public String getCvNameOfCNMandarin() { return cvNameOfCNMandarin; }

    public void setCvNameOfCNMandarin(String cvNameOfCNMandarin) { this.cvNameOfCNMandarin = cvNameOfCNMandarin; }

    public String getCvNameOfCNTopolect() { return cvNameOfCNTopolect; }

    public void setCvNameOfCNTopolect(String cvNameOfCNTopolect) { this.cvNameOfCNTopolect = cvNameOfCNTopolect; }

    public String getCvNameOfJP() { return cvNameOfJP; }

    public void setCvNameOfJP(String cvNameOfJP) { this.cvNameOfJP = cvNameOfJP; }

    public String getCvNameOfKR() { return cvNameOfKR; }

    public void setCvNameOfKR(String cvNameOfKR) { this.cvNameOfKR = cvNameOfKR; }

    public String getCvNameOfEN() { return cvNameOfEN; }

    public void setCvNameOfEN(String cvNameOfEN) { this.cvNameOfEN = cvNameOfEN; }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getSex() {
        if(sex==null){
            sex ="";
        }else {
            sex = sex.trim();
        }
        return sex;
    }

    public void setSex(String sex) { this.sex = sex; }

    public String getComeFrom() {
        if(comeFrom==null){
            comeFrom ="";
        }else {
            comeFrom = comeFrom.trim();
        }
        return comeFrom;
    }

    public void setComeFrom(String comeFrom) { this.comeFrom = comeFrom; }

    public String getBirthday() { return birthday; }

    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getRace() {
        if(race==null){
            race ="";
        }else {
            race = race.trim();
        }
        return race;
    }

    public void setRace(String race) { this.race = race; }

    public Integer getHeight() { return height; }

    public void setHeight(Integer height) { this.height = height; }

    public String getInfection() {
        if(infection==null){
            infection ="";
        }else {
            infection = infection.trim();
        }
        return infection;
    }

    public void setInfection(String infection) {
        this.infection = infection;
    }

    public String getComprehensiveTest() {
        return comprehensiveTest;
    }

    public void setComprehensiveTest(String comprehensiveTest) {
        this.comprehensiveTest = comprehensiveTest;
    }

    public String getObjectiveResume() {
        return objectiveResume;
    }

    public void setObjectiveResume(String objectiveResume) {
        this.objectiveResume = objectiveResume;
    }

    public String getClinicalDiagnosis() {
        return clinicalDiagnosis;
    }

    public void setClinicalDiagnosis(String clinicalDiagnosis) {
        this.clinicalDiagnosis = clinicalDiagnosis;
    }

    public String getArchives1() {
        return archives1;
    }

    public void setArchives1(String archives1) {
        this.archives1 = archives1;
    }

    public String getArchives2() {
        return archives2;
    }

    public void setArchives2(String archives2) {
        this.archives2 = archives2;
    }

    public String getArchives3() {
        return archives3;
    }

    public void setArchives3(String archives3) {
        this.archives3 = archives3;
    }

    public String getArchives4() {
        return archives4;
    }

    public void setArchives4(String archives4) {
        this.archives4 = archives4;
    }

    public String getPromotionInfo() {
        return promotionInfo;
    }

    public void setPromotionInfo(String promotionInfo) {
        this.promotionInfo = promotionInfo;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    @Override
    public String toString() {
        return "基础档案：\n" +
                "画师：" + drawName + '\t' +
                "声优：" + cvNameOfJP + '\n' +
                "代号：" + codeName + '\t' +
                "性别：" + sex + '\t' +
                "出身地：" + comeFrom + '\n' +
                "生日：" + birthday + '\t' +
                "种族：" + race + '\t' +
                "身高：" + height + '\n' +
                "矿石病感染情况：" + infection + '\n' +
                "\n综合体检测试：\n" + comprehensiveTest + '\n' +
                "\n客观履历：\n" + objectiveResume + '\n' +
                "\n临床诊断分析：\n" + clinicalDiagnosis + '\n' +
                "\n档案资料一：\n" + archives1 + '\n' +
                "\n档案资料二：\n" + archives2 + '\n' +
                "\n档案资料三：\n" + archives3 + '\n' +
                "\n档案资料四：\n" + archives4 + '\n' +
                "\n晋升记录：\n" + promotionInfo;
    }
}
