package top.strelitzia.model;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 干员详细信息
 **/
public class OperatorInfo {
    private Integer operator_id;
    private String operator_name;
    private Integer operator_rarity;
    private Integer operator_class;
    private Integer available;
    private Integer in_limit;

    public Integer getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(Integer operator_id) {
        this.operator_id = operator_id;
    }

    public String getOperator_name() {
        return operator_name;
    }

    public void setOperator_name(String operator_name) {
        this.operator_name = operator_name;
    }

    public Integer getOperator_rarity() {
        return operator_rarity;
    }

    public void setOperator_rarity(Integer operator_rarity) {
        this.operator_rarity = operator_rarity;
    }

    public Integer getOperator_class() {
        return operator_class;
    }

    public void setOperator_class(Integer operator_class) {
        this.operator_class = operator_class;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getIn_limit() {
        return in_limit;
    }

    public void setIn_limit(Integer in_limit) {
        this.in_limit = in_limit;
    }
}
