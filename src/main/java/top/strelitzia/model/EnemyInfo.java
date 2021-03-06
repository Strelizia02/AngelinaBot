package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class EnemyInfo {
    private String enemyId;
    private String name;
    private Integer atk;
    private Double baseAttackTime;
    private Integer def;
    private Integer hpRecoveryPerSec;
    private Integer magicResistance;
    private Integer massLevel;
    private Integer maxHp;
    private Double moveSpeed;
    private Double rangeRadius;
    private Integer silenceImmune;
    private Integer sleepImmune;
    private Integer stunImmune;
    private Integer level;

    public EnemyInfo() {
    }

    public EnemyInfo(String enemyId, String name, Integer atk, Double baseAttackTime, Integer def, Integer hpRecoveryPerSec, Integer magicResistance, Integer massLevel, Integer maxHp, Double moveSpeed, Double rangeRadius, Integer silenceImmune, Integer sleepImmune, Integer stunImmune, Integer level) {
        this.enemyId = enemyId;
        this.name = name;
        this.atk = atk;
        this.baseAttackTime = baseAttackTime;
        this.def = def;
        this.hpRecoveryPerSec = hpRecoveryPerSec;
        this.magicResistance = magicResistance;
        this.massLevel = massLevel;
        this.maxHp = maxHp;
        this.moveSpeed = moveSpeed;
        this.rangeRadius = rangeRadius;
        this.silenceImmune = silenceImmune;
        this.sleepImmune = sleepImmune;
        this.stunImmune = stunImmune;
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(String enemyId) {
        this.enemyId = enemyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAtk() {
        return atk;
    }

    public void setAtk(Integer atk) {
        this.atk = atk;
    }

    public Double getBaseAttackTime() {
        return baseAttackTime;
    }

    public void setBaseAttackTime(Double baseAttackTime) {
        this.baseAttackTime = baseAttackTime;
    }

    public Integer getDef() {
        return def;
    }

    public void setDef(Integer def) {
        this.def = def;
    }

    public Integer getHpRecoveryPerSec() {
        return hpRecoveryPerSec;
    }

    public void setHpRecoveryPerSec(Integer hpRecoveryPerSec) {
        this.hpRecoveryPerSec = hpRecoveryPerSec;
    }

    public Integer getMagicResistance() {
        return magicResistance;
    }

    public void setMagicResistance(Integer magicResistance) {
        this.magicResistance = magicResistance;
    }

    public Integer getMassLevel() {
        return massLevel;
    }

    public void setMassLevel(Integer massLevel) {
        this.massLevel = massLevel;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public Double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(Double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public Double getRangeRadius() {
        return rangeRadius;
    }

    public void setRangeRadius(Double rangeRadius) {
        this.rangeRadius = rangeRadius;
    }

    public Integer getSilenceImmune() {
        return silenceImmune;
    }

    public void setSilenceImmune(Integer silenceImmune) {
        this.silenceImmune = silenceImmune;
    }

    public Integer getSleepImmune() {
        return sleepImmune;
    }

    public void setSleepImmune(Integer sleepImmune) {
        this.sleepImmune = sleepImmune;
    }

    public Integer getStunImmune() {
        return stunImmune;
    }

    public void setStunImmune(Integer stunImmune) {
        this.stunImmune = stunImmune;
    }

    @Override
    public String toString() {
        String silence = silenceImmune == 0 ? "???" : "???";
        String sleep = sleepImmune == 0 ? "???" : "???";
        String stun = stunImmune == 0 ? "???" : "???";
        if (name.contains("??????")){
            return "??????" + name + "???" + level + "????????????????????????:\n" +
                    "????????????" + atk +
                    "\t ???????????????" + baseAttackTime +
                    "???\n?????????" + def +
                    "\t?????????" + magicResistance +
                    "\n?????????" + maxHp +
                    "\t???????????????" + hpRecoveryPerSec +
                    "\n?????????" + massLevel +
                    "\t???????????????" + moveSpeed +
                    "\n???????????????" + rangeRadius +
                    "???\n????????????" + silence +
                    "\n????????????" + sleep +
                    "\n????????????" + stun;
        }
        else {
            return "??????" + name + "???" + level + "????????????????????????:\n" +
                    "????????????" + atk +
                    "\t ???????????????" + baseAttackTime +
                    "???\n?????????" + def +
                    "\t?????????" + magicResistance +
                    "\n?????????" + maxHp +
                    "\t???????????????" + hpRecoveryPerSec +
                    "\n?????????" + massLevel +
                    "\t???????????????" + moveSpeed +
                    "\n???????????????" + rangeRadius +
                    "???\n????????????" + silence +
                    "\n????????????" + sleep +
                    "\n????????????" + stun;
        }
    }
}
