package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class OperatorData {
    private Integer id;
    //攻击
    private Integer atk;
    //防御
    private Integer def;
    //生命
    private Integer maxHp;
    //魔抗
    private Integer magicResistance;
    //费用
    private Integer cost;
    //阻挡
    private Integer blockCnt;
    //攻击间隔
    private Double baseAttackTime;
    //再部署
    private Integer respawnTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAtk() {
        return atk;
    }

    public void setAtk(Integer atk) {
        this.atk = atk;
    }

    public Integer getDef() {
        return def;
    }

    public void setDef(Integer def) {
        this.def = def;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public Integer getMagicResistance() {
        return magicResistance;
    }

    public void setMagicResistance(Integer magicResistance) {
        this.magicResistance = magicResistance;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getBlockCnt() {
        return blockCnt;
    }

    public void setBlockCnt(Integer blockCnt) {
        this.blockCnt = blockCnt;
    }

    public Double getBaseAttackTime() {
        return baseAttackTime;
    }

    public void setBaseAttackTime(Double baseAttackTime) {
        this.baseAttackTime = baseAttackTime;
    }

    public Integer getRespawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(Integer respawnTime) {
        this.respawnTime = respawnTime;
    }
}
